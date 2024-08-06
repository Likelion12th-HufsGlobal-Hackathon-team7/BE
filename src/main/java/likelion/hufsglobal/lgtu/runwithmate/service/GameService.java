package likelion.hufsglobal.lgtu.runwithmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.BoxInfo;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfo;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfoForUser;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.*;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.BoxType;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.FinishType;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.User;
import likelion.hufsglobal.lgtu.runwithmate.repository.GameRepository;
import likelion.hufsglobal.lgtu.runwithmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private static final double DISTANCE_THRESHOLD = 0.00102;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public StartCheckResDto checkStart(String roomId, String userId, UserPosition position) {
        String gameStatus = redisTemplate.opsForHash().get("game_rooms:" + roomId, "game_status").toString();
        StartCheckResDto startCheckResDto = new StartCheckResDto();
        startCheckResDto.setStarted(false);
        startCheckResDto.setBetPoint(Long.valueOf((Integer) redisTemplate.opsForHash().get("game_rooms:" + roomId, "bet_point")));

        Map<String, String> userNicknames = new HashMap<>();
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        String userTwoId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user2_id");
        userNicknames.put(userOneId, userRepository.findByUserId(userOneId).orElseThrow(() -> new IllegalArgumentException("User not found")).getNickname());
        userNicknames.put(userTwoId, userRepository.findByUserId(userTwoId).orElseThrow(() -> new IllegalArgumentException("User not found")).getNickname());
        startCheckResDto.setUserNicknames(userNicknames);

        List<BoxInfo> dopamineBoxes = new ArrayList<>();
        List<BoxInfo> pointBoxes = new ArrayList<>();

        if (gameStatus.equals("game_ready")) {
            // 유저 체크 로직 -> 최초 0명 -> 2명 다 찼다면 게임 시작
            Long userCount = redisTemplate.opsForHash().increment("game_rooms:" + roomId, "user_entered", 1);

            // 유저 포인트 초기화
            PlayerPoint userPoints = new PlayerPoint();
            userPoints.setDopamine(0);
            userPoints.setPoint(0);
            redisTemplate.opsForHash().put("player_points:" + roomId, userId, userPoints);
            redisTemplate.opsForHash().put("player_positions:" + roomId, userId, position);

            // 해당 범위 내에서 박스는 도파민-7개, 포인트-5개로 설정
            // 각각 point_boxes:방번호, score_boxes:방번호 집합(Set)에 저장
            Long dopamineCount = redisTemplate.opsForSet().size(BoxType.DOPAMINE.name().toLowerCase() + "_boxes:" + roomId);
            Long pointCount = redisTemplate.opsForSet().size(BoxType.POINT.name().toLowerCase() + "_boxes:" + roomId);
            dopamineBoxes = addBoxes(BoxType.DOPAMINE, 7, roomId, dopamineCount, position);
            pointBoxes = addBoxes(BoxType.POINT, 5, roomId, pointCount, position);

            startCheckResDto.setTimeLeft(Long.valueOf((Integer) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit")));

            if (userCount == 2) {
                String startTimeString = LocalDateTime.now().format(formatter);
                redisTemplate.opsForHash().put("game_rooms:" + roomId, "game_status", "game_started");
                redisTemplate.opsForHash().put("game_rooms:" + roomId, "start_time", startTimeString);
                startCheckResDto.setStarted(true);
            }
        } else {
            Set<Object> dopamineBoxesObj = redisTemplate.opsForSet().members(BoxType.DOPAMINE.name().toLowerCase() + "_boxes:" + roomId);
            Set<Object> pointBoxesObj = redisTemplate.opsForSet().members(BoxType.POINT.name().toLowerCase() + "_boxes:" + roomId);
            for (Object obj : dopamineBoxesObj) {
                ObjectMapper mapper = new ObjectMapper();
                BoxInfo box = mapper.convertValue(obj, BoxInfo.class);
                dopamineBoxes.add(box);
            }
            for (Object obj : pointBoxesObj) {
                ObjectMapper mapper = new ObjectMapper();
                BoxInfo box = mapper.convertValue(obj, BoxInfo.class);
                pointBoxes.add(box);
            }
            startCheckResDto.setTimeLeft(calcTimeLeft(roomId));
            startCheckResDto.setStarted(true);
        }

        startCheckResDto.setDopamineBoxes(dopamineBoxes);
        startCheckResDto.setPointBoxes(pointBoxes);

        return startCheckResDto;
    }

    private List<BoxInfo> addBoxes(BoxType type, int count, String roomId, Long size, UserPosition position) {
        List<BoxInfo> boxes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BoxInfo newBox = createBox(type, 0.003, 0.0002, size+i, position, 10L);
            redisTemplate.opsForSet().add(type.name().toLowerCase() + "_boxes:" + roomId, newBox);
            boxes.add(newBox);
        }
        return boxes;
    }

    private BoxInfo createBox(BoxType boxType, Double range, Double division, long id, UserPosition position, Long amount) {
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setId(id);
        boxInfo.setBoxType(boxType);

        // 위도lat, 경도 lng를 바탕으로 박스 설정.
        // 범위는 각각 현재 위치 기준 0.003 (lat +- 0.003, lng +- 0.003)를 기준으로 하여 0.0002씩의 변동을 기준으로 배정함
        Random random = new Random();
        int maxCount = (int) (range / division);

        Double randomLat = position.getLat() + (random.nextBoolean() ? 1 : -1) * (random.nextInt(maxCount) * 0.0002);
        Double randomLng = position.getLng() + (random.nextBoolean() ? 1 : -1) * (random.nextInt(maxCount) * 0.0002);

        boxInfo.setLat(randomLat);
        boxInfo.setLng(randomLng);
        boxInfo.setBoxAmount(amount);
        return boxInfo;
    }

    private Long calcRunTime(String roomId){
        Object startTimeObject = redisTemplate.opsForHash().get("game_rooms:" + roomId, "start_time");

        if (startTimeObject instanceof String) {
            // 저장된 문자열을 LocalDateTime으로 변환
            LocalDateTime startTime = LocalDateTime.parse((String) startTimeObject, formatter);
            LocalDateTime currentTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, currentTime);
            return duration.getSeconds();
        } else {
            throw new IllegalStateException("게임 시작 시간을 인식할 수 없습니다. String 타입이 아닙니다.");
        }
    }

    private Long calcTimeLeft(String roomId){
        Long runTime = calcRunTime(roomId);
        Long timeLimit = Long.valueOf((Integer) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit"));;
        return timeLimit - runTime;
    }

    // -------------------------------------------------

    public PositionUpdateResDto updatePosition(String roomId, String userId, UserPosition position) {
        /**
         * 1. 해당 플레이어 위치를 `player_position:방번호`방번호 에서 찾음
         * 2. (선택) 현재 위치와 이전 위치의 오차를 계산하고, 비정상적인 변동인지 파악함
         * 3. `player_position:방번호`에서 해당 플레이어 위치 갱신 진행
         */
        // 플레이어의 위치 변경
        redisTemplate.opsForHash().put("player_positions:" + roomId, userId, position);

        removeBox(roomId, userId, position);
        PositionUpdateResDto positionUpdateResDto = new PositionUpdateResDto();
        positionUpdateResDto.setUserId(userId);
        positionUpdateResDto.setPosition(position);
        positionUpdateResDto.setTimeLeft(calcTimeLeft(roomId));

        return positionUpdateResDto;
    }

    // -------------------------------------------------

    public void removeBox(String roomId, String userId, UserPosition position) {
        /**
         * 1. `point_boxes:방번호` 와 `dopamine_boxes:방번호` 에 대하여 플레이어가 특정 박스 주변인지 파악
         * 2. `point_boxes:방번호` 와 `dopamine_boxes:방번호` 에 대하여 해당 박스 `"{'id': 'box1', 'lat': 10, 'lng': 20}"`  제거
         * 3. `player_points:방번호`에서 해당 플레이어에 대한 포인트를 상승(HINCRBY) `points_p1`
         *  * 3-1. player_points는 HSet 형태로 저장되어 있음
         *  * 3-2. userId : {”point”:10, “dopamine”:20} 형태로 저장되어 있음
         * 4. 제거된 박스 프론트한테 공지
         */

        // 1. 특정 박스 주변인지 파악
        List<BoxInfo> nearbyBoxes = findNearbyBoxes(roomId, position);
        if (nearbyBoxes.isEmpty()) {
            return;
        }

        removeBoxes(roomId, nearbyBoxes); // 박스 제거
        incrementPlayerPoints(roomId, userId, nearbyBoxes); // 포인트 상승
        notifyBoxRemoval(roomId, userId, nearbyBoxes); // 박스 제거 로깅
    }

    private List<BoxInfo> findNearbyBoxes(String roomId, UserPosition position) {
        List<BoxInfo> nearbyBoxes = new ArrayList<>();
        Set<Object> pointBoxes = redisTemplate.opsForSet().members("point_boxes:" + roomId);
        Set<Object> dopamineBoxes = redisTemplate.opsForSet().members("dopamine_boxes:" + roomId);

        for (Object obj : pointBoxes) {
            ObjectMapper mapper = new ObjectMapper();
            BoxInfo box = mapper.convertValue(obj, BoxInfo.class);
            if (isNearby(box, position)) {
                nearbyBoxes.add(box);
            }
        }

        for (Object obj : dopamineBoxes) {
            ObjectMapper mapper = new ObjectMapper();
            BoxInfo box = mapper.convertValue(obj, BoxInfo.class);
            if (isNearby(box, position)) {
                nearbyBoxes.add(box);
            }
        }

        return nearbyBoxes;
    }

    private boolean isNearby(BoxInfo box, UserPosition position) {
        double distance = Math.sqrt(Math.pow(box.getLat() - position.getLat(), 2) + Math.pow(box.getLng() - position.getLng(), 2));
        return distance < DISTANCE_THRESHOLD;
    }

    private void removeBoxes(String roomId, List<BoxInfo> boxes) {
        for (BoxInfo box : boxes) {
            if (box.getBoxType() == BoxType.POINT) {
                redisTemplate.opsForSet().remove("point_boxes:" + roomId, box);
            } else if (box.getBoxType() == BoxType.DOPAMINE) {
                redisTemplate.opsForSet().remove("dopamine_boxes:" + roomId, box);
            }
        }
    }

    private void incrementPlayerPoints(String roomId, String userId, List<BoxInfo> boxes) {
        for (BoxInfo box : boxes) {
            Object object = redisTemplate.opsForHash().get("player_points:" + roomId, userId);
            ObjectMapper mapper = new ObjectMapper();
            PlayerPoint userPoints = mapper.convertValue(object, PlayerPoint.class);
            if (box.getBoxType() == BoxType.POINT) {
//                redisTemplate.opsForHash().increment("player_points:" + roomId, userId, (int)(long) box.getBoxAmount());
                userPoints.setPoint(userPoints.getPoint() + (int)(long) box.getBoxAmount());
            } else if (box.getBoxType() == BoxType.DOPAMINE) {
//                redisTemplate.opsForHash().increment("player_points:" + roomId, userId, (int)(long) box.getBoxAmount());
                userPoints.setDopamine(userPoints.getDopamine() + (int)(long) box.getBoxAmount());
            }
            redisTemplate.opsForHash().put("player_points:" + roomId, userId, userPoints);
        }
    }

    private void notifyBoxRemoval(String roomId, String userId, List<BoxInfo> boxes) {
        for (BoxInfo box : boxes) {
            BoxRemoveResDto boxRemoveResDto = new BoxRemoveResDto();
            boxRemoveResDto.setBoxType(box.getBoxType());
            boxRemoveResDto.setBoxId(box.getId());
            boxRemoveResDto.setBoxAmount(box.getBoxAmount().intValue());
            boxRemoveResDto.setUserId(userId);

            messagingTemplate.convertAndSend("/room/" + roomId, boxRemoveResDto);
        }

        // 모든 유저의 포인트 반환하기
        PlayerPointsResDto playerPointsResDto = new PlayerPointsResDto();
        Map<String, PlayerPoint> playerPoints = new HashMap<>();
        Set<Object> userIds = redisTemplate.opsForHash().keys("player_points:" + roomId);
        for (Object obj : userIds) {
            String user = (String) obj;
            Object object = redisTemplate.opsForHash().get("player_points:" + roomId, user);
            ObjectMapper mapper = new ObjectMapper();
            PlayerPoint userPoints = mapper.convertValue(object, PlayerPoint.class);
            playerPoints.put(user, userPoints);
        }
        playerPointsResDto.setPlayerPoints(playerPoints);
        messagingTemplate.convertAndSend("/room/" + roomId, playerPointsResDto);
    }

    // -------------------------------------------------

    public GameFinishResDto finishGame(String roomId, FinishType finishType, String surrenderId) {
        /**
         * 1. dopamine이 높은 유저가 승자
         * * 1-1. dopamine이 같다면 호스트가 승자
         * 2. MySQL에 결과 저장
         * 3. redis 데이터 삭제
         * 4. 결과 반환
         */

        // "player_points:" + roomId, userId : {"point":10, "dopamine":20}
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:"+roomId,"user1_id");
        String userTwoId = (String) redisTemplate.opsForHash().get("game_rooms:"+roomId,"user2_id");

        // user1 인게임 도파민/포인트 빼오기
        Map<String, Long> userOnePoints = (Map<String, Long>) redisTemplate.opsForHash().get("player_points:" + roomId, userOneId);
        Long userOneDopamine = userOnePoints.get("dopamine");
        Long userOneGamePoint = userOnePoints.get("point");

        // user2 인게임 도파민/포인트 빼오기
        Map<String, Long> userTwoPoints = (Map<String, Long>) redisTemplate.opsForHash().get("player_points:" + roomId, userTwoId);
        Long userTwoDopamine = userTwoPoints.get("dopamine");
        Long userTwoGamePoint = userTwoPoints.get("point");


        boolean isUserOneWin = userOneDopamine >= userTwoDopamine;
        if (finishType.equals(FinishType.PLAYER_SURRENDER)){
            isUserOneWin = !surrenderId.equals(userOneId);
        }

        GameFinishResDto gameFinishResDto = new GameFinishResDto();
        gameFinishResDto.setFinishType(finishType);
        gameFinishResDto.setWinner(isUserOneWin ? userOneId : userTwoId);

        User userOne = userRepository.findByUserId(userOneId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String userOneName = userOne.getNickname();

        User userTwo = userRepository.findByUserId(userTwoId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String userTwoName = userTwo.getNickname();

        GameFinishInfoForUser userOneInfo = new GameFinishInfoForUser();
        userOneInfo.setUserId(userOneId);
        userOneInfo.setUserName(userOneName);
        userOneInfo.setDopamine(userOneDopamine);
        userOneInfo.setPoint(userOneGamePoint);

        GameFinishInfoForUser userTwoInfo = new GameFinishInfoForUser();
        userTwoInfo.setUserId(userTwoId);
        userTwoInfo.setUserName(userTwoName);
        userTwoInfo.setDopamine(userTwoDopamine);
        userTwoInfo.setPoint(userTwoGamePoint);

        List<GameFinishInfoForUser> usersInfoList = List.of(userOneInfo, userTwoInfo);
        gameFinishResDto.setUsersInfo(usersInfoList);

        // MySQL에 저장하기 -> Repository에 저장
        // user1 GameInfoForUser에 저장
        GameInfoForUser newGameInfoForUser1 = new GameInfoForUser();
        newGameInfoForUser1.setUserId(userOneId);
        newGameInfoForUser1.setDopamine(userOneDopamine);
        newGameInfoForUser1.setPoint(userOneGamePoint);

        // user2 GameInfoForUser에 저장
        GameInfoForUser newGameInfoForUser2 = new GameInfoForUser();
        newGameInfoForUser2.setUserId(userTwoId);
        newGameInfoForUser2.setDopamine(userTwoDopamine);
        newGameInfoForUser2.setPoint(userTwoGamePoint);

        // user1Info와 user2Info를 담을 List 만들기
        List<GameInfoForUser> newUsersInfo = new ArrayList<>();
        newUsersInfo.add(newGameInfoForUser1);
        newUsersInfo.add(newGameInfoForUser2);

        // 배팅 포인트 가져오기
        Long betPoint = (Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "bet_point");

        GameInfo newGameInfo = new GameInfo();
        newGameInfo.setRoomId(roomId);
        newGameInfo.setBetPoint(betPoint);
        newGameInfo.setUsersInfo(newUsersInfo);

        // mysql에 데이터 저장하기
        gameRepository.save(newGameInfo);

        // TODO : 포인트 소매넣기 -> [ 완 ] ADD : 포인트 뺸 후 소매넣기
        // userOnePoint - user1 포인트 / userOneGamePoint - user1이 게임에서 얻은 포인트
        Long userOnePoint = userOne.getPoint();
        Long userTwoPoint = userTwo.getPoint();

        if (isUserOneWin) {
            userOne.setPoint(userOnePoint + betPoint + userOneGamePoint);
            userTwo.setPoint(userTwoPoint - betPoint + userTwoGamePoint);
        } else {
            userOne.setPoint(userOnePoint - betPoint + userOneGamePoint);
            userTwo.setPoint(userTwoPoint + betPoint + userTwoGamePoint);
        }

        // redis에서 데이터 삭제하기
        redisTemplate.delete("game_rooms:" + roomId);
        redisTemplate.delete("point_boxes:" + roomId);
        redisTemplate.delete("dopamine_boxes:" + roomId);
        redisTemplate.delete("player_positions:" + roomId);
        redisTemplate.delete("player_points:" + roomId);

        // 결과값 반환하기
        return gameFinishResDto;
    }

    // user가 했던 Game return
    public List<GameInfo> getAllGameInfos(String userId) {
        return gameRepository.findAllByUserId(userId);
    }

}

package likelion.hufsglobal.lgtu.runwithmate.service;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.BoxInfo;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfo;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfoForUser;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.*;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.BoxType;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.FinishType;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.CheckUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.User;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.type.statusType;
import likelion.hufsglobal.lgtu.runwithmate.repository.GameRepository;
import likelion.hufsglobal.lgtu.runwithmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {
    private static final double DISTANCE_THRESHOLD = 0.0001;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    // 출석체크
    public CheckUpdateResDto updateCheck(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime userLastCheck = findUserLastCheck(userId);

        if (userLastCheck == null) {
            userLastCheck = now.minusDays(1);
        }

        CheckUpdateResDto checkUpdateResDto = new CheckUpdateResDto();

        if (now.toLocalDate() == userLastCheck.toLocalDate()) {
            checkUpdateResDto.setStatus(statusType.FAIL);
            checkUpdateResDto.setMessage("이미 금일 출석체크가 완료되었습니다.");
        }

        User selectedUser = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        Long userPoint = selectedUser.getPoint();
        selectedUser.setPoint(userPoint + 500L);
        selectedUser.setLastCheck(now);

        checkUpdateResDto.setStatus(statusType.SUCCESS);
        checkUpdateResDto.setMessage("성공적으로 출석체크되었습니다.");

        return checkUpdateResDto;
    }

    public LocalDateTime findUserLastCheck(String userId) {
        User selectedUser = userRepository.findByUserId(userId).orElse(null);
        if (selectedUser == null) {
            return null;
        }
        return selectedUser.getLastCheck();
    }
    
    @Transactional
    public StartCheckResDto checkStart(String roomId, String userId, UserPosition position) {
        // 유저 체크 로직 -> 최초 0명 -> 2명 다 찼다면 게임 시작
        Long userCount = (Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user_entered");
        redisTemplate.opsForHash().increment("game_rooms:" + roomId, "user_entered", 1);

        // 유저 포인트 초기화
        Map<String, Long> userPoints = new HashMap<>();
        userPoints.put("point",0L);
        userPoints.put("dopamine",0L);
        redisTemplate.opsForHash().put("player_points:" + roomId, userId, userPoints);
        // 유저 위치 저장하기
        redisTemplate.opsForHash().put("player_positions:" + roomId, userId, position);

        // 해당 범위 내에서 박스는 도파민-7개, 포인트-5개로 설정
        // 각각 point_boxes:방번호, score_boxes:방번호 집합(Set)에 저장
        Long dopamineCount = redisTemplate.opsForSet().size(BoxType.DOPAMINE.name().toLowerCase() + "_boxes:" + roomId);
        Long pointCount = redisTemplate.opsForSet().size(BoxType.POINT.name().toLowerCase() + "_boxes:" + roomId);
        List<BoxInfo> dopamineBoxes = addBoxes(BoxType.DOPAMINE, 7, roomId, dopamineCount, position);
        List<BoxInfo> pointBoxes = addBoxes(BoxType.POINT, 5, roomId, pointCount, position);

        StartCheckResDto startCheckResDto = new StartCheckResDto();
        startCheckResDto.setStarted(false);
        startCheckResDto.setDopamineBoxes(dopamineBoxes);
        startCheckResDto.setPointBoxes(pointBoxes);
        startCheckResDto.setTimeLeft((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit"));

        if (userCount == 2) {
            redisTemplate.opsForHash().put("game_rooms:" + roomId, "start_time", LocalDateTime.now());
            startCheckResDto.setStarted(true);
        }

        return startCheckResDto;
    }

    private List<BoxInfo> addBoxes(BoxType type, int count, String roomId, Long size, UserPosition position) {
        List<BoxInfo> boxes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BoxInfo newBox = createBox(type, 0.003, 0.0002, size+i, position, 50L);
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
        LocalDateTime startTime = (LocalDateTime) redisTemplate.opsForHash().get("game_rooms:" + roomId, "start_time");
        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, currentTime);
        return duration.getSeconds();
    }

    private Long calcTimeLeft(String roomId){
        Long runTime = calcRunTime(roomId);
        Long timeLimit = (Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit");;
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

        BoxRemoveResDto response = new BoxRemoveResDto();

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
        List<Object> pointBoxes = redisTemplate.opsForList().range("point_boxes:" + roomId, 0, -1);
        List<Object> dopamineBoxes = redisTemplate.opsForList().range("dopamine_boxes:" + roomId, 0, -1);

        for (Object obj : pointBoxes) {
            BoxInfo box = (BoxInfo) obj;
            if (isNearby(box, position)) {
                nearbyBoxes.add(box);
            }
        }

        for (Object obj : dopamineBoxes) {
            BoxInfo box = (BoxInfo) obj;
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
                redisTemplate.opsForList().remove("point_boxes:" + roomId, 1, box);
            } else if (box.getBoxType() == BoxType.DOPAMINE) {
                redisTemplate.opsForList().remove("dopamine_boxes:" + roomId, 1, box);
            }
        }
    }

    private void incrementPlayerPoints(String roomId, String userId, List<BoxInfo> boxes) {
        String playerPointsKey = "player_points:" + roomId;
        for (BoxInfo box : boxes) {
            if (box.getBoxType() == BoxType.POINT) {
                redisTemplate.opsForHash().increment(playerPointsKey, userId, box.getBoxAmount());
            } else if (box.getBoxType() == BoxType.DOPAMINE) {
                redisTemplate.opsForHash().increment(playerPointsKey, userId, box.getBoxAmount());
            }
        }
    }

    private void notifyBoxRemoval(String roomId, String userId, List<BoxInfo> boxes) {
        for (BoxInfo box : boxes) {
            // 박스 제거를 공지하는 로직을 여기에 추가 (예: 메시지 큐, 웹소켓 등)
            BoxRemoveResDto boxRemoveResDto = new BoxRemoveResDto();
            boxRemoveResDto.setBoxType(box.getBoxType());
            boxRemoveResDto.setBoxId(box.getId());
            boxRemoveResDto.setBoxAmount(box.getBoxAmount().intValue());
            boxRemoveResDto.setUserId(userId);

            messagingTemplate.convertAndSend("/room/" + roomId, boxRemoveResDto);
        }
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

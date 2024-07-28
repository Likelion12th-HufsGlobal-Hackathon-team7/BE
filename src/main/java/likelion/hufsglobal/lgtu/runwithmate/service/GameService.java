package likelion.hufsglobal.lgtu.runwithmate.service;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.BoxInfo;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.BoxRemoveResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.GameFinishResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.PositionUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.StartCheckResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.BoxType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public StartCheckResDto checkStart(String roomId, String userId, UserPosition position) {
        // TODO : Refactor -> 전원 입장시에만 유저에게 보내도록
        // 유저 체크 로직 -> 최초 0명 -> 2명 다 찼다면 게임 시작
        Long userCount = (Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user_entered");
        redisTemplate.opsForHash().increment("game_rooms:" + roomId, "user_entered", 1);

        // 유저 위치 저장하기
        redisTemplate.opsForHash().put("player_positions:" + roomId, userId,position);

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
        return new PositionUpdateResDto();
    }

    public BoxRemoveResDto removeBox(String roomId, String userId) {
        /**
         * 1. `point_boxes:방번호` 와 `dopamine_boxes:방번호` 에 대하여 플레이어가 특정 박스 주변인지 파악
         * 2. `point_boxes:방번호` 와 `dopamine_boxes:방번호` 에 대하여 해당 박스 `"{'id': 'box1', 'lat': 10, 'lng': 20}"`  제거
         * 3. `player_points:방번호`에서 해당 플레이어에 대한 포인트를 상승(HINCRBY) `points_p1`
         *  * 3-1. player_points는 HSet 형태로 저장되어 있음
         *  * 3-2. userId : {”point”:10, “dopamine”:20} 형태로 저장되어 있음
         * 4. 제거된 박스 프론트한테 공지
         */
        return new BoxRemoveResDto();
    }


    public GameFinishResDto finishGame(String roomId) {
        /**
         * 1. dopamine이 높은 유저가 승자
         * * 1-1. dopamine이 같다면 호스트가 승자
         * 2. MySQL에 결과 저장
         * 3. redis 데이터 삭제
         * 4. 결과 반환
         */
        return new GameFinishResDto();
    }

}

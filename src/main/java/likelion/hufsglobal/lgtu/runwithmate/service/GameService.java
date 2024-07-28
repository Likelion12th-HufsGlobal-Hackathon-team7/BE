package likelion.hufsglobal.lgtu.runwithmate.service;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.*;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomJoinResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final RedisTemplate<String, Object> redisTemplate;

    public StartCheckResDto checkStart(String roomId, String userId, Map<String, Object> position) {
        // userId에 따라 위치 저장하고, 만약 위치가 없으면 한 명이 준비 안되어있다고 판단하기
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");

        if (userId.equals(userOneId)) {

        }

        // 도파민 박스 생성
        Random random = new Random();

        List<Map<String, Object>> dopaminBoxes = new ArrayList<>();
        for (int i = 1; i<=10; i++) {
            Integer x = random.nextInt(100);
            Integer y = random.nextInt(100);

            Map<String, Object> box = new HashMap<>();
            box.put("box_type", "dopamin");
            box.put("id", i);
            box.put("x", x);
            box.put("y", y);
            box.put("point", 10);

            dopaminBoxes.add(box);

            // 도파민 박스 - redis
            Map<String, Object> dopaminBox = new HashMap<>();
            dopaminBox.put("id", "box" + i);
            dopaminBox.put("x", x);
            dopaminBox.put("y", y);
            redisTemplate.opsForSet().add("score_boxes:" + roomId, dopaminBox);
        }

        // 포인트 박스 생성
        List<Map<String, Object>> pointBoxes = new ArrayList<>();
        for (int i = 1; i<=10; i++) {
            Integer x = random.nextInt(100);
            Integer y = random.nextInt(100);

            Map<String, Object> box = new HashMap<>();
            box.put("box_type", "point");
            box.put("id", i);
            box.put("x", x);
            box.put("y", y);
            box.put("point", 10);

            pointBoxes.add(box);

            // 포인트 박스 - redis
            Map<String, Object> pointBox = new HashMap<>();
            pointBox.put("id", "box" + i);
            pointBox.put("x", x);
            pointBox.put("y", y);
            redisTemplate.opsForSet().add("point_boxes:" + roomId, pointBox);
        }

        // time_left
        Long startTime = System.currentTimeMillis(); // 시작 시간
        Long afterTime = System.currentTimeMillis(); // 현재 시간
        Long runTime = (afterTime-startTime)/1000; //sec

        Long timeLimit = (Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit");
        Long longTimeLeft = timeLimit - runTime;
        Integer timeLeft = longTimeLeft.intValue();

        StartCheckResDto startCheckResDto = new StartCheckResDto();
        startCheckResDto.setStarted(true);
        startCheckResDto.setPointBoxes(dopaminBoxes);
        startCheckResDto.setPointBoxes(pointBoxes);
        startCheckResDto.setTimeLeft(timeLeft);

        return startCheckResDto;
    }

//    public PositionUpdateResDto updatePosition(String roomId, String userId, Map position) {
//
//    }

//    public BoxRemoveResDto removeBox(String roomId, String userId) {
//
//    }
//
//    public GameSurrenderResDto finishGame(String roomId) {
//        // 게임 그냥 종료 시...?
//
//        GameSurrenderResDto gameSurrenderResDto = new GameSurrenderResDto();
//        gameSurrenderResDto.setPointP1();
//        gameSurrenderResDto.setPointP2();
//        gameSurrenderResDto.setDopaminP1();
//        gameSurrenderResDto.setDopaminP2();
//
//        if (dopaminP1 > dapaminP2) {
//            String gameWinner = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
//            String gameWinnerName = "유저1"; // userId를 통해서 가져오기
//            String gameLoserName = "유저2";
//
//            gameSurrenderResDto.setWinner(gameWinner);
//            gameSurrenderResDto.setWinnerName(gameWinnerName);
//            gameSurrenderResDto.setLoserName(gameLoserName);
//        } else if (dopaminP1 == dopaminP2) {
//            // 동점 흠...
//        } else {
//            String gameWinner = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user2_id");
//            String gameWinnerName = "유저2";
//            String gameLoserName = "유저1";
//
//            gameSurrenderResDto.setWinner(gameWinner);
//            gameSurrenderResDto.setWinnerName(gameWinnerName);
//            gameSurrenderResDto.setLoserName(gameLoserName);
//        }
//    }
}

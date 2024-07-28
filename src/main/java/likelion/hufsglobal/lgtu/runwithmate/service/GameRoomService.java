package likelion.hufsglobal.lgtu.runwithmate.service;

import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.GameStartResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomJoinResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomUpdateResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Long DEFAULT_BET_POINT = 1000L;
    private static final Long DEFAULT_TIME_LIMIT = 60L;

    public String createRoom(String userId) {
        // 방 ID 생성
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        while (Boolean.TRUE.equals(redisTemplate.hasKey("game_rooms:" + roomId))) {
            roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "bet_point", DEFAULT_BET_POINT);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "time_limit", DEFAULT_TIME_LIMIT);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "user1_id", userId);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "game_status", "room_created");

        return roomId;
    }

    public RoomUpdateResDto updateRoom(String roomId, String userId, Long betPoint, Long timeLimit) {
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        if (!userId.equals(userOneId)) {
            // TODO : 유저들의 포인트가 미달된 경우에도 이 로직에 포함시키기
            RoomUpdateResDto roomUpdateResDto = new RoomUpdateResDto();
            roomUpdateResDto.setStatus(false);
            roomUpdateResDto.setBetPoint((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "bet_point"));
            roomUpdateResDto.setTimeLimit((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit"));
            return roomUpdateResDto;
        }

        redisTemplate.opsForHash().put("game_rooms:" + roomId, "bet_point", betPoint);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "time_limit", timeLimit);

        RoomUpdateResDto roomUpdateResDto = new RoomUpdateResDto();
        roomUpdateResDto.setStatus(true);
        roomUpdateResDto.setBetPoint(betPoint);
        roomUpdateResDto.setTimeLimit(timeLimit);
        return roomUpdateResDto;


    }

    public RoomJoinResDto joinRoom(String roomId, String userId) {
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "user2_id", userId);
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        String userTwoId = userId;

        String userOneName = "유저1"; // 나중에 userOneId로 이름을 가져와야 함
        String userTwoName = "유저2"; // 나중에 userTwoId로 이름을 가져와야 함


        RoomJoinResDto roomJoinResDto = new RoomJoinResDto();
        roomJoinResDto.setUser1(userOneName);
        roomJoinResDto.setUser2(userTwoName);
        roomJoinResDto.setBetPoint((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "bet_point"));
        roomJoinResDto.setTimeLimit((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit"));
        return roomJoinResDto;
    }

    public GameStartResDto startGame(String roomId) {
        // TODO : 유저 포인트가 모자라면 컷
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "game_status", "game_started");
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "user_entered", 0);

        GameStartResDto gameStartResDto = new GameStartResDto();
        gameStartResDto.setRoomId(roomId);

        return gameStartResDto;
    }
}

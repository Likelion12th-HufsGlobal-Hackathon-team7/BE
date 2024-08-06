package likelion.hufsglobal.lgtu.runwithmate.service;

import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.GameStartResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomCreateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomJoinResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.User;
import likelion.hufsglobal.lgtu.runwithmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long DEFAULT_BET_POINT = 1000L;
    private static final long DEFAULT_TIME_LIMIT = 60L;

    private final UserRepository userRepository;

    public RoomCreateResDto createRoom(String userId) {
        // 방 ID 생성
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        while (Boolean.TRUE.equals(redisTemplate.hasKey("game_rooms:" + roomId))) {
            roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "bet_point", DEFAULT_BET_POINT);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "time_limit", DEFAULT_TIME_LIMIT);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "user1_id", userId);
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "game_status", "room_created");

        // 유저 포인트
        User selectedUser = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        Long userPoint = selectedUser.getPoint();

        // roomId, userPoint 프론트로 전달
        RoomCreateResDto roomCreateResDto = new RoomCreateResDto();
        roomCreateResDto.setRoomId(roomId);
        roomCreateResDto.setUserPoint(userPoint);

        return roomCreateResDto;
    }

    public RoomJoinResDto checkRoomStatus(String roomId) {
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        String userTwoId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user2_id");

        log.info("userOneId : " + userOneId);
        log.info("userTwoId : " + userTwoId);
        String userOneName = findUserName(userOneId);
        String userTwoName = findUserName(userTwoId);

        RoomJoinResDto roomJoinResDto = new RoomJoinResDto();
        roomJoinResDto.setUser1(userOneName);
        roomJoinResDto.setUser2(userTwoName);
        roomJoinResDto.setBetPoint((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "bet_point"));
        roomJoinResDto.setTimeLimit((Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "time_limit"));
        return roomJoinResDto;
    }

    public RoomUpdateResDto updateRoom(String roomId, String userId, Long betPoint, Long timeLimit) {
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        boolean isUpdateAvailable = !userId.equals(userOneId);

        User userOne = userRepository.findByUserId(userOneId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User userTwo = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (userOne.getPoint() < betPoint || userTwo.getPoint() < betPoint) {
            isUpdateAvailable = false;
        }

        if (!isUpdateAvailable) {
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
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        String userTwoId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user2_id");
        if ( !userId.equals(userOneId) && !userId.equals(userTwoId) ) {
            redisTemplate.opsForHash().put("game_rooms:" + roomId, "user2_id", userId);
        }
        return checkRoomStatus(roomId);
    }

    public GameStartResDto startGame(String roomId) {
        String userOneId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user1_id");
        User userOne = userRepository.findByUserId(userOneId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userOnePoint = userOne.getPoint();

        String userTwoId = (String) redisTemplate.opsForHash().get("game_rooms:" + roomId, "user2_id");
        User userTwo = userRepository.findByUserId(userTwoId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userTwoPoint = userTwo.getPoint();

        Long betPoint = (Long) redisTemplate.opsForHash().get("game_rooms:" + roomId, "bet_point");

        if (userOnePoint < betPoint || userTwoPoint < betPoint) {
            throw new IllegalArgumentException("Not enough bet point");
        }

        redisTemplate.opsForHash().put("game_rooms:" + roomId, "game_status", "game_ready");
        redisTemplate.opsForHash().put("game_rooms:" + roomId, "user_entered", 0);

        GameStartResDto gameStartResDto = new GameStartResDto();
        gameStartResDto.setRoomId(roomId);
        return gameStartResDto;
    }

    private String findUserName(String userId){
        User selectedUser = userRepository.findByUserId(userId).orElse(null);
        if (selectedUser == null) {return "-";}
        return selectedUser.getNickname();
    }

    public void createRoomForTest(){

        redisTemplate.delete("game_rooms:" + 111111);
        redisTemplate.delete("point_boxes:" + 111111);
        redisTemplate.delete("dopamine_boxes:" + 111111);
        redisTemplate.delete("player_positions:" + 111111);
        redisTemplate.delete("player_points:" + 111111);

        redisTemplate.opsForHash().put("game_rooms:" + 111111, "game_status", "game_ready");
        redisTemplate.opsForHash().put("game_rooms:" + 111111, "bet_point", DEFAULT_BET_POINT);
        redisTemplate.opsForHash().put("game_rooms:" + 111111, "time_limit", DEFAULT_TIME_LIMIT);
        redisTemplate.opsForHash().put("game_rooms:" + 111111, "user1_id", "kakao_3641722702");
        redisTemplate.opsForHash().put("game_rooms:" + 111111, "user2_id", "kakao_3641686562");
        redisTemplate.opsForHash().put("game_rooms:" + 111111,  "user_entered", 0);
    }
}

package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.JoinRoomRequest;
import com.huy.quizme_backend.dto.request.RoomRequest;
import com.huy.quizme_backend.dto.response.RoomResponse;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.RoomParticipant;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.QuizRepository;
import com.huy.quizme_backend.repository.RoomParticipantRepository;
import com.huy.quizme_backend.repository.RoomRepository;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final LocalStorageService localStorageService;

    /**
     * Tạo mã phòng ngẫu nhiên
     *
     * @return Mã phòng duy nhất
     */
    private String generateUniqueRoomCode() {
        Random random = new Random();
        String code;
        do {
            // Tạo mã 6 ký tự alphanumeric
            code = random.ints(48, 123)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(6)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString()
                    .toUpperCase();
        } while (roomRepository.findByCode(code).isPresent());

        return code;
    }

    /**
     * Tạo phòng mới
     *
     * @param roomRequest Thông tin phòng
     * @param hostId      ID của người tạo phòng
     * @return Thông tin phòng đã tạo
     */
    @Transactional
    public RoomResponse createRoom(RoomRequest roomRequest, Long hostId) {
        // Tìm người tạo phòng
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + hostId));

        // Tìm quiz
        Quiz quiz = quizRepository.findById(roomRequest.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found with id: " + roomRequest.getQuizId()));

        // Tạo mã phòng ngẫu nhiên
        String roomCode = generateUniqueRoomCode();

        // Tạo phòng mới
        Room room = Room.builder()
                .name(roomRequest.getName())
                .code(roomCode)
                .quiz(quiz)
                .host(host)
                .maxPlayers(roomRequest.getMaxPlayers())
                .status(Room.Status.waiting)
                .build();

        // Lưu phòng vào database
        Room savedRoom = roomRepository.save(room);

        // Thêm người tạo phòng vào danh sách người tham gia
        RoomParticipant hostParticipant = RoomParticipant.builder()
                .room(savedRoom)
                .user(host)
                .isHost(true)
                .build();

        participantRepository.save(hostParticipant);

        // Trả về thông tin phòng
        return RoomResponse.fromRoom(savedRoom, localStorageService);
    }

    /**
     * Tham gia phòng
     *
     * @param joinRequest Yêu cầu tham gia phòng
     * @param userId      ID của người dùng (có thể null nếu là khách)
     * @return Thông tin phòng
     */
    @Transactional
    public RoomResponse joinRoom(JoinRoomRequest joinRequest, Long userId) {
        // Tìm phòng theo mã
        Room room = roomRepository.findByCode(joinRequest.getRoomCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with code: " + joinRequest.getRoomCode()));

        // Kiểm tra xem phòng còn chỗ không
        if (participantRepository.findByRoom(room).size() >= room.getMaxPlayers()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is full");
        }

        // Kiểm tra trạng thái phòng
        if (room.getStatus() != Room.Status.waiting) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is not accepting new participants");
        }

        // Tạo participant dựa trên user hoặc guest
        RoomParticipant participant;

        if (userId != null) {
            // Người dùng đã đăng nhập
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found with id: " + userId));

            // Kiểm tra xem người dùng đã tham gia phòng này chưa
            if (participantRepository.findByRoomAndUser(room, user).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined this room");
            }

            participant = RoomParticipant.builder()
                    .room(room)
                    .user(user)
                    .isHost(false)
                    .build();
        } else {
            // Người dùng là khách
            if (joinRequest.getGuestName() == null || joinRequest.getGuestName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name is required");
            }

            // Kiểm tra xem tên khách đã được sử dụng trong phòng này chưa
            if (participantRepository.findByRoomAndGuestName(room, joinRequest.getGuestName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Guest name already used in this room");
            }

            participant = RoomParticipant.builder()
                    .room(room)
                    .isGuest(true)
                    .guestName(joinRequest.getGuestName())
                    .isHost(false)
                    .build();
        }

        // Lưu participant
        participantRepository.save(participant);

        // Tải lại thông tin phòng để đảm bảo dữ liệu mới nhất
        Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();

        return RoomResponse.fromRoom(updatedRoom, localStorageService);
    }

    /**
     * Lấy thông tin phòng theo mã
     *
     * @param roomCode Mã phòng
     * @return Thông tin phòng
     */
    public RoomResponse getRoomByCode(String roomCode) {
        Room room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with code: " + roomCode));

        return RoomResponse.fromRoom(room, localStorageService);
    }

    /**
     * Lấy danh sách phòng theo trạng thái
     *
     * @param status Trạng thái phòng
     * @return Danh sách phòng
     */
    public List<RoomResponse> getRoomsByStatus(Room.Status status) {
        List<Room> rooms = roomRepository.findByStatus(status);

        return rooms.stream()
                .map(room -> RoomResponse.fromRoom(room, localStorageService))
                .collect(Collectors.toList());
    }

    // Thêm các phương thức khác như startRoom, endRoom, leaveRoom, etc.
}
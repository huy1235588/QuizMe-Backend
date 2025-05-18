package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.JoinRoomRequest;
import com.huy.quizme_backend.dto.request.RoomRequest;
import com.huy.quizme_backend.dto.event.PlayerEventResponse;
import com.huy.quizme_backend.dto.response.RoomResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.RoomParticipant;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.enity.enums.RoomStatus;
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
    private final WebSocketService webSocketService;
    private PlayerEventResponse eventResponse;

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
                .password(roomRequest.getPassword())
                .isPublic(roomRequest.getIsPublic())
                .maxPlayers(roomRequest.getMaxPlayers())
                .status(RoomStatus.WAITING)
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

        // Kiểm tra xem phòng có yêu cầu mật khẩu không
        if (room.getPassword() != null && !room.getPassword().isEmpty()) {
            if (joinRequest.getPassword() == null || joinRequest.getPassword().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room password is required");
            }
            if (!room.getPassword().equals(joinRequest.getPassword())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid room password");
            }
        }

        // Kiểm tra xem phòng còn chỗ không
        if (participantRepository.findByRoom(room).size() >= room.getMaxPlayers()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is full");
        }

        // Kiểm tra trạng thái phòng
        if (room.getStatus() != RoomStatus.WAITING) {
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
        RoomParticipant savedParticipant = participantRepository.save(participant);

        // Tải lại thông tin phòng để đảm bảo dữ liệu mới nhất
        Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();

        // Thông báo cho người khác trong phòng về việc người chơi mới tham gia
        String playerName = savedParticipant.isGuest()
                ? savedParticipant.getGuestName()
                : savedParticipant.getUser().getUsername();
        webSocketService.sendPlayerJoinEvent(room.getId(), "Player " + playerName + " joined the room");

        return RoomResponse.fromRoom(updatedRoom, localStorageService);
    }

    /**
     * Tham gia phòng theo ID
     *
     * @param roomId    ID của phòng
     * @param userId    ID của người dùng (có thể null nếu là khách)
     * @param guestName Tên khách (bắt buộc nếu userId là null)
     * @param password  Mật khẩu phòng (nếu có)
     * @return Thông tin phòng
     */
    @Transactional
    public RoomResponse joinRoomById(
            Long roomId,
            Long userId,
            String guestName,
            String password
    ) {
        // Tìm phòng theo ID
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Kiểm tra xem phòng có yêu cầu mật khẩu không
        if (room.getPassword() != null && !room.getPassword().isEmpty()) {
            if (password == null || password.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room password is required");
            }
            if (!room.getPassword().equals(password)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid room password");
            }
        }

        // Kiểm tra xem phòng còn chỗ không
        if (participantRepository.countByRoom(room) >= room.getMaxPlayers()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is full");
        }

        // Kiểm tra trạng thái phòng
        if (room.getStatus() != RoomStatus.WAITING) {
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
            if (guestName == null || guestName.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name is required");
            }

            // Kiểm tra xem tên khách đã được sử dụng trong phòng này chưa
            if (participantRepository.findByRoomAndGuestName(room, guestName).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Guest name already used in this room");
            }

            participant = RoomParticipant.builder()
                    .room(room)
                    .isGuest(true)
                    .guestName(guestName)
                    .isHost(false)
                    .build();
        }

        // Lưu participant
        RoomParticipant savedParticipant = participantRepository.save(participant);

        // Tải lại thông tin phòng để đảm bảo dữ liệu mới nhất
        Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();

        // Thông báo cho người khác trong phòng về việc người chơi mới tham gia
        String playerName = savedParticipant.isGuest()
                ? savedParticipant.getGuestName()
                : savedParticipant.getUser().getUsername();

        // Tạo thông báo cho người chơi tham gia
        String message = "Player " + playerName + " joined the room";

        // Tạo thông tin người chơi
        UserResponse userResponse = UserResponse.fromUser(
                savedParticipant.getUser(),
                localStorageService
        );

        // Tạo sự kiện người chơi tham gia phòng
        eventResponse = PlayerEventResponse.fromUser(
                userResponse,
                message,
                "join"
        );

        // Gửi thông báo cho tất cả người trong phòng
        webSocketService.sendPlayerJoinEvent(room.getId(), eventResponse);

        return RoomResponse.fromRoom(updatedRoom, localStorageService);
    }

    /**
     * Rời phòng
     *
     * @param roomId    ID của phòng
     * @param userId    ID của người dùng (có thể null nếu là khách)
     * @param guestName Tên khách (bắt buộc nếu userId là null)
     * @return Thông báo thành công
     */
    @Transactional
    public String leaveRoom(Long roomId, Long userId, String guestName) {
        // Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        RoomParticipant participant;
        String playerName = "";

        if (userId != null) {
            // Người dùng đã đăng nhập
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found with id: " + userId));

            participant = participantRepository.findByRoomAndUser(room, user)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User is not in this room"));

            // Người dùng không thể rời phòng nếu họ là host
            if (participant.isHost()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Host cannot leave the room. Close the room instead.");
            }

            playerName = user.getUsername();
        } else {
            // Người dùng là khách
            if (guestName == null || guestName.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name is required");
            }

            participant = participantRepository.findByRoomAndGuestName(room, guestName)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Guest is not in this room"));

            playerName = guestName;
        }

        // Xóa participant khỏi phòng
        participantRepository.delete(participant);

        // Thông báo cho người khác trong phòng về việc người chơi rời đi
        String message = "Player " + playerName + " left the room";

        // Tạo thông tin người chơi
        UserResponse userResponse = UserResponse.fromUser(
                participant.getUser(),
                localStorageService
        );

        // Tạo sự kiện người chơi rời phòng
        eventResponse = PlayerEventResponse.fromUser(
                userResponse,
                message,
                "leave"
        );

        // Gửi thông báo cho tất cả người trong phòng
        webSocketService.sendPlayerLeaveEvent(room.getId(), eventResponse);

        return "Left room successfully";
    }

    /**
     * Đóng phòng (chỉ host mới có quyền)
     *
     * @param roomId ID của phòng
     * @param userId ID của người dùng (phải là host)
     * @return Thông tin phòng đã đóng
     */
    @Transactional
    public RoomResponse closeRoom(Long roomId, Long userId) {
        // Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Tìm người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // Kiểm tra xem người dùng có phải là host không
        RoomParticipant hostParticipant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User is not in this room"));

        if (!hostParticipant.isHost()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only host can close the room");
        }

        // Kiểm tra trạng thái phòng
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can only close rooms in waiting status");
        }

        // Đổi trạng thái phòng thành cancelled
        room.setStatus(RoomStatus.CANCELLED);
        room = roomRepository.save(room);

        // Thông báo cho tất cả người trong phòng
        webSocketService.sendRoomEvent(roomId, "close", "Room has been closed by the host");

        return RoomResponse.fromRoom(room, localStorageService);
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
    public List<RoomResponse> getRoomsByStatus(RoomStatus status) {
        List<Room> rooms = roomRepository.findByStatus(status);

        return rooms.stream()
                .map(room -> RoomResponse.fromRoom(room, localStorageService))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách phòng có thể tham gia
     *
     * @param categoryId ID của danh mục (có thể null)
     * @param search     Từ khóa tìm kiếm (có thể null)
     * @return Danh sách phòng
     */
    public List<RoomResponse> getAvailableRooms(Long categoryId, String search) {
        // Chỉ lấy phòng đang chờ
        List<Room> rooms = roomRepository.findByStatusAndIsPublic(RoomStatus.WAITING, true);

        // Lọc theo danh mục nếu có
        if (categoryId != null) {
            rooms = rooms.stream()
                    .filter(room -> room.getQuiz().getCategories().stream()
                            .anyMatch(category -> category.getId().equals(categoryId)))
                    .collect(Collectors.toList());
        }

        // Lọc theo tìm kiếm nếu có
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            rooms = rooms.stream()
                    .filter(room ->
                            (room.getName() != null && room.getName().toLowerCase().contains(searchLower)) ||
                                    room.getQuiz().getTitle().toLowerCase().contains(searchLower) ||
                                    room.getHost().getUsername().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // Chuyển đổi sang RoomResponse
        return rooms.stream()
                .map(room -> {
                    RoomResponse response = RoomResponse.fromRoom(room, localStorageService);
                    // Thêm số người chơi hiện tại
                    response.setCurrentPlayerCount(participantRepository.countByRoom(room));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin phòng
     *
     * @param roomId      ID của phòng
     * @param userId      ID của người dùng (phải là host)
     * @param roomRequest Thông tin cập nhật
     * @return Thông tin phòng đã cập nhật
     */
    @Transactional
    public RoomResponse updateRoom(Long roomId, Long userId, RoomRequest roomRequest) {
        // Tìm phòng theo ID
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Kiểm tra xem người dùng có phải là host không
        if (!room.getHost().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only host can update room settings");
        }

        // Kiểm tra trạng thái phòng
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot update room settings when game is not in waiting state");
        }

        // Cập nhật thông tin phòng
        room.setName(roomRequest.getName());
        room.setQuiz(quizRepository.getReferenceById(roomRequest.getQuizId()));
        room.setPassword(roomRequest.getPassword());
        room.setIsPublic(roomRequest.getIsPublic());

        // Kiểm tra số người tham gia trước khi cập nhật maxPlayers
        int currentPlayerCount = participantRepository.countByRoom(room);
        if (roomRequest.getMaxPlayers() < currentPlayerCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot set max players less than current player count");
        }
        room.setMaxPlayers(roomRequest.getMaxPlayers());

        // Lưu phòng vào database
        Room updatedRoom = roomRepository.save(room);

        // Trả về thông tin phòng đã cập nhật
        return RoomResponse.fromRoom(updatedRoom, localStorageService);
    }

    /**
     * Bắt đầu trò chơi (chỉ host mới có quyền)
     *
     * @param roomId ID của phòng
     * @param userId ID của người dùng (phải là host)
     * @return Thông tin phòng đã bắt đầu
     */
    @Transactional
    public RoomResponse startGame(Long roomId, Long userId) {
        // Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Kiểm tra xem người dùng có phải là host không
        if (!room.getHost().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only host can start the game");
        }

        // Kiểm tra trạng thái phòng
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Game can only be started from waiting state");
        }        // Đổi trạng thái phòng thành playing
        room.setStatus(RoomStatus.IN_PROGRESS);
        room = roomRepository.save(room);

        // Gửi thông báo qua WebSocket
        webSocketService.sendGameStartEvent(roomId, "Game started");

        // Trả về thông tin phòng đã cập nhật
        return RoomResponse.fromRoom(room, localStorageService);
    }

    /**
     * Xử lý timeout khi người dùng đã đăng nhập mất kết nối
     *
     * @param roomId Room ID
     * @param userId User ID
     */
    @Transactional
    public void handleUserDisconnectTimeout(Long roomId, Long userId) {
        // Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Tìm người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // Tìm thông tin tham gia phòng
        RoomParticipant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User is not in this room"));

        // Kiểm tra nếu người dùng là host
        if (participant.isHost()) {
            // Nếu là host, đóng phòng
            closeRoomDueToHostDisconnect(room);
        } else {
            // Nếu không phải host, chỉ xóa participant
            handleParticipantTimeout(participant, user.getUsername(), false);
        }
    }

    /**
     * Xử lý timeout khi khách mất kết nối
     *
     * @param roomId    Room ID
     * @param guestName Guest name
     */
    @Transactional
    public void handleGuestDisconnectTimeout(Long roomId, String guestName) {
        // Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Tìm thông tin tham gia phòng
        RoomParticipant participant = participantRepository.findByRoomAndGuestName(room, guestName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Guest is not in this room"));

        // Xóa participant (khách không bao giờ là host)
        handleParticipantTimeout(participant, guestName, true);
    }

    /**
     * Xử lý chung khi một người tham gia bị timeout
     *
     * @param participant Thông tin tham gia
     * @param playerName  Tên hiển thị
     * @param isGuest     Là khách hay đã đăng nhập
     */
    private void handleParticipantTimeout(RoomParticipant participant, String playerName, boolean isGuest) {
        // Xóa participant
        Long roomId = participant.getRoom().getId();
        participantRepository.delete(participant);

        // Thông báo cho người khác trong phòng
        String message = "Player " + playerName + " left the room";

        // Tạo thông tin người chơi
        UserResponse userResponse = UserResponse.fromUser(
                participant.getUser(),
                localStorageService
        );

        eventResponse = PlayerEventResponse.fromUser(
                userResponse,
                message,
                "leave"
        );

        // Gửi thông báo cho những người còn lại trong phòng
        webSocketService.sendPlayerLeaveEvent(roomId, eventResponse);
    }

    /**
     * Đóng phòng khi host ngắt kết nối
     */
    private void closeRoomDueToHostDisconnect(Room room) {
        // Đổi trạng thái phòng thành cancelled
        room.setStatus(RoomStatus.CANCELLED);
        roomRepository.save(room);

        // Thông báo cho tất cả người trong phòng
        webSocketService.sendRoomEvent(room.getId(), "close", "Room has been closed because the host was disconnected");
    }
}
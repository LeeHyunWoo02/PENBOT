package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.ChatAPI.Converter.ChatLogConverter;
import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Entity.ChatLog;
import Project.PENBOT.ChatAPI.Respository.ChatLogRepository;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Slf4j
@Service
public class ChatLogService {
    private final ChatLogRepository chatLogRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ChatLogService(ChatLogRepository chatLogRepository, BookingRepository bookingRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.chatLogRepository = chatLogRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ChatLog saveUserChat(String auth, QueryRequestDTO requestDTO) {
        int userId = getUserId(auth);
        try{
            User user = userRepository.findById(userId);
            ChatLog chatlog = ChatLogConverter.UsertoEntity(requestDTO, user);
            user.addChatLog(chatlog);
            return chatLogRepository.save(chatlog);
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }

    @Transactional
    public ChatLog saveBotChat(String auth, String answer){
        int userId = getUserId(auth);
        try{
            User user = userRepository.findById(userId);
            ChatLog chatlog = ChatLogConverter.BottoEntity(answer, user);
            user.addChatLog(chatlog);
            return chatLogRepository.save(chatlog);
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }

    @Transactional
    public void BookingChatSave(int bookingId){
        log.info("BookingChatSave: {}", bookingId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        try{
            if (bookingOptional.isPresent()){
                Booking booking = bookingOptional.get();
                User user = booking.getUser();
                log.info("User : {}", user.getId());
                List<ChatLog> chatLogList = chatLogRepository.findTop3ByUserAndBookingIsNullOrderByDateTime(user);
                for (ChatLog chatLog : chatLogList) {
                    log.info("매칭중");
                    chatLog.setBooking(booking);
                }
            }
        } catch (BookingNotFoundException e){
            throw new BookingNotFoundException();
        }
    }

    private int getUserId(String auth) {
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        return userId;
    }
}

package Project.PENBOT.Booking.Serivce;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,JwtUtil jwtUtil) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Booking createBooking(BookingRequestDTO requestDTO, String auth) {

        int userId = getUserId(auth);

        LocalDate startDate = requestDTO.getStartDate();
        LocalDate endDate = requestDTO.getEndDate();
        boolean isDuplicated = bookingRepository
                .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(startDate, endDate);

        if(isDuplicated) {
            throw new RuntimeException("이미 해당 기간에 예약이 존재합니다.");
        }

        try{
            User user = userRepository.findById(userId);
            Booking booking = BookingConverter.toEntity(requestDTO, user);
            user.addBooking(booking);
            return bookingRepository.save(booking);
        } catch (NullPointerException e) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
    }

    public void isAvailable(BookingRequestDTO requestDTO) {
        LocalDate start = requestDTO.getStartDate();
        LocalDate end = requestDTO.getEndDate();
        // 이미 예약된 데이터 있는지 체크
        boolean isDuplicated = bookingRepository
                .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(start, end);

        if(isDuplicated) {
            throw new RuntimeException("이미 해당 기간에 예약이 존재합니다.");
        }
    }

    public MyBookingResponseDTO getAllMyBooking(String auth){
        int userId = getUserId(auth);
        try{
            User user = userRepository.findById(userId);
            return BookingConverter.toDto(user);
        } catch (NullPointerException e) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
    }

    private int getUserId(String auth) {
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        return userId;
    }
}

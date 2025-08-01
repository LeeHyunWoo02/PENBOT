package Project.PENBOT.Booking.Serivce;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.ForbiddenCreateBookingException;
import Project.PENBOT.CustomException.ForbiddenException;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.Host.Repository.BlockedDateRepository;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public BookingService(BookingRepository bookingRepository, BlockedDateRepository blockedDateRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.bookingRepository = bookingRepository;
        this.blockedDateRepository = blockedDateRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO requestDTO, String auth) {

        int userId = getUserId(auth);

        if (!isAvailable(requestDTO)) {
            throw new ForbiddenCreateBookingException("예약이 불가능한 날짜입니다.");
        }

        User user = userRepository.findById(userId);
        if( user == null) {
            throw new UserNotFoundException();
        }

        Booking booking = BookingConverter.toEntity(requestDTO, user);
        user.addBooking(booking);

        return bookingRepository.save(booking);
    }

    public boolean isAvailable(BookingRequestDTO requestDTO) {
        LocalDate start = requestDTO.getStartDate();
        LocalDate end = requestDTO.getEndDate();

        boolean isBooked = bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start);
        boolean isBlocked = blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start); // BlockedDate 존재 여부

        return !(isBooked || isBlocked);

    }

    public MyBookingResponseDTO getAllMyBooking(String auth){
        int userId = getUserId(auth);
        try{
            User user = userRepository.findById(userId);
            return BookingConverter.toAllDto(user);
        } catch (NullPointerException e) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
    }

    public MyBookingResponseDTO getMyBooking(String auth, int bookingId) {
        int userId = getUserId(auth);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException();
        }

        if (bookingOptional.get().getUser().getId() != userId) {
            throw new ForbiddenException();
        }

        return BookingConverter.toMyDto(bookingOptional.get());
    }

    @Transactional
    public void deleteBooking(String auth, int bookingId){
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if(bookingOptional.isEmpty()) {
            throw new RuntimeException("존재하지 않는 예약입니다.");
        }
        Booking booking = bookingOptional.get();
        if(booking.getUser().getId() != getUserId(auth)) {
            throw new ForbiddenException();
        }

        bookingRepository.delete(booking);
    }

    private int getUserId(String auth) {
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        return userId;
    }
}

package Project.PENBOT.Host.Service;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.CustomException.BlockedDateConflictException;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.Host.Converter.BlockedDateConverter;
import Project.PENBOT.Host.Converter.BookingAllConverter;
import Project.PENBOT.Host.Dto.*;
import Project.PENBOT.Host.Entity.BlockedDate;
import Project.PENBOT.Host.Repository.BlockedDateRepository;
import Project.PENBOT.User.Dto.UserResponseDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostService {

    private final BookingRepository bookingRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final UserRepository userRepository;

    public HostService(BookingRepository bookingRepository, BlockedDateRepository blockedDateRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.blockedDateRepository = blockedDateRepository;
        this.userRepository = userRepository;
    }

    public List<BookingListResponseDTO> getBookingAll(){
        List<Booking> bookings = bookingRepository.findAll();
        if (bookings.isEmpty()) {
            throw new BookingNotFoundException("예약이 존재하지 않습니다.");
        }
        return BookingAllConverter.toDTO(bookings);
    }
    /**
     * 예약 상세 조회
     * */
    public BookingSimpleDTO getBookingInfo(int bookingId) {

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException();
        }

        return BookingConverter.toDTO(bookingOptional.get());
    }

    /**
     * 예약 업데이트
     * 날짜, 인원수 변경
     * 예약 상태 변경 ( 대기 -> 승인 )
     * */
    @Transactional
    public BookingResponseDTO updateBooking(int bookingId, BookingUpdateRequestDTO requestDTO) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);

        Booking.builder()
                .status(requestDTO.getStatus())
                .headcount(requestDTO.getHeadcount())
                .endDate(requestDTO.getEndDate())
                .startDate(requestDTO.getStartDate())
                .build();

        bookingRepository.save(booking);
        return BookingResponseDTO.builder()
                .success(true)
                .message("예약 정보가 성공적으로 업데이트되었습니다.")
                .bookingId(booking.getId())
                .build();
    }

    /**
     * 예약 삭제
     * */
    @Transactional
    public BookingResponseDTO deleteBooking(int bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException();
        }

        bookingRepository.delete(bookingOptional.get());
        return BookingResponseDTO.builder()
                .success(true)
                .message("예약이 성공적으로 삭제되었습니다.")
                .bookingId(bookingId)
                .build();
    }

    @Transactional
    public BlockedDateResponseDTO deleteBlocked(int blockedDateId) {
        Optional<BlockedDate> blockedDateOptional = blockedDateRepository.findById(blockedDateId);
        if (blockedDateOptional.isEmpty()) {
            throw new BookingNotFoundException("차단된 날짜를 찾을 수 없습니다.");
        }

        blockedDateRepository.delete(blockedDateOptional.get());
        return BlockedDateResponseDTO.builder()
                .success(true)
                .message("차단 날짜가 성공적으로 삭제되었습니다.")
                .blockedDateId(blockedDateId)
                .build();
    }

    @Transactional
    public BlockedDateResponseDTO createBlockedDate(BlockDateRequestDTO requestDTO) {

        if (isAvailable(requestDTO.getEndDate(), requestDTO.getStartDate())) {
            log.error("차단 날짜가 이미 예약된 날짜와 겹칩니다: {} ~ {}", requestDTO.getStartDate(), requestDTO.getEndDate());
            throw new BlockedDateConflictException();
        }

        BlockedDate blockedDate = BlockedDateConverter.toEntity(requestDTO);
        blockedDateRepository.save(blockedDate);

        return BlockedDateResponseDTO.builder()
                .success(true)
                .message("차단 날짜가 성공적으로 생성되었습니다.")
                .blockedDateId(blockedDate.getId())
                .build();

    }

    /**
     * 관리자 차단 날짜 모두 조회
     * */
    public List<UnavailableDateDTO> getHostBlockedDates(){
        List<UnavailableDateDTO> unavailableDates = new ArrayList<>();
        List<BlockedDate> blockedDates = blockedDateRepository.findAll();
        SemiBlockDateConverter(blockedDates, unavailableDates);
        return unavailableDates;
    }

    /**
     * 불가능한 날짜 모두 조회
     * */
    public List<UnavailableDateDTO> getUnavailableDates() {
        List<UnavailableDateDTO> unavailableDates = new ArrayList<>();

        // 예약된 날짜 → BOOKED
        List<BookStatus> statuses = Arrays.asList(BookStatus.CONFIRMED, BookStatus.PENDING);
        List<Booking> bookings = bookingRepository.findAllByStatusIn(statuses); // 예약 확정된 것만
        SemiBookingsConverter(bookings, unavailableDates);

        // 차단된 날짜 → BLOCKED
        List<BlockedDate> blockedDates = blockedDateRepository.findAll();
        SemiBlockDateConverter(blockedDates, unavailableDates);

        return unavailableDates;
    }

    @Transactional
    public UserResponseDTO deleteUser(int userId){
        User user = userRepository.findById(userId);
        if(user == null){
            throw new UserNotFoundException();
        }
        userRepository.delete(user);
        return new UserResponseDTO(true, "사용자가 성공적으로 삭제되었습니다.");
    }

    /**
     * 가입한 유저 모두 조회
     * */
    public List<UserListResponseDTO> getAllUsers(){

        List<User> users = userRepository.findAll();
        if(users == null){
            throw new UserNotFoundException();
        }
        return users.stream()
                .map(user -> UserListResponseDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().toString()) // Role을 문자열로 변환
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저 상세 조회
     * */
    public UserDetailResponseDTO getUserDetail(int userId){
        User user = userRepository.findById(userId);
        if(user == null){
            throw new UserNotFoundException();
        }

        return UserDetailResponseDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    public boolean isAvailable(LocalDate startDate, LocalDate endDate){
        boolean isBooked = bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
        boolean isBlocked = blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate); // BlockedDate 존재 여부

        return (isBooked || isBlocked);
    }

    private static void SemiBookingsConverter(List<Booking> bookings, List<UnavailableDateDTO> unavailableDates) {
        for (Booking booking : bookings) {
            unavailableDates.add(UnavailableDateDTO.builder()
                    .startDate(booking.getStartDate())
                    .endDate(booking.getEndDate())
                    .reason(booking.getStatus() == BookStatus.PENDING ? "예약 대기 중" : "예약 확정")
                    .type("BOOKED")
                    .build());
        }
    }

    private static void SemiBlockDateConverter(List<BlockedDate> blockedDates, List<UnavailableDateDTO> unavailableDates) {
        for (BlockedDate blocked : blockedDates) {
            unavailableDates.add(UnavailableDateDTO.builder()
                    .blockedDateId(blocked.getId())
                    .startDate(blocked.getStartDate())
                    .endDate(blocked.getEndDate())
                    .reason(blocked.getReason())
                    .type("BLOCKED")
                    .build());
        }
    }
}

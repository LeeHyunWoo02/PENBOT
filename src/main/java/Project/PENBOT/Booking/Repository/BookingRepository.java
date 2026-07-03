package Project.PENBOT.Booking.Repository;

import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    /**
     *  겹치는 예약이 있는지 조회
     * */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);
    Optional<Booking> findById(int id);
    List<Booking> findAllByStatusIn(List<BookStatus> statuses);

    List<Booking> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate monthEnd, LocalDate monthStart);

    /**
     * 비회원 예약 조회 - 이름/전화번호/비밀번호 일치 여부로 조회
     * */
    List<Booking> findByGuestNameAndGuestPhoneAndPassword(String guestName, String guestPhone, Integer password);

}

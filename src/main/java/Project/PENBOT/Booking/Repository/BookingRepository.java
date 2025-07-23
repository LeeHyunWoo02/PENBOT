package Project.PENBOT.Booking.Repository;

import Project.PENBOT.Booking.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    /**
     *  겹치는 예약이 있는지 조회
     * */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);
    Booking findById(int id);


}

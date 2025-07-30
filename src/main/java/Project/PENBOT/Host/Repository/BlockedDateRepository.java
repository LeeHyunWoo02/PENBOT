package Project.PENBOT.Host.Repository;

import Project.PENBOT.Host.Entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Integer> {
    /**
     *  겹치는 예약이 있는지 조회
     * */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);
}

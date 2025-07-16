package Project.PENBOT.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "bookings_id")
    private Booking booking;

    @Enumerated(EnumType.STRING)
    private PayStatus payStatus;

    @Enumerated(EnumType.STRING)
    private Method method;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

}

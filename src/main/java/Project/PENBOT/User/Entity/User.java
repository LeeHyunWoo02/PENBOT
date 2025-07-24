package Project.PENBOT.User.Entity;

import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.ChatAPI.Entity.ChatLog;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider;

    private String providerId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatLog> chatlogs;

    public void addChatLog(ChatLog chatLog) {
        chatlogs.add(chatLog);
        chatLog.setUser(this);
    }
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setUser(this);
    }

}

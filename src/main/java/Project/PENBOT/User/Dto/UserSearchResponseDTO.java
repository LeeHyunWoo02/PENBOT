package Project.PENBOT.User.Dto;

import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class UserSearchResponseDTO {

    private String name;
    private String phone;
    private String email;
    private HashMap<String, BookingSimpleDTO> myBookings;


}

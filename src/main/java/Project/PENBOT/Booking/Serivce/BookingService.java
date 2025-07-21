package Project.PENBOT.Booking.Serivce;

import Project.PENBOT.Booking.Repository.BookingRepository;

public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    
}

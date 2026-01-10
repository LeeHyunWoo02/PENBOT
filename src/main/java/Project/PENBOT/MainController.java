package Project.PENBOT;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/reservationInfo")
    public String reservationInfo() {
        return "reservationInfo";
    }

    @GetMapping("/roomInfo")
    public String roomInfo() {
        return "explainRoom";
    }

    @GetMapping("/directions")
    public String directions() {
        return "directions";
    }

    @GetMapping("/reserve")
    public String reserve() {
        return "reserve";
    }
}

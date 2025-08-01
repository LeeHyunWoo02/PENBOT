package Project.PENBOT.ChatAPI.Controller;


import Project.PENBOT.ChatAPI.Dto.PlaceInfoDTO;
import Project.PENBOT.ChatAPI.Service.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class GooglePlaceController {

    private final GooglePlacesService googlePlacesService;

    @GetMapping("/search")
    public List<PlaceInfoDTO> searchNearbyPlaces(
            @RequestParam String keyword,
            @RequestParam String type
    ) {
        return googlePlacesService.searchNearby(keyword, type);
    }
}

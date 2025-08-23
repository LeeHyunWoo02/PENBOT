package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Dto.PlaceInfoDTO;
import Project.PENBOT.ChatAPI.Dto.QueryResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GooglePlacesService {

    @Value("${spring.ai.places.api-key}")
    private String apiKey;
    private final ObjectMapper objectMapper;


    // 대부도 라온아띠 펜션 위도/경도
    private static final double LATITUDE = 37.207361;
    private static final double LONGITUDE = 126.568450;
    private static final int RADIUS = 15000;

    public GooglePlacesService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<PlaceInfoDTO> searchNearby(String type) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + LATITUDE + "," + LONGITUDE +
                "&radius=" + RADIUS +
                "&type=" + type +
                "&language=ko" +
                "&key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<PlaceInfoDTO> result = new ArrayList<>();
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode results = root.get("results");
                if (results != null) {
                    for (int i = 0; i < Math.min(results.size(), 5); i++) { // 최대 5개만 추출
                        JsonNode item = results.get(i);
                        String name = item.path("name").asText();
                        double rating = item.has("rating") ? item.path("rating").asDouble() : 0.0;
                        String address = item.path("vicinity").asText();
                        result.add(new PlaceInfoDTO(name, rating, address));
                    }
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public Optional<QueryResponseDTO> findPlaceAddressByText() {
        return Optional.of(new QueryResponseDTO("안산시 단원구 멍골 길 10-1"));
    }

}

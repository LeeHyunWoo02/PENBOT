package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Dto.PlaceInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GooglePlacesService {

    @Value("${google.places.api.key}")
    private String apiKey;
    private final ObjectMapper objectMapper;


    // 대부도 라온아띠 펜션 위도/경도
    private static final double LATITUDE = 37.207361;
    private static final double LONGITUDE = 126.568450;
    private static final int RADIUS = 10000;

    public GooglePlacesService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<PlaceInfoDTO> searchNearby(String keyword, String type) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + LATITUDE + "," + LONGITUDE +
                "&radius=" + RADIUS +
                "&type=" + type +
                "&keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8) +
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
                System.out.println("Google Places API Response: " + response.getBody());
                System.out.println(result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Google Places API Response: " + response.getBody());
        System.out.println(result);
        return result;
    }

}

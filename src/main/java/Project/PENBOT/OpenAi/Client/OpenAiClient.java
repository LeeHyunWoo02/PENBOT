package Project.PENBOT.OpenAi.Client;



import Project.PENBOT.OpenAi.Dto.OpenAiRequest;
import Project.PENBOT.OpenAi.Dto.OpenAiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class OpenAiClient {
    private final RestTemplate restTemplate;
    private final String APIKEY;
    private final String APIURL;

    public OpenAiClient(RestTemplate restTemplate, @Value("${openai.api-key}") String apiKey,
                        @Value("${openai.api-url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.APIURL = apiUrl;
        this.APIKEY = apiKey;
    }
    /**
     * 사용자 질문을 GPT 모델에 전달하고 응답 받기.
     * */
    public OpenAiResponse getChatCompletion(OpenAiRequest requestDto) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(APIKEY);

        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<OpenAiResponse> res = restTemplate.postForEntity(
                APIURL, entity, OpenAiResponse.class);
//        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
//            throw new JsonProcessingException();
//        }
        return res.getBody();
    }

}

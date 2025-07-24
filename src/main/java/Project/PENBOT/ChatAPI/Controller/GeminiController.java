package Project.PENBOT.ChatAPI.Controller;
import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Dto.QueryResponseDTO;
import Project.PENBOT.ChatAPI.Service.ChatLogService;
import Project.PENBOT.ChatAPI.Service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final ChatLogService chatLogService;

    public GeminiController(GeminiService geminiService, ChatLogService chatLogService) {
        this.geminiService = geminiService;
        this.chatLogService = chatLogService;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponseDTO> askGemini(@RequestBody QueryRequestDTO requestDTO,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        // ChatLog에 질문 저장
        chatLogService.saveUserChat(auth, requestDTO);
        // Gemini API 호출
        String answer = geminiService.getCompletion(requestDTO.getText());
        // ChatLog에 답변 저장
        chatLogService.saveBotChat(auth, answer);
        return ResponseEntity.ok(new QueryResponseDTO(answer));
    }
}

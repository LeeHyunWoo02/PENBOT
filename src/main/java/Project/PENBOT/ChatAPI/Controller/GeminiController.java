package Project.PENBOT.ChatAPI.Controller;
import Project.PENBOT.ChatAPI.Dto.ChatMessageDTO;
import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Dto.QueryResponseDTO;
import Project.PENBOT.ChatAPI.Entity.Sender;
import Project.PENBOT.ChatAPI.Service.ChatLogService;
import Project.PENBOT.ChatAPI.Service.GeminiService;
import Project.PENBOT.ChatAPI.Service.RedisChatService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final ChatLogService chatLogService;
    private final RedisChatService redisChatService;

    public GeminiController(GeminiService geminiService, ChatLogService chatLogService, RedisChatService redisChatService) {
        this.geminiService = geminiService;
        this.chatLogService = chatLogService;
        this.redisChatService = redisChatService;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponseDTO> askGemini(@RequestBody QueryRequestDTO requestDTO,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        // ChatLog에 질문 저장 && Redis에 질문 저장
        chatLogService.saveUserChat(auth, requestDTO);
        ChatMessageDTO chatMessageDTO= new ChatMessageDTO(Sender.USER, requestDTO.getText());
        redisChatService.addChatMessage(auth, chatMessageDTO);

        // Gemini API 호출
        String answer = geminiService.getCompletion(requestDTO.getText());

        // ChatLog에 답변 저장 && Redis에 답변 저장
        chatLogService.saveBotChat(auth, answer);
        ChatMessageDTO chatMessageDTO2 = new ChatMessageDTO(Sender.BOT, answer);
        redisChatService.addChatMessage(auth, chatMessageDTO2);
        return ResponseEntity.ok(new QueryResponseDTO(answer));
    }
}

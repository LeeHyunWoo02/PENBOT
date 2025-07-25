package Project.PENBOT.ChatAPI.Controller;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.ChatAPI.Dto.ChatMessageDTO;
import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Dto.QueryResponseDTO;
import Project.PENBOT.ChatAPI.Entity.Role;
import Project.PENBOT.ChatAPI.Service.ChatLogService;
import Project.PENBOT.ChatAPI.Service.GeminiService;
import Project.PENBOT.ChatAPI.Service.RedisChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final ChatLogService chatLogService;
    private final BookingService bookingService;
    private final RedisChatService redisChatService;
    private final ObjectMapper objectMapper;

    public GeminiController(GeminiService geminiService, ChatLogService chatLogService, BookingService bookingService, RedisChatService redisChatService, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.chatLogService = chatLogService;
        this.bookingService = bookingService;
        this.redisChatService = redisChatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponseDTO> askGemini(@RequestBody QueryRequestDTO requestDTO,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        // ChatLog에 질문 저장 && Redis에 질문 저장
        chatLogService.saveUserChat(auth, requestDTO);
        ChatMessageDTO chatMessageDTO= new ChatMessageDTO(Role.USER, requestDTO.getText());
        redisChatService.addChatMessage(auth, chatMessageDTO);

        // Gemini API 호출
        String answer = geminiService.getCompletion(requestDTO.getText(), auth);

        // JSON 추출 & BookingRequestDTO 파싱
        Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]+?\\}");
        Matcher matcher = jsonPattern.matcher(answer);
        String json = null;
        if (matcher.find()) {
            json = matcher.group();
        }

        BookingRequestDTO bookingRequestDTO = null;
        if(json != null){
            try{
                bookingRequestDTO = objectMapper.readValue(json, BookingRequestDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // ChatLog에 답변 저장 && Redis에 답변 저장
        chatLogService.saveBotChat(auth, answer);
        ChatMessageDTO chatMessageDTO2 = new ChatMessageDTO(Role.MODEL, answer);
        redisChatService.addChatMessage(auth, chatMessageDTO2);

        // 예약 API 호출 ( 필요한 정보가 다 있으면 )
        if (bookingRequestDTO != null
                && bookingRequestDTO.getStartDate() != null
                && bookingRequestDTO.getEndDate() != null
                && bookingRequestDTO.getHeadcount() > 0) {
            // 예약 생성
            bookingService.createBooking(bookingRequestDTO, auth);

            String successMsg =  String.format(
                    "예약이 완료되었습니다! (%s ~ %s, %d명)",
                    bookingRequestDTO.getStartDate(),
                    bookingRequestDTO.getEndDate(),
                    bookingRequestDTO.getHeadcount()
            );

            chatLogService.saveBotChat(auth, successMsg);
            redisChatService.addChatMessage(auth, new ChatMessageDTO(Role.MODEL, successMsg));
            return ResponseEntity.ok(new QueryResponseDTO(successMsg));
        }

        return ResponseEntity.ok(new QueryResponseDTO(answer));
    }
}

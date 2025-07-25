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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
        String responseMsg = answer;

        // 1. 예약 가능 질문(인원 없는 단순 날짜 문의)
        if (bookingRequestDTO != null
                && bookingRequestDTO.getStartDate() != null
                && bookingRequestDTO.getEndDate() != null
                && bookingRequestDTO.getHeadcount() == 0) {
            log.info("예약 가능 여부 확인 요청: {}", bookingRequestDTO.getStartDate());
            boolean isAvailable = bookingService.isAvailable(bookingRequestDTO);
            log.info("예약 가능 여부: {}", isAvailable);
            responseMsg = isAvailable
                    ? String.format("요청하신 기간(%s ~ %s)은 예약이 가능합니다!",
                    bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate())
                    : String.format("죄송합니다. 요청하신 기간(%s ~ %s)은 이미 예약이 되어 있습니다.",
                    bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
        }
        // 2. 실제 예약(인원까지 모두 채워진 경우)
        else if (bookingRequestDTO != null
                && bookingRequestDTO.getStartDate() != null
                && bookingRequestDTO.getEndDate() != null
                && bookingRequestDTO.getHeadcount() > 0) {
            log.info("예약 요청: {} ~ {}, 인원: {}",
                    bookingRequestDTO.getStartDate(),
                    bookingRequestDTO.getEndDate(),
                    bookingRequestDTO.getHeadcount());
            if(!bookingService.isAvailable(bookingRequestDTO)) {
                responseMsg = String.format("죄송합니다. 요청하신 기간(%s ~ %s)은 이미 예약이 되어 있습니다.",
                        bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                return ResponseEntity.badRequest().body(new QueryResponseDTO(responseMsg));
            } else {
                log.info("예약 가능, 예약 진행 중...");
            }
            bookingService.createBooking(bookingRequestDTO, auth);

            responseMsg = String.format(
                    "예약이 완료되었습니다! (%s ~ %s, %d명)",
                    bookingRequestDTO.getStartDate(),
                    bookingRequestDTO.getEndDate(),
                    bookingRequestDTO.getHeadcount());
        }

        // 답변/로그 저장(공통)
        chatLogService.saveBotChat(auth, responseMsg);
        redisChatService.addChatMessage(auth, new ChatMessageDTO(Role.MODEL, responseMsg));
        return ResponseEntity.ok(new QueryResponseDTO(responseMsg));

    }
}

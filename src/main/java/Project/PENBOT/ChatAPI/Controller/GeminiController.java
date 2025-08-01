package Project.PENBOT.ChatAPI.Controller;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.ChatAPI.Dto.ChatMessageDTO;
import Project.PENBOT.ChatAPI.Dto.PlaceInfoDTO;
import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Dto.QueryResponseDTO;
import Project.PENBOT.ChatAPI.Entity.ChatRole;
import Project.PENBOT.ChatAPI.Service.ChatLogService;
import Project.PENBOT.ChatAPI.Service.GeminiService;
import Project.PENBOT.ChatAPI.Service.GooglePlacesService;
import Project.PENBOT.ChatAPI.Service.RedisChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final ChatLogService chatLogService;
    private final BookingService bookingService;
    private final GooglePlacesService googlePlacesService;
    private final RedisChatService redisChatService;
    private final ObjectMapper objectMapper;

    public GeminiController(GeminiService geminiService, ChatLogService chatLogService, BookingService bookingService, GooglePlacesService googlePlacesService, RedisChatService redisChatService, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.chatLogService = chatLogService;
        this.bookingService = bookingService;
        this.googlePlacesService = googlePlacesService;
        this.redisChatService = redisChatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponseDTO> askGemini(@RequestBody QueryRequestDTO requestDTO,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        // ChatLog에 질문 저장 && Redis에 질문 저장
        chatLogService.saveUserChat(auth, requestDTO);
        ChatMessageDTO chatMessageDTO= new ChatMessageDTO(ChatRole.USER, requestDTO.getText());
        redisChatService.addChatMessage(auth, chatMessageDTO);

        String userText = requestDTO.getText();
        String lower = userText.toLowerCase();
        if (lower.contains("맛집") || lower.contains("카페")) {
            // type은 google api docs 참고 (restaurant, cafe 등)
            String keyword = lower.contains("카페") ? "카페" : "맛집";
            String type = lower.contains("카페") ? "cafe" : "restaurant";
            List<PlaceInfoDTO> places = googlePlacesService.searchNearby(keyword, type);

            // 결과가 없으면 안내, 있으면 Top 5 안내
            String resultMsg = formatPlaceResult(places, keyword);

            // 답변 저장/반환
            chatLogService.saveBotChat(auth, resultMsg);
            ChatMessageDTO chatMessageDTO2 = new ChatMessageDTO(ChatRole.MODEL, resultMsg);
            redisChatService.addChatMessage(auth, chatMessageDTO2);

            return ResponseEntity.ok(new QueryResponseDTO(resultMsg));
        }


        // Gemini API 호출
        String answer = geminiService.getCompletion(requestDTO.getText(), auth);

        // JSON 추출 & BookingRequestDTO 파싱
        Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]+?\\}");
        Matcher matcher = jsonPattern.matcher(answer);
        String json = null;
        if (matcher.find()) {
            json = matcher.group();
            log.info("JSON 추출 성공: {}", json);
        }

        BookingRequestDTO bookingRequestDTO = null;
        if(json != null){
            try{
                bookingRequestDTO = objectMapper.readValue(json, BookingRequestDTO.class);
            } catch (JsonProcessingException e) {
                // 예약 관련이 아닌 경우 FAQ 등 자연어만 응답하도록!
                bookingRequestDTO = null;
            }
        }

        // ChatLog에 답변 저장 && Redis에 답변 저장
        chatLogService.saveBotChat(auth, answer);
        ChatMessageDTO chatMessageDTO2 = new ChatMessageDTO(ChatRole.MODEL, answer);
        redisChatService.addChatMessage(auth, chatMessageDTO2);
        String responseMsg = answer;

        // 예약 관련(JSON 추출 성공) → 예약/예약
        if (bookingRequestDTO != null
                && bookingRequestDTO.getStartDate() != null
                && bookingRequestDTO.getEndDate() != null) {

            // 예약 가능만 문의(headcount 없는 경우, 또는 0/빈값)
            if (bookingRequestDTO.getHeadcount() <= 0) {
                if (!bookingService.isAvailable(bookingRequestDTO)) {
                    log.info("예약 불가 기간 요청: {} ~ {}", bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                    responseMsg = String.format("죄송합니다. 요청하신 기간(%s ~ %s)은 이미 예약이 되어 있습니다.",
                            bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                    chatLogService.saveBotChat(auth, responseMsg);
                    redisChatService.addChatMessage(auth, new ChatMessageDTO(ChatRole.MODEL, responseMsg));
                    return ResponseEntity.badRequest().body(new QueryResponseDTO(responseMsg));
                }
                log.info("예약 가능 기간 요청: {} ~ {}", bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                responseMsg = String.format(
                        "요청하신 기간(%s ~ %s)은 예약이 가능합니다. 인원 수를 입력해 주세요.",
                        bookingRequestDTO.getStartDate(),
                        bookingRequestDTO.getEndDate());
            }
            // 실제 예약 요청 (인원까지 모두 입력)
            else {
                if (!bookingService.isAvailable(bookingRequestDTO)) {
                    log.info("예약 불가능 기간 요청: {} ~ {}", bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                    responseMsg = String.format("죄송합니다. 요청하신 기간(%s ~ %s)은 이미 예약이 되어 있습니다.",
                            bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                    chatLogService.saveBotChat(auth, responseMsg);
                    redisChatService.addChatMessage(auth, new ChatMessageDTO(ChatRole.MODEL, responseMsg));
                    return ResponseEntity.badRequest().body(new QueryResponseDTO(responseMsg));
                }
                bookingService.createBooking(bookingRequestDTO, auth);
                responseMsg = String.format(
                        "예약이 완료되었습니다! (%s ~ %s, %d명)",
                        bookingRequestDTO.getStartDate(),
                        bookingRequestDTO.getEndDate(),
                        bookingRequestDTO.getHeadcount());
            }
        }

        // 답변/로그 저장(공통)
        chatLogService.saveBotChat(auth, responseMsg);
        redisChatService.addChatMessage(auth, new ChatMessageDTO(ChatRole.MODEL, responseMsg));
        return ResponseEntity.ok(new QueryResponseDTO(responseMsg));

    }

    private String formatPlaceResult(List<PlaceInfoDTO> places, String keyword) {
        if (places == null || places.isEmpty()) {
            return "죄송합니다. 대부도 라온아띠 펜션 주변에 '" + keyword + "' 정보를 찾지 못했습니다.";
        }
        StringBuilder sb = new StringBuilder("대부도 라온아띠 펜션 주변 " + keyword + " 추천 Top 5입니다:\n");
        int idx = 1;
        for (PlaceInfoDTO p : places) {
            sb.append(idx++)
                    .append(". ").append(p.getName())
                    .append(" (★").append(p.getRating()).append(") ")
                    .append("- ").append(p.getAddress()).append("\n");
        }
        return sb.toString();
    }
}

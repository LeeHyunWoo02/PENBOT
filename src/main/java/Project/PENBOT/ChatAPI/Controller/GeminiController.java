package Project.PENBOT.ChatAPI.Controller;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.ChatAPI.Dto.*;
import Project.PENBOT.ChatAPI.Entity.ChatRole;
import Project.PENBOT.ChatAPI.Service.ChatLogService;
import Project.PENBOT.ChatAPI.Service.GeminiService;
import Project.PENBOT.ChatAPI.Service.GooglePlacesService;
import Project.PENBOT.ChatAPI.Service.RedisChatService;
import Project.PENBOT.CustomException.UnableBookingException;
import Project.PENBOT.CustomException.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tag(name = "Gemini 챗봇 API", description = "Gemini AI를 통한 펜션 예약 및 맛집 안내 챗봇 기능 제공")
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

    @Operation(
            summary = "Gemini 챗봇에게 질문",
            description = "사용자의 자연어 질문을 분석하여 예약 가능 여부 또는 맛집/카페 정보를 제공합니다. 예약 가능 시 직접 예약까지 수행합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자의 질문 텍스트",
                    required = true,
                    content = @Content(schema = @Schema(implementation = QueryRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 응답 (예약 정보 또는 맛집 안내 등)"),
            @ApiResponse(responseCode = "404", description = "예약 불가 등 사용자 요청 오류"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/ask")
    public ResponseEntity<QueryResponseDTO> askGemini(@RequestBody QueryRequestDTO requestDTO,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        // ChatLog에 질문 저장 && Redis에 질문 저장
        chatLogService.saveUserChat(auth, requestDTO);
        ChatMessageDTO chatMessageDTO= new ChatMessageDTO(ChatRole.USER, requestDTO.getText());
        redisChatService.addChatMessage(auth, chatMessageDTO);

        String userText = requestDTO.getText();
        String lower = userText == null ? "" : userText.toLowerCase().trim();

        String responseMsg;
        
        // 위치 / 주소 문의 => Goolge Places TextSearch로 즉시 응답
        if (isAddressQuestion(lower)) {
            Optional<QueryResponseDTO> opt = googlePlacesService.findPlaceAddressByText();
            if (opt.isPresent()) {
                QueryResponseDTO p = opt.get();
                responseMsg = String.format(
                        "라온아띠 펜션 위치 안내입니다.\n\n" +
                                "• 주소: %s\n", p.getResult());
            } else {
                responseMsg = "죄송합니다. 현재 라온아띠 펜션 주소를 확인하지 못했습니다. 잠시 후 다시 시도해 주세요.";
            }
            // 저장 + 반환
            chatLogService.saveBotChat(auth, responseMsg);
            redisChatService.addChatMessage(auth, new ChatMessageDTO(ChatRole.MODEL, responseMsg));
            return ResponseEntity.ok(new QueryResponseDTO(responseMsg));
        }
        
        // 맛집 / 카페 등 주변 추천
        if (lower.contains("맛집") || lower.contains("카페")) {
            String keyword = lower.contains("카페") ? "카페" : "맛집";
            String type = lower.contains("카페") ? "cafe" : "restaurant";
            List<PlaceInfoDTO> places = googlePlacesService.searchNearby(type);

            // 결과가 없으면 안내, 있으면 Top 5 안내
            String resultMsg = formatPlaceResult(places, keyword);

            // 답변 저장/반환
            chatLogService.saveBotChat(auth, resultMsg);
            ChatMessageDTO chatMessageDTO2 = new ChatMessageDTO(ChatRole.MODEL, resultMsg);
            redisChatService.addChatMessage(auth, chatMessageDTO2);

            return ResponseEntity.ok(new QueryResponseDTO(resultMsg));
        }


        // 그 외 Gemini API 호출
        String answer = geminiService.getCompletion(requestDTO.getText(), auth);

        // JSON 추출 & BookingRequestDTO 파싱 ( 예약 분기 )
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
                // 예약 관련이 아닌 경우 FAQ 등 자연어만 응답하도록!
                bookingRequestDTO = null;
            }
        }

        // ChatLog에 답변 저장 && Redis에 답변 저장
        chatLogService.saveBotChat(auth, answer);
        ChatMessageDTO chatMessageDTO2 = new ChatMessageDTO(ChatRole.MODEL, answer);
        redisChatService.addChatMessage(auth, chatMessageDTO2);
        responseMsg = answer;

        // 예약 관련(JSON 추출 성공) → 예약/예약
        if (bookingRequestDTO != null
                && bookingRequestDTO.getStartDate() != null
                && bookingRequestDTO.getEndDate() != null) {

            // 예약 가능만 문의(headcount 없는 경우, 또는 0/빈값)
            if (bookingRequestDTO.getHeadcount() <= 0) {
                if (!bookingService.isAvailable(bookingRequestDTO)) {
                    responseMsg = String.format("죄송합니다. 요청하신 기간(%s ~ %s)은 이미 예약이 되어 있습니다.",
                            bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate());
                    chatLogService.saveBotChat(auth, responseMsg);
                    redisChatService.addChatMessage(auth, new ChatMessageDTO(ChatRole.MODEL, responseMsg));
                    return ResponseEntity.badRequest().body(new QueryResponseDTO(responseMsg));
                }
                responseMsg = String.format(
                        "요청하신 기간(%s ~ %s)은 예약이 가능합니다. 인원 수를 입력해 주세요.",
                        bookingRequestDTO.getStartDate(),
                        bookingRequestDTO.getEndDate());
            }
            // 실제 예약 요청 (인원까지 모두 입력)
            else {
                if (!bookingService.isAvailable(bookingRequestDTO)) {
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
    private boolean isAddressQuestion(String lower) {
        if (lower == null) return false;
        return lower.contains("주소") || lower.contains("위치") || lower.contains("어디") ||
                lower.contains("찾아오는 길") || lower.contains("오시는 길") ||
                lower.contains("지도") || lower.contains("펜션 위치") || lower.contains("펜션 주소");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<QueryResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new QueryResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(UnableBookingException.class)
    public ResponseEntity<QueryResponseDTO> handleUserNotFound(UnableBookingException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new QueryResponseDTO(ex.getMessage()));
    }
}

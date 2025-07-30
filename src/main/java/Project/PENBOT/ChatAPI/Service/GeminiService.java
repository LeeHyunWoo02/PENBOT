package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Dto.*;
import Project.PENBOT.ChatAPI.Entity.ChatRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GeminiService {
    public static final String GEMINI_FLASH = "gemini-1.5-flash";
    public static final String GEMINI_PRO = "gemini-1.5-pro";

    private final GeminiInterface geminiInterface;
    private final RedisChatService redisChatService;
    private final GooglePlacesService googlePlacesService;

    @Autowired
    public GeminiService(GeminiInterface geminiInterface, RedisChatService redisChatService, GooglePlacesService googlePlacesService) {
        this.geminiInterface = geminiInterface;
        this.redisChatService = redisChatService;
        this.googlePlacesService = googlePlacesService;
    }
    public String getCompletion(String text, String auth){
        // 사용자 질문이 장소 추천 관련인지 확인
        if (isPlaceRecommendQuestion(text)){
            String place = extractPlaceType(text);
            List<PlaceInfoDTO> places = googlePlacesService.searchNearby(place, place);
            String placePrompt = makePlaceRecommendPrompt(places, text);

            GeminiRequestDTO placeRequest = buildSinglePrompt(placePrompt);
            GeminiResponseDTO response = geminiInterface.getCompletion(GEMINI_FLASH, placeRequest);

            return response.getCandidates()
                    .stream()
                    .findFirst()
                    .flatMap(candidate -> candidate.getContent().getParts()
                            .stream()
                            .findFirst()
                            .map(part -> Optional.ofNullable(((TextPart) part).getText()).orElse(null)))
                    .orElse(null);
        }

        // 일반 예약 관련 질문 처리
        List<ChatMessageDTO> context = redisChatService.getRecentMessages(auth);

        GeminiRequestDTO request = buildGeminiBookingPrompt(context, text);
        GeminiResponseDTO response = geminiInterface.getCompletion(GEMINI_FLASH, request);



        return response.getCandidates()
                .stream()
                .findFirst()
                .flatMap(candidate -> candidate.getContent().getParts()
                        .stream()
                        .findFirst()
                        .map(part -> Optional.ofNullable(((TextPart) part).getText()).orElse(null)))
                .orElse(null);
    }

    public GeminiRequestDTO buildGeminiBookingPrompt(List<ChatMessageDTO> context,String latestUserInput){
        List<Content> contents = new ArrayList<>();

        String promptText =
                "아래 대화에서 사용자의 의도를 분석해 적절하게 응답하세요.\n" +
                        "1. **예약 관련 질문/요청**(날짜, 인원, 예약 등)일 경우:\n" +
                        "   - 예약 가능 여부만 묻는 질문(예: '예약 가능해?', '방 비었어?' 등)은 startDate, endDate만 JSON으로 반환하고 headcount는 포함하지 마세요.\n" +
                        "   - 예약 요청(예: '예약해줘', '예약 신청' 등)일 경우 반드시 headcount도 함께 JSON으로 반환하세요. headcount가 없으면 안내문구로 '몇 명이 예약하시나요?'도 반환하세요.\n" +
                        "   - 예시 1) 예약 가능 문의:\n" +
                        "     User: 8월 25일 예약 가능해?\n" +
                        "     → { \"startDate\": \"2024-08-25\", \"endDate\": \"2024-08-25\" }\n" +
                        "     안내문구: \"예약 가능합니다!\"\n" +
                        "   - 예시 2) 예약 요청(인원 누락):\n" +
                        "     User: 8월 25일 예약해줘\n" +
                        "     → { \"startDate\": \"2024-08-25\", \"endDate\": \"2024-08-25\", \"headcount\": 0 }\n" +
                        "     안내문구: \"몇 명이 예약하시나요?\"\n" +
                        "   - 예시 3) 예약 요청(모든 정보):\n" +
                        "     User: 8월 25일부터 27일까지 3명 예약 신청\n" +
                        "     → { \"startDate\": \"2024-08-25\", \"endDate\": \"2024-08-27\", \"headcount\": 3 }\n" +
                        "     안내문구: \"예약이 완료되었습니다.\"\n" +
                        "2. **예약과 무관한 일반 질문**(예: 펜션 위치, 체크인 시간, 날씨, 교통 등)일 경우:\n" +
                        "   - JSON은 반환하지 말고, 자연어 답변만 하세요.\n" +
                        "   - 우리 펜션은 대부도 라온아띠 펜션이야. 해당 정보를 고려해서 답변해줘.\n" +
                        "   - 예시:\n" +
                        "     User: 펜션 위치가 어디야?\n" +
                        "     → 안내문구: \"저희 펜션은 강원도 속초시에 위치해 있습니다.\"\n";


        contents.add(new Content(ChatRole.USER, List.of(new TextPart(promptText))));

        // Redis에 저장된 과거 대화 context 추가
        for(ChatMessageDTO msg : context){
            ChatRole role = msg.getRole() == ChatRole.USER ? ChatRole.USER : ChatRole.MODEL;
            contents.add(new Content(role, List.of(new TextPart(msg.getMessage()))));
        }

        // 이번 요청의 User 입력도 마지막에 추가
        contents.add(new Content(ChatRole.USER, List.of(new TextPart(latestUserInput))));

        GeminiRequestDTO request = new GeminiRequestDTO();
        request.setContents(contents);
        return request;
    }

    // 장소 추천 질문 판별
    private boolean isPlaceRecommendQuestion(String text) {
        String t = text.toLowerCase();
        return t.contains("맛집") || t.contains("카페") || t.contains("관광지") ||
                t.contains("근처") || t.contains("주변") || t.contains("놀거리");
    }

    // 장소 유형 추출
    private String extractPlaceType(String text) {
        if (text.contains("맛집") || text.contains("음식점")){
            return "restaurant";
        }
        if (text.contains("카페")) {
            return "cafe";
        }
        if (text.contains("관광지") || text.contains("명소")){
            return "tourist_attraction";
        }
        return "restaurant";
    }

    // ▶️ Google Place 결과 → Gemini 자연어 추천 프롬프트 생성
    private String makePlaceRecommendPrompt(List<PlaceInfoDTO> places, String userText) {
        StringBuilder sb = new StringBuilder();
        sb.append("대부도 라온아띠 펜션 주변 추천 장소입니다:\n");
        int idx = 1;
        for (PlaceInfoDTO p : places) {
            sb.append(idx++).append(". ").append(p.getName())
                    .append(" (평점: ").append(p.getRating())
                    .append(", 주소: ").append(p.getAddress()).append(")\n");
        }
        sb.append("\n위의 장소 중 손님에게 추천할 만한 곳을 자연스럽게 안내해줘. " +
                "사용자 질문: ").append(userText);
        return sb.toString();
    }

    // ▶️ 단일 프롬프트만 Gemini에 보낼 때 (장소추천용)
    private GeminiRequestDTO buildSinglePrompt(String prompt) {
        List<Content> contents = new ArrayList<>();
        contents.add(new Content(ChatRole.USER, List.of(new TextPart(prompt))));
        GeminiRequestDTO request = new GeminiRequestDTO();
        request.setContents(contents);
        return request;
    }
}

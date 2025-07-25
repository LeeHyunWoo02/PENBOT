package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Dto.*;
import Project.PENBOT.ChatAPI.Entity.Role;
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

    @Autowired
    public GeminiService(GeminiInterface geminiInterface, RedisChatService redisChatService) {
        this.geminiInterface = geminiInterface;
        this.redisChatService = redisChatService;
    }
    public String getCompletion(String text, String auth){
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
                "아래 대화에서 사용자의 의도를 파악해 JSON으로 반환하세요.\n"  +
                        "- 예약 가능 여부만 묻는 질문(예약 가능해?, 방 비었어? 등)이라면, startDate, endDate만 반환하고 headcount는 포함하지 마세요.\n" +
                        "- 예약 요청(예약해줘, 예약 신청 등)이라면 반드시 headcount도 반환하세요. headcount가 없으면 안내문구로 '몇 명이 예약하시나요?'를 함께 반환하세요.\n\n" +
                        "예시 1) 예약 가능 문의:\n" +
                        "User: 8월 25일 예약 가능해?\n" +
                        "→ { \"startDate\": \"2024-08-25\", \"endDate\": \"2024-08-25\" }\n" +
                        "안내문구: \"예약 가능합니다!\"\n" +
                        "예시 2) 예약 요청:\n" +
                        "User: 8월 25일 예약해줘\n" +
                        "→ { \"startDate\": \"2024-08-25\", \"endDate\": \"2024-08-25\", \"headcount\": 0 }\n" +
                        "안내문구: \"몇 명이 예약하시나요?\"\n" +
                        "예시 3) 예약 요청(모든 정보):\n" +
                        "User: 8월 25일부터 27일까지 3명 예약 신청\n" +
                        "→ { \"startDate\": \"2024-08-25\", \"endDate\": \"2024-08-27\", \"headcount\": 3 }\n" +
                        "안내문구: \"예약이 완료되었습니다.\"\n";

        contents.add(new Content(Role.USER, List.of(new TextPart(promptText))));

        // Redis에 저장된 과거 대화 context 추가
        for(ChatMessageDTO msg : context){
            Role role = msg.getRole() == Role.USER ? Role.USER : Role.MODEL;
            contents.add(new Content(role, List.of(new TextPart(msg.getMessage()))));
        }

        // 이번 요청의 User 입력도 마지막에 추가
        contents.add(new Content(Role.USER, List.of(new TextPart(latestUserInput))));

        GeminiRequestDTO request = new GeminiRequestDTO();
        request.setContents(contents);
        return request;
    }

}

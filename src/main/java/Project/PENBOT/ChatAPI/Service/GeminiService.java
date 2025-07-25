package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Dto.*;
import Project.PENBOT.ChatAPI.Entity.Role;
import Project.PENBOT.User.Util.JwtUtil;
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
                "아래 대화에서 예약 정보를 추출해 JSON으로 반환하세요.\n" +
                        "누락된 항목이 있다면 어떤 정보가 필요한지 한글로 안내문구도 추가해서 반환하세요.\n\n" +
                        "반환 예시:\n" +
                        "{\n  \"startDate\": \"2024-07-30\",\n  \"endDate\": \"2024-08-01\",\n  \"headcount\": 2\n}\n" +
                        "안내문구: \"예약 인원이 입력되지 않았습니다. 몇 명이 예약하시나요?\"\n";
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

//    private GeminiResponseDTO getCompletion(GeminiRequestDTO request,String auth){
//        List<ChatMessageDTO> context = redisChatService.getRecentMessages(auth);
//
//        List<Content> contests = new ArrayList<>();
//
//        for(ChatMessageDTO message : context){
//            Content content = new Content();
//            content.setRole(message.getRole());
//            content.setParts(List.of(new TextPart(message.getMessage())));
//            contests.add(content);
//        }
//        Content userContent = new Content();
//        userContent.setRole(Role.USER);
//        userContent.setParts(List.of(new TextPart(request.getContents().get(0).getParts().get(0).getText())));
//        contests.add(userContent);
//        GeminiRequestDTO fullReqeust = new GeminiRequestDTO();
//        fullReqeust.setContents(contests);
//        return geminiInterface.getCompletion(GEMINI_FLASH, fullReqeust);

//    }
//    public String getCompletion(String text, String auth){
//
//        GeminiRequestDTO request = new GeminiRequestDTO(Role.USER,text);
//        GeminiResponseDTO response = getCompletion(request, auth);
//
//        return response.getCandidates()
//                .stream()
//                .findFirst().flatMap(candidate -> candidate.getContent().getParts()
//                        .stream()
//                        .findFirst()
//                        .map(part -> Optional.ofNullable(((TextPart)part).getText()).orElse(null)))
//                .orElse(null);

//    }
}

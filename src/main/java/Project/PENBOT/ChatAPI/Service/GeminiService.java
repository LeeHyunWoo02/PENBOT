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
    private final JwtUtil jwtUtil;

    @Autowired
    public GeminiService(GeminiInterface geminiInterface, RedisChatService redisChatService, JwtUtil jwtUtil) {
        this.geminiInterface = geminiInterface;
        this.redisChatService = redisChatService;
        this.jwtUtil = jwtUtil;
    }

    private GeminiResponseDTO getCompletion(GeminiRequestDTO request,String auth){
        List<ChatMessageDTO> context = redisChatService.getRecentMessages(auth);
        List<Content> contests = new ArrayList<>();

        for(ChatMessageDTO message : context){
            Content content = new Content();
            content.setRole(message.getRole());
            content.setParts(List.of(new TextPart(message.getMessage())));
            contests.add(content);
        }
        Content userContent = new Content();
        userContent.setRole(Role.USER);
        userContent.setParts(List.of(new TextPart(request.getContents().get(0).getParts().get(0).getText())));
        contests.add(userContent);
        GeminiRequestDTO fullReqeust = new GeminiRequestDTO();
        fullReqeust.setContents(contests);
        return geminiInterface.getCompletion(GEMINI_FLASH, fullReqeust);
    }

    public String getCompletion(String text, String auth){

        GeminiRequestDTO request = new GeminiRequestDTO(Role.USER,text);
        GeminiResponseDTO response = getCompletion(request, auth);

        return response.getCandidates()
                .stream()
                .findFirst().flatMap(candidate -> candidate.getContent().getParts()
                        .stream()
                        .findFirst()
                        .map(part -> Optional.ofNullable(((TextPart)part).getText()).orElse(null)))
                .orElse(null);
    }
}

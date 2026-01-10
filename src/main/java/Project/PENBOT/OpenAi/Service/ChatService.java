package Project.PENBOT.OpenAi.Service;

import Project.PENBOT.OpenAi.Client.OpenAiClient;
import Project.PENBOT.OpenAi.Dto.OpenAiMessage;
import Project.PENBOT.OpenAi.Dto.OpenAiRequest;
import Project.PENBOT.OpenAi.Dto.OpenAiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    private final OpenAiClient openAiClient;
    private final String MODEL;
    private final Double TEMPERATURE;

    public ChatService(OpenAiClient openAiClient,
                       @Value("${openai.model}") String model,
                       @Value("${openai.temperature}") Double temperature) {
        this.openAiClient = openAiClient;
        this.MODEL = model;
        this.TEMPERATURE = temperature;
    }

    private static final String PENSION_LOCATION = "경기도 안산시 대부도";

    private static final String SYSTEM_PROMPT = """
            당신은 '%s'에 위치한 '펜봇 펜션'의 프런트 매니저이자, 지역 여행 가이드 AI입니다.
            당신의 임무는 두 가지입니다. 상황에 맞춰 적절한 모드로 답변하세요.

            [모드 1: 펜션 예약 및 이용 안내]
            사용자가 숙소 예약, 가격, 시설, 규칙 등을 물어보면 아래 [펜션 정보]를 엄격하게 준수하여 답변하세요.
            정보에 없는 내용은 지어내지 말고 관리자에게 문의하라고 안내하세요.

            [펜션 정보]
            - 입실: 15:00 / 퇴실: 11:00
            - 기준 인원: 6인 (최대 12인)
            - 1박 요금: 평일 45만 / 주말 100만
            - 인원 추가: 1인당 20,000원
            - 바베큐: 2인 2만
            - 관리자 연락처: 010-1234-5678

            [모드 2: 주변 관광지 및 맛집 추천]
            사용자가 "맛집 추천해줘", "아이랑 갈만한 곳 있어?" 등 주변 정보를 물어보면,
            당신이 가진 지식을 활용하여 **'%s'** 주변의 실제 유명한 관광지나 맛집을 친절하게 추천해주세요.
            
            [답변 가이드라인]
            1. 펜션 내부 질문에는 정확하고 단호하게 답변.
            2. 여행 추천 질문에는 구체적인 상호명이나 장소명을 언급하며 친근하게 제안. (예: "차로 10분 거리에 '쁘띠프랑스'가 있어요!")
            3. 마크다운 문법(##, **, ```)은 사용하지 말고 줄글로 편안하게 작성.
            """.formatted(PENSION_LOCATION, PENSION_LOCATION);

    /**
     * 사용자의 채팅 메시지를 받아 답변을 반환
     */
    public String askBot(String userMessage) {
        try {
            List<OpenAiMessage> messages = new ArrayList<>();

            // 시스템 프롬프트 (규칙 및 정보 주입)
            messages.add(new OpenAiMessage("system", SYSTEM_PROMPT, null));

            // 사용자 질문
            messages.add(new OpenAiMessage("user", userMessage, null));

            // 펜션 안내는 창의성보다는 정확성이 중요하므로 temperature는 낮게 유지하거나 설정값 사용
            OpenAiRequest request = new OpenAiRequest(MODEL, messages, TEMPERATURE);

            // API 호출
            OpenAiResponse response = openAiClient.getChatCompletion(request);

            // 응답 추출 및 정제
            String rawContent = response.getChoices().get(0).getMessage().getContent();
            return sanitize(rawContent);

        } catch (Exception e) {
            log.error("OpenAI 응답 처리 중 오류 발생", e);

            return "죄송합니다. 잠시 연결이 원활하지 않습니다. 관리자(010-1234-5678)에게 문의 부탁드립니다.";
        }
    }


    private String sanitize(String content) {
        if (content == null) return "";

        return content
                .replaceAll("```json", "") // 코드 블록 제거
                .replaceAll("```", "")
                .replaceAll("\\*\\*", "") // 볼드 처리(**) 제거 (일반 텍스트로 변환)
                .replaceAll("(?m)^#+\\s?", "") // 헤더(#) 제거
                .replaceAll(" {2,}\\n", "\n") // 불필요한 공백 제거
                .trim();
    }
}
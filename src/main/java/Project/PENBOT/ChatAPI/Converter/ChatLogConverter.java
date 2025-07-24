package Project.PENBOT.ChatAPI.Converter;


import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Entity.ChatLog;
import Project.PENBOT.ChatAPI.Entity.Role;
import Project.PENBOT.User.Entity.User;

public class ChatLogConverter {
    public static ChatLog UsertoEntity(QueryRequestDTO requestDTO, User user) {
        return ChatLog.builder()
                .user(user)
                .message(requestDTO.getText())
                .role(Role.USER)
                .build();
    }

    public static ChatLog BottoEntity(String answer, User user){
        return ChatLog.builder()
                .user(user)
                .message(answer)
                .role(Role.MODEL)
                .build();
    }
}

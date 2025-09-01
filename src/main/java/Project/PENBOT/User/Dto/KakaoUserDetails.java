package Project.PENBOT.User.Dto;

import Project.PENBOT.User.Repository.OAuth2UserInfo;

import java.util.Map;

public class KakaoUserDetails implements OAuth2UserInfo {

    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccountAttributes;
    private Map<String, Object> profileAttributes;

    public KakaoUserDetails(Map<String, Object> attributes){
        this.attributes = attributes;
        this.kakaoAccountAttributes = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccountAttributes != null) {
            this.profileAttributes = (Map<String, Object>) kakaoAccountAttributes.get("profile");
        } else {
            this.profileAttributes = null;
        }
    }

    @Override
    public String getProviderId() {
        Object idObject = attributes.get("id");

        if (idObject instanceof Number) {
            return String.valueOf(((Number) idObject).longValue());
        }
        return idObject != null ? idObject.toString() : null;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getName() {
        if(profileAttributes != null && profileAttributes.containsKey("nickname")){
            return (String) profileAttributes.get("nickname");
        }
        return null;
    }

    @Override
    public String getEmail() {
        if(kakaoAccountAttributes != null && kakaoAccountAttributes.containsKey("email")){
            return (String) kakaoAccountAttributes.get("email");
        }
        return null;
    }

    @Override
    public String getMobile() {
        Object phone = kakaoAccountAttributes.get("phone_number"); // 동의/카카오싱크에 따라 존재
        return phone != null ? String.valueOf(phone) : null;
    }
}

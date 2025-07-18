package Project.PENBOT.User.UserDetails;

import Project.PENBOT.User.Repository.OAuth2UserInfo;

import java.util.Map;

public class NaverUserDetails implements OAuth2UserInfo {

    private Map<String,Object> attributes;
    private Map<String, Object> response;

    public NaverUserDetails(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }
    @Override
    public String getProvider() {
        return "Naver";
    }

    @Override
    public String getProviderId() {
        if(response != null && response.containsKey("id")) {
            return response.get("id").toString();
        } else if (attributes.containsKey("id")) {
            return attributes.get("id").toString();
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return response.get("name").toString();
    }

    @Override
    public String getEmail() {
        return response.get("email").toString();
    }

    @Override
    public String getMobile() {
        return response.get("mobile").toString();
    }
}

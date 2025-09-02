package Project.PENBOT.User.Dto;

import Project.PENBOT.User.Repository.OAuth2UserInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NaverUserDetails implements OAuth2UserInfo {

    private final Map<String,Object> attributes;

    @SuppressWarnings("unchecked")
    public NaverUserDetails(Map<String, Object> attributes) {
        if (attributes == null) {
            this.attributes = Collections.emptyMap();
            return;
        }

        Map<String,Object> flat = new HashMap<>(attributes);

        Object resp = attributes.get("response");
        if (resp instanceof Map<?,?> respMap) {
            respMap.forEach((k,v) -> flat.put(String.valueOf(k), v));
            flat.remove("response");
        }

        this.attributes = Collections.unmodifiableMap(flat);
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        return name != null ? String.valueOf(name) : null;
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email != null ? String.valueOf(email) : null;
    }

    @Override
    public String getMobile() {
        Object mobile = attributes.get("mobile");
        if (mobile == null) {
            mobile = attributes.get("mobile_e164"); // 네이버는 이 키로도 올 수 있음
        }
        return mobile != null ? String.valueOf(mobile) : null;
    }
    // (필요하면 attributes 그대로 꺼내는 메서드 추가 가능)
    public Map<String, Object> getFlattenedAttributes() {
        return attributes;
    }
}

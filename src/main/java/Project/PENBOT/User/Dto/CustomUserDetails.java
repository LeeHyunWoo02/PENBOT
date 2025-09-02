package Project.PENBOT.User.Dto;

import Project.PENBOT.User.Entity.User;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class CustomUserDetails implements UserDetails, OAuth2User {

    @JsonIgnore
    private final transient User userRef;
    private Map<String, Object> attributes;


    private final int userId;
    private final String email;
    private final String name;
    private final String role;

//    public CustomUserDetails(User user, Map<String, Object> attributes) {
//        this.user = Objects.requireNonNull(user, "user must not be null");
//        this.attributes = attributes == null
//                ? Collections.emptyMap()
//                : Collections.unmodifiableMap(new HashMap<>(attributes));
//    }

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        Objects.requireNonNull(user, "user must not be null");
        this.userRef = user; // 필요 시 레이지 액세스용으로만 사용

        // ✅ 얇은 필드만 복사 (toString/equals/hashCode가 엔티티 그래프를 타지 않게)
        this.userId = user.getId();
        this.email  = user.getEmail();
        this.name   = user.getName();
        this.role = String.valueOf(user.getRole());

        // ✅ attributes는 사본 + 불변
        this.attributes = (attributes == null)
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Stream.of(new SimpleGrantedAuthority(user.getRole().toString()))
//                .collect(toList());
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ 단일 권한만 생성 (Stream → collect는 유지)
        return Stream.of(new SimpleGrantedAuthority(role)).collect(toList());
    }

    @Override
    public String getName() {
        Object providerId = attributes.get("id");
        return (providerId != null) ? String.valueOf(providerId) : String.valueOf(userId);
    }
//
//    @Override
//    public String getName() {
//        return user.getName();
//    }

    @Override
    public String getPassword() {
        return userRef.getPassword();
    }

    @Override
    public String getUsername() {
        return email;
    }

    public int getId(){
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // ✅ 동등성은 "식별자"만 사용 (엔티티 그래프를 타지 않게)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails that)) return false;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        // ✅ 민감정보/그래프 누수 없이 최소 정보만
        return "CustomUserDetails{id=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
}

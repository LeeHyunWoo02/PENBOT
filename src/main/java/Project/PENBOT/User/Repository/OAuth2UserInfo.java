package Project.PENBOT.User.Repository;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getName();
    String getEmail();
    String getMobile();
}

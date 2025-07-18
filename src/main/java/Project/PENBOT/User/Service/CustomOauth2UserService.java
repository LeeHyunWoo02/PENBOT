package Project.PENBOT.User.Service;

import Project.PENBOT.User.Dto.JoinUserDTO;
import Project.PENBOT.User.Entity.Role;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.OAuth2UserInfo;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Dto.CustomUserDetails;
import Project.PENBOT.User.Dto.NaverUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JoinService joinService;

    public CustomOauth2UserService(UserRepository userRepository, JoinService joinService) {
        this.userRepository = userRepository;
        this.joinService = joinService;
    }

    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        return processOAuth2User(request, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String provider = request.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if (provider.equals("naver")) {
            oAuth2UserInfo = new NaverUserDetails(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않은 OAuth2 제공자.");
        }

        String userInfoProvider = oAuth2UserInfo.getProvider();
        String userInfoId = userInfoProvider + "_" + oAuth2UserInfo.getProviderId();

        String email = oAuth2UserInfo.getEmail();
        String mobile = oAuth2UserInfo.getMobile();
        String role = Role.TEMP.name();
        String name = oAuth2UserInfo.getName();

        JoinUserDTO dto = new JoinUserDTO(name, null, mobile, email,
                role, userInfoProvider, userInfoId);

        User savedUser = userRepository.findByEmail(email);

        if(savedUser == null){
            return registerNewUser(oAuth2User,dto);
        } else {
            return new CustomUserDetails( savedUser, oAuth2User.getAttributes());
        }
    }

    private CustomUserDetails registerNewUser(OAuth2User oAuth2User, JoinUserDTO dto) {

        User savedUser = joinService.JoinUser(dto);

        return new CustomUserDetails(savedUser, oAuth2User.getAttributes());
    }

}

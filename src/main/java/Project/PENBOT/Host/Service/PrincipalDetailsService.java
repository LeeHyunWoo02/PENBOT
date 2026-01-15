package Project.PENBOT.Host.Service;

import Project.PENBOT.Host.Entity.Admin;
import Project.PENBOT.Host.Repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Admin admin = adminRepository.findByUsername(username);

        if (admin == null) {
            throw new UsernameNotFoundException("Admin not found with username: " + username);
        }
        return User.builder()
                .username(admin.getUsername())
                .password("{noop}" + admin.getPassword())
                .roles("HOST")
                .build();
    }
}
package info.app.juleaften.security;

import info.app.juleaften.repository.UserProfileRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserProfileRepository userProfileRepository;

    public UserDetailsServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userProfileRepository.findByUser_Email(email)
                .map(userProfile -> (UserDetails) userProfile)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

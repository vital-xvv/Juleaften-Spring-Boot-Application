package info.app.juleaften.model;

import info.app.juleaften.dict.AuthenticationMethod;
import info.app.juleaften.dict.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Temporal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter

public class UserProfile implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @NotBlank
    private String password;
    private boolean verified;
    private boolean active;
    @Enumerated(EnumType.STRING)
    private List<UserRole> roles;
    @Temporal
    private LocalDateTime createdAt;
    @Temporal
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private AuthenticationMethod signUpMethod;
    private String verificationTokenHash;
    @Temporal
    private LocalDateTime verificationTokenExpiresAt;

    @PrePersist
    public void initiateUserProfile() {
        createdAt = LocalDateTime.now();
        roles = new ArrayList<>();
        roles.add(UserRole.USER);
    }

    public boolean isUserProfileValid() {
        return verified && active;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return isUserProfileValid();
    }
}

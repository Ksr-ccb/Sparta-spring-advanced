package org.example.expert.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.global.auth.dto.AuthUser;
import org.example.expert.global.common.entity.Timestamped;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.global.config.PasswordEncoder;
import org.example.expert.global.exception.InvalidRequestException;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public User(String email, String password, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    private User(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    public static User fromAuthUser(AuthUser authUser) {
        return new User(authUser.getId(), authUser.getEmail(), authUser.getUserRole());
    }

    public void changePassword(UserChangePasswordRequest userChangePasswordRequest, PasswordEncoder encoder) {
        if (encoder.matches(userChangePasswordRequest.getNewPassword(), this.password)) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }
        this.password = encoder.encode(userChangePasswordRequest.getNewPassword());
    }

    public boolean isPasswordCorrect(String inputPassword, PasswordEncoder encoder){
        return encoder.matches(inputPassword, this.password);
    }


    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }
}

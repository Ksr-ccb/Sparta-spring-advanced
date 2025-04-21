package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;
import org.aspectj.lang.annotation.Before;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.global.auth.jwt.JwtUtil;
import org.example.expert.global.config.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    UserRole userRole;
    String bearerToken;

    @BeforeEach
    void setUp() {
        userRole = UserRole.USER;
        bearerToken = "bearerToken";
    }

    @Test
    @DisplayName("성공 - 회원가입")
    void signup_success(){
        //given
        SignupRequest signupRequest = new SignupRequest("email@eamil.com", "passWord1", "USER");
        String encodedPassword = "encodedPassword1";

        User user = new User(signupRequest.getEmail(), encodedPassword, userRole);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(user.getId(),user.getEmail(),userRole)).willReturn(bearerToken);
        //when
        SignupResponse response = authService.signup(signupRequest);

        //then
        assertNotNull(response);
        assertEquals(response.getBearerToken(), bearerToken);
    }

    @Test
    @DisplayName("성공 - 로그인")
    void signing_success(){
        //given
        SigninRequest signinRequest = new SigninRequest("email@email.conm", "password1");
        User user = new User(signinRequest.getEmail(), signinRequest.getPassword(), UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(user.isPasswordCorrect(signinRequest.getPassword(), passwordEncoder)).willReturn(true);
        given(jwtUtil.createToken(user.getId(),user.getEmail(),userRole)).willReturn(bearerToken);
        //when
        SigninResponse response = authService.signin(signinRequest);

        //then
        assertNotNull(response);
        assertEquals(response.getBearerToken(), bearerToken);
    }


}
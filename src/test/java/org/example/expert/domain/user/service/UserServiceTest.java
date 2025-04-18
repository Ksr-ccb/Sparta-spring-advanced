package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.global.config.PasswordEncoder;
import org.example.expert.global.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private  UserRepository userRepository;
    @Mock
    private  PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("성공 - 유저 정보 가져오기 성공")
    void getUser_success(){
        //given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        UserResponse getUserResponse = new UserResponse(user.getId(), user.getEmail());

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        //when
        UserResponse response = userService.getUser(userId);

        //then
        assertNotNull(response);
        assertEquals(response.getId(), getUserResponse.getId());
        assertEquals(getUserResponse.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("성공 - 유저 비밀번호 변경 성공")
    void changePassword_success(){
        //given
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword1","newPassword1");
        long userId = 1L;
        User user = new User("user1@example.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.matches(passwordRequest.getNewPassword(), user.getPassword())).willReturn(false); // 새 비번이 기존과 다름

        // when & then: 예외 없이 실행되면 성공
        assertDoesNotThrow(() -> userService.changePassword(userId, passwordRequest));
    }

    @Test
    @DisplayName("실패 - 현재 비밀번호 틀림")
    void changePassword_fail(){
        //given
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword1","newPassword1");
        long userId = 1L;
        User user = new User("user1@example.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword())).willReturn(false);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, passwordRequest)
        );

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 - 현재 비밀번호와 새로운 비밀번호가 같음")
    void changePassword_fail1(){
        //given
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword1","newPassword1");
        long userId = 1L;
        User user = new User("user1@example.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.matches(passwordRequest.getNewPassword(), user.getPassword())).willReturn(true); // 새 비번이 기존과 다름

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, passwordRequest)
        );

        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

}
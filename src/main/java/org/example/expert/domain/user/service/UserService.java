package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.global.config.PasswordEncoder;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.global.exception.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser(long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        User user = userRepository.findByIdOrElseThrow(userId);
        if(!user.isPasswordCorrect(userChangePasswordRequest.getOldPassword(), passwordEncoder)) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(userChangePasswordRequest, passwordEncoder);
    }
}

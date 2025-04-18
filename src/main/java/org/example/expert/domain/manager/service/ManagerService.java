package org.example.expert.domain.manager.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.global.exception.AuthException;
import org.example.expert.global.auth.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.global.exception.InvalidRequestException;
import org.example.expert.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public ManagerSaveResponse saveManager(AuthUser authUser, long todoId,
        ManagerSaveRequest managerSaveRequest) {

        Long authUserId = authUser.getId(); //로그인 유저
        Todo todo = todoRepository.findByIdOrElseThrow(todoId); //일정

        validateUserAndWriter(authUserId, todo.getUser());//로그인유저, 일정작성자 검사

        User managerUser = userRepository.findById(managerSaveRequest.getManagerUserId()) //담당자 확인
            .orElseThrow(() -> new NotFoundException("등록하려고 하는 담당자 유저가 존재하지 않습니다."));

        verifyManagerIdentity(authUserId, managerUser);

        Manager newManagerUser = new Manager(managerUser, todo);
        Manager savedManagerUser = managerRepository.save(newManagerUser);

        return new ManagerSaveResponse(
            savedManagerUser.getId(),
            new UserResponse(managerUser.getId(), managerUser.getEmail())
        );
    }

    private void validateUserAndWriter(Long authUserId, User writer){
        if (!ObjectUtils.nullSafeEquals(authUserId,
            Optional.ofNullable(writer).map(User::getId))) {
            throw new InvalidRequestException("일정 작성자 본인만 담당자를 등록할 수 있습니다. 로그인 정보와 작성자를 확인하세요.");
        }
    }

    private void verifyManagerIdentity(Long authUserId, User manager){
        if (ObjectUtils.nullSafeEquals(authUserId, manager.getId())) {
            throw new AuthException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ManagerResponse> getManagers(long todoId) {
        Todo todo = todoRepository.findByIdOrElseThrow(todoId);

        List<Manager> managerList = managerRepository.findByTodoIdWithUser(todo.getId());

        List<ManagerResponse> dtoList = new ArrayList<>();
        for (Manager manager : managerList) {
            User user = manager.getUser();
            dtoList.add(new ManagerResponse(
                manager.getId(),
                new UserResponse(user.getId(), user.getEmail())
            ));
        }
        return dtoList;
    }

    @Transactional
    public void deleteManager(long userId, long todoId, long managerId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Todo todo = todoRepository.findByIdOrElseThrow(todoId);

        if (todo.getUser() == null || !ObjectUtils.nullSafeEquals(user.getId(),
            todo.getUser().getId())) {
            throw new InvalidRequestException("해당 일정을 만든 유저가 유효하지 않습니다.");
        }

        Manager manager = managerRepository.findById(managerId)
            .orElseThrow(() -> new NotFoundException("Manager not found"));

        if (!ObjectUtils.nullSafeEquals(todo.getId(), manager.getTodo().getId())) {
            throw new AuthException("해당 일정에 등록된 담당자가 아닙니다.");
        }

        managerRepository.delete(manager);
    }
}

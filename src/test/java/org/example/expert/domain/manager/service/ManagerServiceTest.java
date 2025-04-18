package org.example.expert.domain.manager.service;

import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.global.auth.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.global.exception.AuthException;
import org.example.expert.global.exception.InvalidRequestException;
import org.example.expert.global.exception.NotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;


    @Test
    @DisplayName("성공 - 정상적인 담당자등록")
    void saveManager_success() {
        //given
        AuthUser authUser = new AuthUser(1L, "writer@email.com", UserRole.USER);
        AuthUser manager = new AuthUser(2L, "manager@email.com", UserRole.USER);
        long todoId = 10L;
        User writer = User.fromAuthUser(authUser);
        User managerUser = User.fromAuthUser(manager);

        Todo todo = new Todo("Title", "Contents", "Sunny", writer);
        ReflectionTestUtils.setField(todo, "id", todoId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUser.getId());
        Manager savedManagerUser = new Manager(managerUser, todo);

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(userRepository.findById(managerUser.getId())).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willReturn(savedManagerUser);

        UserResponse userResponse = new UserResponse(managerUser.getId(), managerUser.getEmail());

        //when
        ManagerSaveResponse response = managerService.saveManager(authUser,todoId,managerSaveRequest);

        //then
        assertNotNull(response);
        assertEquals(response.getId(), savedManagerUser.getId());
        assertEquals(userResponse.getId(), response.getUser().getId());
        assertEquals(userResponse.getEmail(), response.getUser().getEmail());
    }


    @Test
    @DisplayName("실패 - todo의 user와 로그인 user가 다르거나 null인 상태 ")
    void saveManager_fail1() {
        // given
        AuthUser authUser = new AuthUser(1L, "writer@email.com", UserRole.USER);
        AuthUser manager = new AuthUser(2L, "manager@email.com", UserRole.USER);
        long todoId = 10L;
        User managerUser = User.fromAuthUser(manager);

        Todo todo = new Todo("Title", "Contents", "Sunny", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUser.getId());

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자 본인만 담당자를 설정할 수 있습니다. 로그인 정보와 작성자를 확인하세요.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 - 등록하려고 하는 담당자를 찾을 수 없는 상태 ")
    void saveManager_fail2() {
        // given
        AuthUser authUser = new AuthUser(1L, "writer@email.com", UserRole.USER);
        long managerId = 100L;
        long todoId = 10L;
        User authUser1 = User.fromAuthUser(authUser);

        Todo todo = new Todo("Title", "Contents", "Sunny", authUser1);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerId);

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(userRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 - 등록하려고 하는 담당자와 작성자가 동일인물인 상태")
    void saveManager_fail3() {
        // given
        AuthUser authUser = new AuthUser(1L, "writer@email.com", UserRole.USER);
        long todoId = 10L;
        User authUser1 = User.fromAuthUser(authUser);

        Todo todo = new Todo("Title", "Contents", "Sunny", authUser1);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(authUser1.getId());

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(userRepository.findById(authUser1.getId())).willReturn(Optional.of(authUser1));

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }



    @Test // 테스트코드 샘플
    @DisplayName("성공 -  manager_목록_조회에_성공한다")
    public void getManagers_success() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    @DisplayName("성공 - 담당자 삭제 성공")
    public void deleteManager_success() {
        // given
        long todoId = 1L;
        long userId = 1L;
        long managerId = 10L;

        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(mockManager));

        // when & then: 예외 없이 실행되면 성공
        assertDoesNotThrow(() -> managerService.deleteManager(userId, todoId, managerId));
    }

    @Test
    @DisplayName("실패 - 입력한 담당자 id 와 할일에 등록된 담당자 id 값이 다르다.")
    public void deleteManager_fail() {
        // given
        long todoId = 1L;
        long userId = 1L;
        long managerId = 10L;

        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        Todo managersTodo = new Todo("Title2", "Other Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), managersTodo);

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(mockManager));

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    @DisplayName("실패 - 해당 할 일에 담당자가 없음")
    public void deleteManager_fail2() {
        // given
        long todoId = 1L;
        long userId = 1L;
        long managerId = 10L;

        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("Manager not found", exception.getMessage());
    }
}

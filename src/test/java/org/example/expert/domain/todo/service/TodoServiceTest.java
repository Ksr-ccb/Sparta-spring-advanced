package org.example.expert.domain.todo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.global.auth.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    String title;
    String contents;
    String weather;
    User user;

    @BeforeEach
    void setUp() {
        weather = "Sunny";
        title = "title";
        contents = "contents";
        user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    @DisplayName("성공 - Todo 등록 정상")
    void saveTodo_success(){
        //given
        AuthUser authUser = new AuthUser(1L, "writer@email.com", UserRole.USER);

        User writer = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(writer, "id", 1L);

        TodoSaveRequest request = new TodoSaveRequest(title,contents);
        Todo todo = new Todo(request.getTitle(),request.getContents(), weather, writer);
        ReflectionTestUtils.setField(todo, "id", 1L);
        ReflectionTestUtils.setField(todo, "weather", weather);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        UserResponse userResponse = new UserResponse(writer.getId(), writer.getEmail());
        //when
        TodoSaveResponse result = todoService.saveTodo(authUser,request);

        //then
        assertNotNull(result);
        assertEquals(result.getId(), todo.getId());
        assertEquals(result.getTitle(), todo.getTitle());
        assertEquals(result.getContents(), todo.getContents());
        assertEquals(result.getWeather(), todo.getWeather());
        assertEquals(userResponse.getId(), result.getUser().getId());  // 필드로 비교
        assertEquals(userResponse.getEmail(), result.getUser().getEmail());
    }

    @Test
    @DisplayName("성공 - getTodos")
    void getTodos_success(){
        //given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page-1, size);

        Todo todo = new Todo(title,contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);
        ReflectionTestUtils.setField(todo, "weather", weather);

        Page<Todo> todos = new PageImpl<>(List.of(todo), pageable, 1);
        Page<TodoResponse> todoResponses = todos.map(td ->
            TodoResponse.of(todo, new UserResponse(user.getId(), user.getEmail()))
        );

        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todos);
        //when
        Page<TodoResponse> responses = todoService.getTodos(page, size);

        //then
        assertNotNull(responses);
        assertEquals(responses.getTotalElements(),todoResponses.getTotalElements());
        assertEquals(responses.getTotalPages(), todoResponses.getTotalPages());
    }

    @Test
    @DisplayName("성공 - getTodo")
    void getTodo_success(){
        //given
        long todoId = 1L;

        Todo todo = new Todo(title,contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        ReflectionTestUtils.setField(todo, "weather", weather);

        given(todoRepository.findByIdOrElseThrow(todoId)).willReturn(todo);
        //when
        TodoResponse response = todoService.getTodo(todoId);

        //then
        assertNotNull(response);
        assertEquals(response.getId(), todo.getId());
        assertEquals(response.getUser(), response.getUser());
    }
}
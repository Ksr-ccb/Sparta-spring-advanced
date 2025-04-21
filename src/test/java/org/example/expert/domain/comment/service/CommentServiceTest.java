package org.example.expert.domain.comment.service;

import java.util.List;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.global.auth.dto.AuthUser;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.global.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> commentService.saveComment(authUser, todoId, request));

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    @DisplayName("성공 - getComments")
    void getComments_success(){
        // given
        long todoId = 1;
        User user = new User("email@email.conm", "password1", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("title", "title", "contents", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Comment comment = new Comment("contents", user, todo);
        List<Comment> commentList = List.of(comment);
        List<CommentResponse> dtoList = List.of(
            new CommentResponse(comment.getId(),
                comment.getContents(),
                new UserResponse(user.getId(), user.getEmail()))
        );

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertNotNull(result);
        assertEquals(dtoList.size(), result.size());
        assertEquals(result.get(0).getId(), dtoList.get(0).getId());
        assertEquals(result.get(0).getContents(), dtoList.get(0).getContents());
        assertEquals(result.get(0).getUser().getId(), dtoList.get(0).getUser().getId());
        assertEquals(result.get(0).getUser().getEmail(), dtoList.get(0).getUser().getEmail());
    }
}

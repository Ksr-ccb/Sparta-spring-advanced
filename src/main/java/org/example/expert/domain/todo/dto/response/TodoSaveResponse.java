package org.example.expert.domain.todo.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.dto.response.UserResponse;

@Getter
public class TodoSaveResponse {

    private final Long id;
    private final String title;
    private final String contents;
    private final String weather;
    private final UserResponse user;

    @Builder
    public TodoSaveResponse(Long id, String title, String contents, String weather, UserResponse user) {

        this.id = id;
        this.title = title;
        this.contents = contents;
        this.weather = weather;
        this.user = user;
    }

    public static TodoSaveResponse of(Todo todo, String weather, UserResponse user) {
        return TodoSaveResponse.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .contents(todo.getContents())
            .weather(weather)
            .user(user)
            .build();
    }
}

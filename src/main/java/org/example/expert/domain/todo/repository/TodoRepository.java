package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.global.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    //기존 : 특정 테이블 조인시 fetch조인을 사용하여 조인 결과값에 중복값이 있을 경우 객체 하나만을 바라보도록 함.
    // fetch 조인을 하면 모든 entity 가 영속성 컨텍스트로 관리되기 떄문에 SQL 한번만으로 처리할 수 있음.
    // => N+1 문제 해결

    //@EntityGraph 는 특정 엔티티인 Todo 와 User 가 1:다 양방향 관계를 갖고있을 때 사용이 가능하다.
    // 하지만 @EntityGraph 는 left outer join 만을 지원하니 다른 방식이 필요하면 직접 JPQL 작성해야 한다.
    @Query("SELECT t FROM Todo t ORDER BY t.modifiedAt DESC")
    @EntityGraph(attributePaths = {"user"})
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "WHERE t.id = :todoId")
    @EntityGraph(attributePaths = {"user"})
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    int countById(Long todoId);


    default Todo findByIdOrElseThrow(Long id){
        return findById(id).orElseThrow(() -> new NotFoundException("Todo not found"));
    }
}

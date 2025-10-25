package store.bookscamp.api.tag.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Tag {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** 엔티티 생성 책임을 한 곳으로 모은 '팩토리 메서드' */
    public static Tag create(String name) {
        Tag t = new Tag();
        t.name = name;
        return t;
    }

    /** 세터 대신 의도를 드러내는 '도메인 메서드' */
    public void changeName(String name) {
        this.name = name;
    }
}

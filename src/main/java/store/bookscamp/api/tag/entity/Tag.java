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

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public static Tag create(String name) {
        Tag t = new Tag();
        t.name = name;
        return t;
    }

    public void changeName(String newName) {
        if (newName != null && !this.name.equals(newName)) {
            this.name = newName;
        }
    }

    public Tag(String name) {
        this.name = name;
    }
}

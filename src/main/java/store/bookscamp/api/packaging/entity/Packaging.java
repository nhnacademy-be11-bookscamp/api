package store.bookscamp.api.packaging.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.member.service.dto.MemberCreateDto;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Packaging {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String imageUrl;

    public Packaging(String name, Integer price,  String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public void change(String name, Integer price, String imageUrl) {
        if (name != null) this.name = name;
        if (price != null) this.price = price;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }
}

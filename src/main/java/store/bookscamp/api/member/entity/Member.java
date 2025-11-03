package store.bookscamp.api.member.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.bookscamp.api.common.entity.SoftDeleteEntity;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE `member` SET deleted_at = NOW(), status = 'WITHDRAWN' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Member extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private int point;

    @Enumerated(STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = false)
    private LocalDate statusUpdateDate;

    @Column(nullable = false, unique = true)
    private String username;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private LocalDate birthDate;

    public Member(String name,
                  String password,
                  String email,
                  String phone,
                  int point,
                  MemberStatus status,
                  LocalDate statusUpdateDate,
                  String username,
                  LocalDateTime lastLoginAt,
                  LocalDate birthDate
    ) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.point = point;
        this.status = status;
        this.statusUpdateDate = statusUpdateDate;
        this.username = username;
        this.lastLoginAt = lastLoginAt;
        this.birthDate = birthDate;
    }
    public void changeInfo(String name, String email, String phone){
        this.name= name;
        this.email=email;
        this.phone=phone;
    }

    public void changeStatus(MemberStatus memberStatus){
        this.status = memberStatus;
    }

    public void changePassword(String password){
        this.password = password;
    }
}
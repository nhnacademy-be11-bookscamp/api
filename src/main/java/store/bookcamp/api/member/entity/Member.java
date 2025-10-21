package store.bookcamp.api.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Integer point;

    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDate statusUpdateDate;

    @Column(nullable = false, unique = true)
    private String accountId;

    @Column(nullable = true)
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private LocalDate birthDate;

    public Member(String accountId, String password, String name, String email, String phone,
                  LocalDate birthDate){
        this.accountId = accountId;
        this.password = password;
        this.name = name;
        this.email= email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.point = 0;
        this.status = Status.NORMAL;
        this.statusUpdateDate = LocalDate.now();
    }
}

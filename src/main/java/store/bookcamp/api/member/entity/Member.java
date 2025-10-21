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
import store.bookcamp.api.member.service.MemberDto;

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

    public Member(MemberDto memberDto){
        this.accountId = memberDto.accountId();
        this.password = memberDto.password();
        this.name = memberDto.name();
        this.email= memberDto.email();
        this.phone = memberDto.phone();
        this.birthDate = memberDto.birthDate();
        this.point = 0;
        this.status = Status.NORMAL;
        this.statusUpdateDate = LocalDate.now();
    }
}

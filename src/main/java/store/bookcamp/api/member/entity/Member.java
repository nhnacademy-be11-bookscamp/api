package store.bookcamp.api.member.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Size(min = 8, max = 64)
    private String password;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    @Column(unique = true)
    private String phone;

    @NotNull
    private Integer point;

    @NotNull
    private Status status;

    @NotNull
    @Column(name = "status_update_date")
    private LocalDate statusUpdateDate;

    @NotNull
    @Column(name = "account_id" , unique = true)
    private String accountId;

    @Nullable
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @NotNull
    private LocalDate birth;

    public Member(String accountId, String password, String name, String email, String phone,
                  LocalDate birth){
        this.accountId = accountId;
        this.password = password;
        this.name = name;
        this.email= email;
        this.phone = phone;
        this.birth = birth;
        this.point = 0;
        this.status = Status.NORMAL;
        this.statusUpdateDate = LocalDate.now();
    }
}

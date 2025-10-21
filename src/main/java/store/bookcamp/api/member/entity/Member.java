package store.bookcamp.api.member.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Min(8)
    @Max(64)
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
    private State state;

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

}

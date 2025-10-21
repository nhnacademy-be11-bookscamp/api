package store.bookcamp.api.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Member {

    @Id
    private Long id;

    private String name;

    private String password;

    private String email;

    private String phone;

    private Integer point;

    private State state;

    @Column(name = "status_update_date")
    private LocalDate statusUpdateDate;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    private LocalDate birth;

}

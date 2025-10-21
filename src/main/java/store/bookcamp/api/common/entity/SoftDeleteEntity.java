package store.bookcamp.api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE #{#entityName} SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
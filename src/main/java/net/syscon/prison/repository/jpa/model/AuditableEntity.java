package net.syscon.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
abstract class AuditableEntity {
    @Column(name = "CREATE_USER_ID")
    @CreatedBy
    private String createUserId;

    @Column(name = "CREATE_DATETIME")
    @CreatedDate
    private LocalDateTime createDatetime;

    @Column(name = "MODIFY_USER_ID")
    @LastModifiedBy
    private String modifyUserId;

    @Column(name = "MODIFY_DATETIME")
    @LastModifiedDate
    private LocalDateTime modifyDatetime;
}

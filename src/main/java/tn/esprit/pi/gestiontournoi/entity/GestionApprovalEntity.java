package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class GestionApprovalEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    private Long requesterUserId;

    private String requesterName;

    private String requesterEmail;

    private Long approvedByUserId;

    private String approvedByName;

    private String approvedByEmail;

    @Column(length = 500)
    private String approvalNote;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

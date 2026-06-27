package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(
        name = "create_date",
        nullable = true
    )
    private LocalDateTime createDate;

    @Column(
        name = "created_by",
        length = 100,
        nullable = true
    )
    private String createdBy;

    @Column(
        name = "modify_date",
        nullable = true
    )
    private LocalDateTime modifyDate;

    @Column(
        name = "modified_by",
        length = 100,
        nullable = true
    )
    private String modifiedBy;

    @Column(
        name = "is_deleted"
    )
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now();
        this.modifyDate = LocalDateTime.now();
        
        String currentUser = getCurrentUser();
        if (this.createdBy == null) {
            this.createdBy = currentUser;
        }
        if (this.modifiedBy == null) {
            this.modifiedBy = currentUser;
        }
        
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifyDate = LocalDateTime.now();
        this.modifiedBy = getCurrentUser();
    }

    private String getCurrentUser() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // ignore
        }
        return "admin"; // default fallback if not logged in or during initial setup
    }
}

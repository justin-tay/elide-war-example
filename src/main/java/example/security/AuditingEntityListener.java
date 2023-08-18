package example.security;

import java.time.OffsetDateTime;

import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * AuditingEntityListener.
 */
public class AuditingEntityListener {
    @Inject
    private SecurityContext securityContext;

    @PrePersist
    public void onCreate(Object object) {
        if (object instanceof Auditable auditable) {
            auditable.setCreatedBy(securityContext.getCallerPrincipal().getName());
            auditable.setCreatedOn(OffsetDateTime.now());
        }
    }

    @PreUpdate
    public void onUpdate(Object object) {
        if (object instanceof Auditable auditable) {
            auditable.setUpdatedBy(securityContext.getCallerPrincipal().getName());
            auditable.setUpdatedOn(OffsetDateTime.now());
        }
    }
}

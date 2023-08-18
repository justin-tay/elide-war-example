package example.security;

import java.security.Principal;
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

    private String getName() {
        Principal principal = securityContext.getCallerPrincipal();
        return principal != null ? principal.getName() : null;
    }

    @PrePersist
    public void onCreate(Object object) {
        if (object instanceof Auditable auditable) {
            auditable.setCreatedBy(getName());
            auditable.setCreatedOn(OffsetDateTime.now());
        }
    }

    @PreUpdate
    public void onUpdate(Object object) {
        if (object instanceof Auditable auditable) {
            auditable.setUpdatedBy(getName());
            auditable.setUpdatedOn(OffsetDateTime.now());
        }
    }
}

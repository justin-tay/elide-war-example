package example.security;

import java.time.OffsetDateTime;

/**
 * Auditable entity.
 */
public interface Auditable {
    public void setCreatedBy(String name);

    public void setCreatedOn(OffsetDateTime createdOn);

    public void setUpdatedBy(String name);

    public void setUpdatedOn(OffsetDateTime createdOn);

    public String getCreatedBy();

    public OffsetDateTime getCreatedOn();

    public String getUpdatedBy();

    public OffsetDateTime getUpdatedOn();
}

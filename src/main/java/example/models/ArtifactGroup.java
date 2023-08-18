package example.models;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.LifeCycleHookBinding;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.graphql.subscriptions.annotations.Subscription;
import com.yahoo.elide.graphql.subscriptions.annotations.SubscriptionField;

import example.models.hooks.ArtifactGroupCreateHook;
import example.security.Auditable;
import example.security.checks.GroupOwnerIsUserCheck;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Include(name = "group", description = "Artifact group.", friendlyName = "Group")
@Table(name = "artifactgroup")
@Entity
@Subscription
@ReadPermission(expression = GroupOwnerIsUserCheck.GROUP_OWNER_IS_USER)
@LifeCycleHookBinding(operation = Operation.CREATE, phase = TransactionPhase.PRESECURITY, hook = ArtifactGroupCreateHook.class)
public class ArtifactGroup implements Auditable {
    @Id
    private String groupId = "";

    @SubscriptionField
    private String description = "";

    @SubscriptionField
    @OneToMany(mappedBy = "group")
    private List<ArtifactProduct> products = new ArrayList<>();

    private String createdBy;
    private OffsetDateTime createdOn;

    private String updatedBy;
    private OffsetDateTime updatedOn;
    
    private String owner;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ArtifactProduct> getProducts() {
        return products;
    }

    public void setProducts(List<ArtifactProduct> products) {
        this.products = products;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(OffsetDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

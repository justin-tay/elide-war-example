package example.models;

import com.yahoo.elide.annotation.Include;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Include(rootLevel = false, name = "product", description = "Artifact product.", friendlyName = "Product")
@Table(name = "artifactproduct")
@Entity
public class ArtifactProduct {
    @Id
    private String productId = "";

    private String name = "";

    private String description = "";

    @ManyToOne
    private ArtifactGroup group = null;

    @OneToMany(mappedBy = "artifact")
    private List<ArtifactVersion> versions = new ArrayList<>();

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArtifactGroup getGroup() {
        return group;
    }

    public void setGroup(ArtifactGroup group) {
        this.group = group;
    }

    public List<ArtifactVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ArtifactVersion> versions) {
        this.versions = versions;
    }
}

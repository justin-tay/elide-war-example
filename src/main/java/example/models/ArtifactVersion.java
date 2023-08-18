package example.models;

import java.time.OffsetDateTime;

import com.yahoo.elide.annotation.Include;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Include(rootLevel = false, name = "version", description = "Artifact version.", friendlyName = "Version")
@Table(name = "artifactversion")
@Entity
public class ArtifactVersion {
    @Id
    @GeneratedValue
    private long versionId;
    
    private String version = "";

    private OffsetDateTime createdOn = OffsetDateTime.now();

    @ManyToOne
    private ArtifactProduct artifact;

    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public ArtifactProduct getArtifact() {
        return artifact;
    }

    public void setArtifact(ArtifactProduct artifact) {
        this.artifact = artifact;
    }
    
    

}

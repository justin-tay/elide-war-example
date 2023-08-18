package example.security.checks;

import java.util.Optional;

import com.yahoo.elide.Elide;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;

import example.models.ArtifactGroup;
import jakarta.inject.Inject;

/**
 * {@link OperationCheck} for {@link ArtifactGroup}.
 */
@SecurityCheck(GroupOwnerIsUserCheck.GROUP_OWNER_IS_USER)
public class GroupOwnerIsUserCheck extends OperationCheck<ArtifactGroup> {
    public static final String GROUP_OWNER_IS_USER = "Group Owner Is User";

    @Inject
    Elide elide;

    @Override
    public boolean ok(ArtifactGroup object, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
        return true;
    }
}

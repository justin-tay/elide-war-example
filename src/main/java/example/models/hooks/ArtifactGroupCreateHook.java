package example.models.hooks;

import java.security.Principal;
import java.util.Optional;

import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;

import example.models.ArtifactGroup;
import example.security.SecurityContext;
import jakarta.inject.Inject;

/**
 * {@link LifeCycleHook} to set the owner.
 * <p>
 * This is a PRESECURITY hook as it needs to set the owner before the read check
 * is performed.
 * 
 * @see example.security.checks.GroupOwnerIsUserCheck
 */
public class ArtifactGroupCreateHook implements LifeCycleHook<ArtifactGroup> {
    @Inject
    private SecurityContext securityContext;
    
    private String getName() {
        Principal principal = securityContext.getCallerPrincipal();
        return principal != null ? principal.getName() : null;
    }

    @Override
    public void execute(Operation operation, TransactionPhase phase, ArtifactGroup elideEntity,
            RequestScope requestScope, Optional<ChangeSpec> changes) {
        String name = getName();
        elideEntity.setOwner(name);
    }
}

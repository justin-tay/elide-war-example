package example.security.checks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.Path;
import com.yahoo.elide.core.filter.expression.FilterExpression;
import com.yahoo.elide.core.filter.predicates.InPredicate;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.FilterExpressionCheck;
import com.yahoo.elide.core.security.checks.OperationCheck;
import com.yahoo.elide.core.type.Type;

import example.models.ArtifactGroup;
import example.security.SecurityContext;
import jakarta.inject.Inject;

/**
 * {@link OperationCheck} for {@link ArtifactGroup} to filter by owner.
 * <p>
 * Note that the owner needs to be populated before the check.
 * 
 * @see example.models.hooks.ArtifactGroupCreateHook
 */
@SecurityCheck(GroupOwnerIsUserCheck.GROUP_OWNER_IS_USER)
public class GroupOwnerIsUserCheck extends FilterExpressionCheck<ArtifactGroup> {
    public static final String GROUP_OWNER_IS_USER = "Group Owner Is User";
    private final Logger logger = LoggerFactory.getLogger(GroupOwnerIsUserCheck.class);

    @Inject
    private SecurityContext securityContext;

    @Override
    public FilterExpression getFilterExpression(Type<?> entityClass, RequestScope requestScope) {
        logger.info("User [{}]", securityContext.getCallerPrincipal().getName());
        Path.PathElement creatorPath = new Path.PathElement(entityClass.getUnderlyingClass().get(), String.class,
                "owner");
        Path path = new Path(List.of(creatorPath));
        return new InPredicate(path, securityContext.getCallerPrincipal().getName());
    }
}

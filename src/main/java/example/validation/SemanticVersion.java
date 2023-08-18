package example.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import example.validation.validators.SemanticVersionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Indicates that the version string follows semantic versioning.
 * 
 * @see <a href="https://semver.org/">Semantic Versioning</a>
 */
@Constraint(validatedBy = SemanticVersionValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SemanticVersion {
    String message() default "Invalid version";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
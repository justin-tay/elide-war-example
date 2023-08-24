package example.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;

@BasicAuthenticationMechanismDefinition()
@ApplicationScoped
public class SecurityConfiguration {
}

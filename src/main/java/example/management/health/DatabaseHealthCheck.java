package example.management.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.SessionFactoryImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Database health check.
 */
@Liveness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Override
    public HealthCheckResponse call() {
        boolean status = true;
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        Dialect dialect = sessionFactoryImpl.getJdbcServices().getDialect();

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                entityManager.createNativeQuery("SELECT 1").getSingleResult();
            } catch (Exception e) {
                status = false;
            }
        }
        return HealthCheckResponse.named("db")
                .status(status)
                .withData("dialect", dialect.getClass().getSimpleName())
                .build();
    }

}

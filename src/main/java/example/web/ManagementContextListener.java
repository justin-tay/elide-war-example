package example.web;

import java.io.IOException;

import example.config.properties.ManagementProperties;
import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link ServletContextLister} for configuring the management endpoints.
 */
@WebListener
public class ManagementContextListener implements ServletContextListener {
    @Inject
    private ManagementProperties managementProperties;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (managementProperties == null) {
            managementProperties = CDI.current()
                    .getBeanManager()
                    .createInstance()
                    .select(ManagementProperties.class)
                    .get();
        }

        if (managementProperties.enabled()) {
            ServletContext servletContext = sce.getServletContext();

            if (managementProperties.health().enabled()) {
                ServletRegistration.Dynamic servletRegistration = servletContext.addServlet("HealthServlet",
                        HealthServlet.class);
                servletRegistration.addMapping(managementProperties.path() + managementProperties.health().path());
            }
        }
    }

    public static class HealthServlet extends HttpServlet {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Inject
        private SmallRyeHealthReporter reporter;

        public HealthServlet() {
            if (reporter == null) {
                reporter = CDI.current().getBeanManager().createInstance().select(SmallRyeHealthReporter.class).get();
            }
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

            SmallRyeHealth health = reporter.getHealth();
            if (health.isDown()) {
                resp.setStatus(503);
            }
            try {
                reporter.reportHealth(resp.getOutputStream(), health);
            } catch (IOException ioe) {
                resp.setStatus(500);
            }
        }
    }
}

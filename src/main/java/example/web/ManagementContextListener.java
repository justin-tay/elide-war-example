package example.web;

import java.io.IOException;

import example.config.properties.ManagementProperties;
import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
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

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) {
            response.setContentType("application/json");
            SmallRyeHealth health = reporter.getHealth();
            if (health.isDown()) {
                response.setStatus(503);
            }
            try {
                reporter.reportHealth(response.getOutputStream(), health);
            } catch (IOException ioe) {
                response.setStatus(500);
            }
        }
    }
}

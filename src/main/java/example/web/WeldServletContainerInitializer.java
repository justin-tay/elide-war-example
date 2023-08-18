package example.web;

import java.util.Set;

import org.jboss.weld.environment.servlet.WeldProvider;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * {@link ServletContainerInitializer} that explicitly sets the CDI Provider to
 * the {@link WeldProvider}.
 * <p>
 * This is only required in Jetty 12 due to issue
 * <a href="https://github.com/eclipse/jetty.project/issues/10150">10150</a> as
 * the presence of the jakarta.enterprise.cdi-api library in the Jetty classpath
 * causes the ServiceLoader not to find the WeldProvider.
 */
public class WeldServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        CDI.setCDIProvider(new WeldProvider());
    }

}

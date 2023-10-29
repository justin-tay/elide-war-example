package example.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.ElideSettings.ElideSettingsBuilder;
import com.yahoo.elide.async.models.AsyncQuery;
import com.yahoo.elide.async.resources.ExportApiEndpoint.ExportApiProperties;
import com.yahoo.elide.async.service.storageengine.FileResultStorageEngine;
import com.yahoo.elide.async.service.storageengine.ResultStorageEngine;
import com.yahoo.elide.core.TransactionRegistry;
import com.yahoo.elide.core.audit.Slf4jLogger;
import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.core.dictionary.Injector;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.core.request.route.RouteResolver;
import com.yahoo.elide.core.utils.ClassScanner;
import com.yahoo.elide.core.utils.DefaultClassScanner;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import com.yahoo.elide.datastores.jpa.PersistenceUnitInfoImpl;
import com.yahoo.elide.datastores.jpa.transaction.NonJtaTransaction;
import com.yahoo.elide.graphql.GraphQLSettings;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.jsonapi.JsonApiSettings;
import com.yahoo.elide.swagger.OpenApiBuilder;
import com.yahoo.elide.swagger.resources.ApiDocsEndpoint;
import com.yahoo.elide.swagger.resources.ApiDocsEndpoint.ApiDocsRegistration;

import example.config.properties.DataSourceProperties;
import example.config.properties.ElideProperties;
import example.config.properties.GraphQLControllerProperties;
import example.config.properties.JpaProperties;
import example.config.properties.JsonApiControllerProperties;
import example.models.ArtifactGroup;
import graphql.execution.DataFetcherExceptionHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.validation.ValidatorFactory;

/**
 * Configuration for Elide.
 */
@Singleton
public class ElideConfiguration {
	private final Logger logger = LoggerFactory.getLogger(ElideConfiguration.class);

	@Produces
	@Named("elide")
	@ApplicationScoped
	public Elide elide(ElideSettingsBuilder elideSettingsBuilder, TransactionRegistry transactionRegistry) {
		logger.info("Creating Elide");
		ElideSettings elideSettings = elideSettingsBuilder.build();
		return new Elide(elideSettings, transactionRegistry, elideSettings.getEntityDictionary().getScanner(), true);
	}

	@Produces
	@Dependent
	public ElideSettingsBuilder elideSettingsBuilder(DataStore store, EntityDictionary dictionary,
			ElideProperties elideProperties, JsonApiControllerProperties jsonApiControllerProperties,
			GraphQLControllerProperties graphqlControllerProperties, JsonApiMapper jsonApiMapper,
			ObjectMapper objectMapper) {
		ElideSettingsBuilder builder = ElideSettings.builder().dataStore(store).entityDictionary(dictionary)
				.maxPageSize(elideProperties.maxPageSize()).defaultPageSize(elideProperties.defaultPageSize())
				.objectMapper(objectMapper).auditLogger(new Slf4jLogger()).baseUrl(elideProperties.baseUrl().getValue())
				.settings(
						JsonApiSettings.builder().path(jsonApiControllerProperties.path()).jsonApiMapper(jsonApiMapper)
								.joinFilterDialect(RSQLFilterDialect.builder().dictionary(dictionary).build())
								.subqueryFilterDialect(RSQLFilterDialect.builder().dictionary(dictionary).build()))
				.settings(GraphQLSettings.builder().path(graphqlControllerProperties.path()))
				.serdes(serdes -> serdes.withISO8601Dates("yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC")));
		if (elideProperties.verboseErrors()) {
			builder = builder.verboseErrors(true);
		}

		return builder;
	}

	@Produces
	@Singleton
	public JsonApiMapper jsonApiMapper(ObjectMapper objectMapper) {
		return new JsonApiMapper(objectMapper);
	}

	@Produces
	@Singleton
	public ObjectMapper objectMapper() {
		return JsonMapper.builder().addModule(new JavaTimeModule()).build();
	}

	@Produces
	@Singleton
	public TransactionRegistry transactionRegistry() {
		return new TransactionRegistry();
	}

	@Produces
	@Singleton
	public Injector injector(BeanManager manager) {
		logger.info("Creating Injector");
		return new Injector() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void inject(Object entity) {
				AnnotatedType<?> annotatedType = manager.createAnnotatedType(entity.getClass());
				CreationalContext<Object> creationalContext = manager.createCreationalContext(null);
				InjectionTargetFactory<?> injectionTargetFactory = manager.getInjectionTargetFactory(annotatedType);
				InjectionTarget injectionTarget = injectionTargetFactory.createInjectionTarget(null);
				injectionTarget.inject(entity, creationalContext);
			}

			@Override
			public <T> T instantiate(Class<T> cls) {
				try {
					return manager.createInstance().select(cls).get();
				} catch (UnsatisfiedResolutionException e) {
					return Injector.super.instantiate(cls);
				}
			}
		};
	}

	@Produces
	@Singleton
	public EntityDictionary entityDictionary(ClassScanner scanner, Injector injector) {
		logger.info("Creating EntityDictionary");
		EntityDictionary entityDictionary = EntityDictionary.builder().scanner(scanner).injector(injector).build();
		entityDictionary.scanForSecurityChecks();
		return entityDictionary;

	}

	@Produces
	@Singleton
	public ClassScanner classScanner() {
		return new DefaultClassScanner();
	}

	@Produces
	@Singleton
	public DataStore dataStore(EntityDictionary dictionary, EntityManagerFactory entityManagerFactory) {
		logger.info("Creating DataStore");
		final Consumer<EntityManager> txCancel = em -> em.unwrap(Session.class).cancelQuery();

		DataStore store = new JpaDataStore(entityManagerFactory::createEntityManager,
				em -> new NonJtaTransaction(em, txCancel), entityManagerFactory::getMetamodel);

		store.populateEntityDictionary(dictionary);
		return store;
	}

	@Produces
	@Named("apiDocs")
	@Singleton
	public List<ApiDocsRegistration> apiDocsRegistrations(Elide elide,
			JsonApiControllerProperties jsonApiControllerProperties) {
		logger.info("Creating ApiDocsRegistrations");

		EntityDictionary dictionary = elide.getElideSettings().getEntityDictionary();
		List<ApiDocsRegistration> docs = new ArrayList<>();
		dictionary.getApiVersions().stream().forEach(apiVersion -> {
			Info info = new Info().title("Elide Service").version(apiVersion);
			OpenApiBuilder builder = new OpenApiBuilder(dictionary).apiVersion(apiVersion);
			String moduleBasePath = jsonApiControllerProperties.path();
			OpenAPI openApi = builder.build().info(info).addServersItem(new Server().url(moduleBasePath));
			docs.add(new ApiDocsEndpoint.ApiDocsRegistration("", () -> openApi, "3.0", apiVersion));
		});
		return docs;
	}

	@Produces
	@Singleton
	public EntityManagerFactory entityManagerFactory(ClassScanner classScanner,
			DataSourceProperties dataSourceProperties, JpaProperties jpaProperties, BeanManager beanManager,
			ValidatorFactory validatorFactory) {
		Properties properties = new Properties();
		properties.put(AvailableSettings.JAKARTA_JDBC_DRIVER, dataSourceProperties.driverClassName());
		properties.put(AvailableSettings.JAKARTA_JDBC_URL, dataSourceProperties.url());
		properties.put(AvailableSettings.JAKARTA_JDBC_USER, dataSourceProperties.username());
		properties.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, dataSourceProperties.password().getValue());

		if (jpaProperties.showSql()) {
			properties.put(AvailableSettings.SHOW_SQL, "true");
		}

		// org.hibernate.boot.internal.SessionFactoryOptionsBuilder
		properties.put(AvailableSettings.JAKARTA_CDI_BEAN_MANAGER, beanManager);
		properties.put(AvailableSettings.JAKARTA_VALIDATION_FACTORY, validatorFactory);

		// Register the entity listeners
		URL orm = Thread.currentThread().getContextClassLoader().getResource("/META-INF/orm.xml");
		String path = orm.toString();
		properties.put(AvailableSettings.ORM_XML_FILES, List.of(path));

		properties.putAll(jpaProperties.properties());

		final String modelPackageName = ArtifactGroup.class.getPackage().getName();
		final ClassLoader classLoader = null;

		final PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfoImpl("elide-standalone",
				Entities.combineModelEntities(classScanner, modelPackageName, false), properties, classLoader);

		return new EntityManagerFactoryBuilderImpl(new PersistenceUnitInfoDescriptor(persistenceUnitInfo),
				new HashMap<>(), classLoader).build();
	}

	@Produces
	@Singleton
	public Optional<DataFetcherExceptionHandler> dataFetcherExceptionHandler() {
		return Optional.empty();
	}

	@Produces
	@Singleton
	@Named("exportApiProperties")
	public ExportApiProperties exportApiProperties() {
		return new ExportApiProperties(null, null);
	}

	@Produces
	@Singleton
	@Named("resultStorageEngine")
	public ResultStorageEngine resultStorageEngine() {
		return new FileResultStorageEngine(null, false);
	}

	@Produces
	@Singleton
	public Optional<RouteResolver> routeResolver() {
		return Optional.empty();
	}

	/**
	 * Entities.
	 */
	public static class Entities {

		/**
		 * Combine the model entities with Async and Dynamic models.
		 *
		 * @param scanner           Class scanner
		 * @param modelPackageName  Package name
		 * @param includeAsyncModel Include Async model package Name
		 * @return All entities combined from both package.
		 */
		public static List<String> combineModelEntities(ClassScanner scanner, String modelPackageName,
				boolean includeAsyncModel) {

			List<String> modelEntities = getAllEntities(scanner, modelPackageName);

			if (includeAsyncModel) {
				modelEntities.addAll(getAllEntities(scanner, AsyncQuery.class.getPackage().getName()));
			}
			return modelEntities;
		}

		/**
		 * Get all the entities in a package.
		 *
		 * @param packageName Package name
		 * @return All entities found in package.
		 */
		public static List<String> getAllEntities(ClassScanner scanner, String packageName) {
			return scanner.getAnnotatedClasses(packageName, Entity.class).stream().map(Class::getName)
					.collect(Collectors.toList());
		}
	}
}

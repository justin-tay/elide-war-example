# Elide WAR Example

## Overview

| Component            | Implementation  | Standard
| -------------------- | --------------- | -------------------------------------
| Dependency Injection | Weld            | Contexts and Dependency Injection (CDI)
| Web Services         | Jersey          | Jakarta RESTful Web Services (JAX-RS)
| Configuration        | SmallRye Config | Eclipse MicroProfile OpenAPI

## Quick Start

### Run

#### Tomcat 10

```shell
mvn package cargo:run
```

#### Jetty 12

```shell
mvn package cargo:run -Dcargo.maven.containerId=jetty12x
```

### Debug

This will add `-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y`.

#### Tomcat 10

```shell
mvn package cargo:run -Dcargo.debug=true
```

#### Jetty 12

```shell
mvn package cargo:run -Dcargo.maven.containerId=jetty12x -Dcargo.debug=true
```

### Test

#### Tomcat 10

```shell
mvn verify
```

#### Jetty 12

```shell
mvn verify -Dcargo.maven.containerId=jetty12x
```

## Development

### Eclipse

* Select `Window > Show View > Other...`
* Choose `Server > Servers`
* Select `No servers are available. Click this link to create a new server...`
* Choose `Apache > Tomcat v10.1 Server`
* Select `Download and Install...`
* Choose `Folder` to install
* Select `Next`
* Add `elide-war-example`
* Select `Finish`

## Configuration

The application uses SmallRye Config for YAML configuration. The configuration file can be found in `src/main/resources/META-INF/microprofile-config.yaml`.

## Validation

The application uses Hibernate Validator for Bean Validation. This also uses Hibernate Validator CDI which exposes a `jakarta.validation.ValidatorFactory` which is configured on Hibernate with the setting `AvailableSettings.JAKARTA_VALIDATION_FACTORY`. This allows the use of `@Inject` in `jakarta.validation.ConstraintValidator` implementations.

## GraphQL

### Mutation

* URL: `POST` `http://localhost:8080/api/graphql`
* Content-Type: `application/json`
* Accept: `application/json`

```
mutation UpsertGroup {
  group(op: UPSERT, data: {groupId: "com.yahoo.elide", description: "Elide"}) {
    edges {
      node {
        groupId
        description
        products(
          op: UPSERT
          data: {productId: "elide-core", description: "Elide Core", name: "Elide Core"}
        ) {
          edges {
            node {
              productId
              description
              name
              versions(op: UPSERT, data: {version: "7.0.0"}) {
                edges {
                  node {
                    versionId
                    version
                    createdOn
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

### Query

* URL: `POST` `http://localhost:8080/api/graphql`
* Content-Type: `application/json`
* Accept: `application/json`

```
query QueryGroup {
  group {
    edges {
      node {
        groupId
        description
        products {
          edges {
            node {
              productId
              description
              name
              versions {
                edges {
                  node {
                    versionId
                    version
                    createdOn
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

### Subscription

* URL: `ws://localhost:8080/subscription`

```
subscription {
  group(topic: ADDED) {
    groupId
    description
  }
}
```

## JSON-API

### Atomic Operations

#### Add

* URL: `POST` `http://localhost:8080/api/operations`
* Content-Type: `application/vnd.api+json; ext="https://jsonapi.org/ext/atomic"`
* Accept: `application/vnd.api+json; ext="https://jsonapi.org/ext/atomic"`

```
{
  "atomic:operations": [
    {
      "op": "add",
      "data": {
        "type": "group",
        "id": "io.swagger.core.v3",
        "attributes": {
          "description": "Swagger"
        }
      }
    },
    {
      "op": "add",
      "href": "/group/io.swagger.core.v3/products",
      "data": {
        "type": "product",
        "id": "swagger-core",
        "attributes": {
          "description": "Swagger Core"
        }
      }
    },
    {
      "op": "add",
      "href": "/group/io.swagger.core.v3/products/swagger-core/versions",
      "data": {
        "type": "version",
        "lid": "dfb0e20c-cea9-4233-9e7d-32ba15596bdf",
        "attributes": {
          "version": "2.2.15"
        }
      }
    },
    {
      "op": "add",
      "href": "/group/io.swagger.core.v3/products/swagger-core/versions",
      "data": {
        "type": "version",
        "lid": "b6fce0b4-2034-4146-a6a6-d6e7777165b3",
        "attributes": {
          "version": "2.1.13"
        }
      }
    }
  ]
}
```

#### Query
* URL: `GET` `http://localhost:8080/api/group?include=products,products.versions`
* Accept: `application/vnd.api+json`

#### Remove
* URL: `POST` `http://localhost:8080/api/operations`
* Content-Type: `application/vnd.api+json; ext="https://jsonapi.org/ext/atomic"`
* Accept: `application/vnd.api+json; ext="https://jsonapi.org/ext/atomic"`

```
{
  "atomic:operations": [
    {
      "op": "remove",
      "href": "/group/io.swagger.core.v3/products/swagger-core"
    },
    {
      "op": "remove",
      "ref": {
        "type": "group",
        "id": "io.swagger.core.v3"
      }
    }
  ]
}
```

## Docker

1. Build the image
   ```shell
   docker build -t elide/elide-war-example .
   ```

2. Run the container
   ```shell
   docker run -p 80:8080 -d elide/elide-war-example
   ```

3. The application should be running on port 80
   ```
   http://localhost/
   ```

## Build

To build:

```shell
mvn clean package
```

## Deploy

### Tomcat 10

The war should be deployed to `CATALINA_HOME/webapps`.

### Jetty 12

The `JETTY_BASE` needs to be set up with the following with the war deployed at `JETTY_BASE/webapp`.

```shell
cd $JETTY_BASE
java -jar $JETTY_HOME/start.jar --add-modules=http,ee10-deploy,ee10-annotations,ee10-websocket-jakarta,ee10-cdi-decorate
```

Currently Jetty 12 requires a custom `ServletContainerInitializer` to explicitly set the CDI Provider to the `WeldProvider` due to the following issue https://github.com/eclipse/jetty.project/issues/10150 as the presence of the jakarta.enterprise.cdi-api library in the Jetty classpath causes the ServiceLoader not to find the WeldProvider. This is implemented in `WeldServletContainerInitializer`.

### Jetty 11

The `JETTY_BASE` needs to be set up with the following with the war deployed at `JETTY_BASE/webapp`.

```shell
cd $JETTY_BASE
java -jar $JETTY_HOME/start.jar --add-modules=http,deploy,annotations,websocket-jakarta,cdi-decorate
```

## Starting

### Tomcat 10

```shell
cd $CATALINA_HOME
catalina start
```

### Jetty 12

The server can be started with the following.
```shell
cd $JETTY_BASE
java -jar $JETTY_HOME/start.jar
```

### Jetty 11

The server can be started with the following.
```shell
cd $JETTY_BASE
java -jar $JETTY_HOME/start.jar
```

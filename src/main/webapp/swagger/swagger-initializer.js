/*
 * Source code adapted from:
 * https://github.com/swagger-api/swagger-ui/blob/v5.1.3/dist/swagger-initializer.js
 */

window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/swagger'));
  window.ui = SwaggerUIBundle({
    url: `${contextPath}/api-docs`,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};

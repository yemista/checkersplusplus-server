package com.checklersplusplus.server.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("websocket")
@Configuration
public class TomcatHttpConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
    	TomcatServletWebServerFactory server = new TomcatServletWebServerFactory();
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(8081);
        server.addAdditionalTomcatConnectors(connector);
        return server;
    }
}

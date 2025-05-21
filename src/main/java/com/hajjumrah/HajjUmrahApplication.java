package com.hajjumrah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class HajjUmrahApplication {
    public static void main(String[] args) {
        SpringApplication.run(HajjUmrahApplication.class, args);
    }

    @Component
    public static class NetworkAddressLogger {
        private final Environment environment;

        public NetworkAddressLogger(Environment environment) {
            this.environment = environment;
        }

        @EventListener
        public void onApplicationStarted(ApplicationStartedEvent event) {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                String port = environment.getProperty("server.port", "8080");
                String contextPath = environment.getProperty("server.servlet.context-path", "");
                
                System.out.println("\n=================================================");
                System.out.println("Application is running! Access URLs:");
                System.out.println("Local:      http://localhost:" + port + contextPath);
                System.out.println("External:   http://" + ip + ":" + port + contextPath);
                System.out.println("=================================================\n");
            } catch (UnknownHostException e) {
                System.out.println("Could not determine network address");
            }
        }
    }
} 
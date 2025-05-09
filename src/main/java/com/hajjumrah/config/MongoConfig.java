package com.hajjumrah.config;

import com.hajjumrah.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class MongoConfig {
    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Autowired
    private MongoMappingContext mongoMappingContext;

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter converter) {
        try {
            logger.info("Initializing MongoDB connection with URI: {}", mongoUri);
            MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);
            initIndices(mongoTemplate);
            logger.info("MongoDB connection initialized successfully");
            return mongoTemplate;
        } catch (Exception e) {
            logger.error("Failed to initialize MongoDB connection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MongoDB connection", e);
        }
    }

    private void initIndices(MongoTemplate mongoTemplate) {
        try {
            logger.info("Creating MongoDB indices");
            IndexOperations indexOps = mongoTemplate.indexOps(User.class);
            IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
            resolver.resolveIndexFor(User.class).forEach(indexOps::ensureIndex);
            logger.info("MongoDB indices created successfully");
        } catch (Exception e) {
            logger.error("Error creating MongoDB indices: {}", e.getMessage(), e);
            // Don't throw the exception to allow the application to start even if index creation fails
        }
    }
} 
package com.akarsha.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() throws URISyntaxException {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl != null && dbUrl.startsWith("postgres")) {
            try {
                String withoutScheme = dbUrl.substring(dbUrl.indexOf("://") + 3);
                int atIndex = withoutScheme.lastIndexOf('@');
                
                String credentials = withoutScheme.substring(0, atIndex);
                String hostPortDb = withoutScheme.substring(atIndex + 1);
                
                String username = credentials.contains(":") ? credentials.substring(0, credentials.indexOf(':')) : credentials;
                String password = credentials.contains(":") ? credentials.substring(credentials.indexOf(':') + 1) : "";
                
                String jdbcUrl = "jdbc:postgresql://" + hostPortDb;

                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } catch (Exception e) {
                // fallback below
            }
        }

        // Fallback to local or PG variables
        String pgHost = System.getenv("PGHOST");
        String pgPort = System.getenv("PGPORT");
        String pgDb = System.getenv("PGDATABASE");
        
        String url = (pgHost != null) ? 
            "jdbc:postgresql://" + pgHost + ":" + (pgPort != null ? pgPort : "5432") + "/" + (pgDb != null ? pgDb : "railway") : 
            "jdbc:postgresql://localhost:5435/akarsha";
            
        return DataSourceBuilder.create()
                .url(url)
                .username(System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres")
                .password(System.getenv("PGPASSWORD") != null ? System.getenv("PGPASSWORD") : "postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}

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
            URI dbUri = new URI(dbUrl);
            String username = dbUri.getUserInfo() != null ? dbUri.getUserInfo().split(":")[0] : "postgres";
            String password = dbUri.getUserInfo() != null && dbUri.getUserInfo().contains(":") ? dbUri.getUserInfo().split(":")[1] : "";
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
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

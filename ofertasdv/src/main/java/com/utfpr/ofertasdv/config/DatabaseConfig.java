package com.utfpr.ofertasdv.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.*;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${app.db.create-if-missing:false}")
    private boolean createIfMissing;

    @Value("${app.db.admin.url:}")
    private String adminUrl;

    @Value("${app.db.admin.username:}")
    private String adminUsername;

    @Value("${app.db.admin.password:}")
    private String adminPassword;

    @Value("${spring.datasource.url}")
    private String appUrl;

    @Value("${spring.datasource.username}")
    private String appUsername;

    @Value("${spring.datasource.password}")
    private String appPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbName = extractDbName(appUrl);

        if (createIfMissing && isPostgresUrl(appUrl) && hasAdminCreds()) {
            try {
                ensureDatabaseExists(dbName);
            } catch (Exception e) {
                // Do not prevent app startup; just warn.
                log.warn("Skipping auto-create of database \"{}\": {}", dbName, e.getMessage());
            }
        } else {
            log.debug("Auto-create skipped (flag={}, postgresUrl={}, hasAdminCreds={})",
                    createIfMissing, isPostgresUrl(appUrl), hasAdminCreds());
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(appUrl);
        ds.setUsername(appUsername);
        ds.setPassword(appPassword);
        if (!driverClassName.isBlank()) {
            ds.setDriverClassName(driverClassName);
        }
        return ds;
    }

    private boolean hasAdminCreds() {
        return !adminUrl.isBlank() && !adminUsername.isBlank();
    }

    private boolean isPostgresUrl(String url) {
        return url != null && url.toLowerCase().startsWith("jdbc:postgresql:");
    }

    private void ensureDatabaseExists(String dbName) {
        // Basic safety: avoid executing CREATE with suspicious names
        if (!dbName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Refusing to auto-create database with invalid name: " + dbName);
        }

        DriverManager.setLoginTimeout(5);
        try (Connection conn = DriverManager.getConnection(adminUrl, adminUsername, adminPassword)) {
            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, dbName);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            if (!exists) {
                log.info("Creating PostgreSQL database \"{}\" ...", dbName);
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("CREATE DATABASE \"" + dbName + "\" OWNER \"" + appUsername + "\"");
                } catch (SQLException ownerEx) {
                    log.warn("CREATE DATABASE with OWNER failed ({}). Retrying without OWNER ...", ownerEx.getMessage());
                    try (Statement st2 = conn.createStatement()) {
                        st2.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                    }
                }
            } else {
                log.debug("Database \"{}\" already exists. Skipping creation.", dbName);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to ensure database exists: " + dbName, e);
        }
    }

    private static String extractDbName(String jdbcUrl) {
        String noParams = jdbcUrl.split("\\?")[0];
        int slash = noParams.lastIndexOf('/');
        if (slash < 0 || slash == noParams.length() - 1) {
            throw new IllegalArgumentException("Invalid JDBC URL, cannot extract database name: " + jdbcUrl);
        }
        return noParams.substring(slash + 1);
    }
}

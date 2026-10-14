package com.x.hhqs.joa.alarm.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
public class FlywayManualConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayManualConfig.class);

    private static final Pattern MARIADB_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+(-\\d+)*.*-MariaDB.*");

    @Bean(initMethod = "migrate")
    public Flyway flyway(
            @Value("${joa-alarm.datasource.url}") String url,
            @Value("${joa-alarm.datasource.username}") String user,
            @Value("${joa-alarm.datasource.password}") String password,
            @Value("${joa-alarm.datasource.schema}") String schema
    ) {
        hack();
        return Flyway.configure()
                .dataSource(url, user, password)
                .schemas(schema)
                .table("joa_alarm_schema_history")
                .locations("classpath:db/migration/")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .placeholders(Map.of("joa-alarm-schema", schema))
                .load();
    }

    void hack() {
        try {
            Class<?> mysqlDatabaseClass = Class.forName("org.flywaydb.database.mysql.MySQLDatabase");

            Field field = mysqlDatabaseClass.getDeclaredField("MARIADB_VERSION_PATTERN");
            field.setAccessible(true);

            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            long fieldOffset = unsafe.staticFieldOffset(field);
            unsafe.putObject(unsafe.staticFieldBase(field), fieldOffset, MARIADB_VERSION_PATTERN);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
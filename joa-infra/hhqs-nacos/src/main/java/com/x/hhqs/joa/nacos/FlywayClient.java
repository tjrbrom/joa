package com.x.hhqs.joa.nacos;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateOutput;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Unsafe;

public class FlywayClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(FlywayClient.class);

	private static final Pattern MARIADB_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+(-\\d+)*.*-MariaDB.*");

	private static final String DEFAULT_CONFIG_FILE = "/home/nacos/conf/flyway.properties";

	private Map<String, String> valueMap = new HashMap<>();

	public boolean migrate() throws Exception {
		String file = valueMap.getOrDefault("FLYWAY_CONFIG_FILE", DEFAULT_CONFIG_FILE);
		Map<String, String> map = loadConfigurations(file);

		String dbHost = map.get("db.host");
		String dbName = map.get("db.name");
		String dbPort = map.get("db.port");
		String param = map.get("db.param");
		String user = map.get("flyway.user");
		String password = map.get("flyway.password");

		String url = String.format("jdbc:mysql://%s:%s", dbHost, dbPort);
		String flywayUrl = url + "/" + dbName;

		if (!StringUtils.isBlank(param)) {
			url = url + "?" + param;
			flywayUrl = flywayUrl + "?" + param;
		}
		map.put("flyway.url", flywayUrl);

		createDatabaseIfNotExists(url, dbName, user, password);

		hack();
		Flyway flyway = Flyway.configure()
				.configuration(map)
				.load();

		MigrateResult result = flyway.migrate();
		printMigrateResult(result);

		return result.success;
	}

	private void hack() {
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

	void printMigrateResult(MigrateResult result) {
		System.out.println("Migration Success: " + result.success);// NOSONAR
		System.out.println("Migrations Executed: " + result.migrationsExecuted);// NOSONAR
		System.out.println("Migrations State: " + result.success);// NOSONAR
		System.out.println("Migrations FlywayVersion: " + result.flywayVersion);// NOSONAR
		System.out.println("Migrations Database: " + result.database);// NOSONAR

		if (result.migrations != null && !result.migrations.isEmpty()) {
			System.out.println("Executed Migrations:");// NOSONAR
			for (MigrateOutput migration : result.migrations) {
				System.out.println("  Version: " + migration.version);// NOSONAR
				System.out.println("  Description: " + migration.description);// NOSONAR
				System.out.println("  Type: " + migration.type);// NOSONAR
				System.out.println("  Category: " + migration.category);// NOSONAR
				System.out.println("  Execution Time: " + migration.executionTime + "ms");// NOSONAR
				System.out.println("----------");// NOSONAR
			}
		}
	}

	Map<String, String> propertiesToMap(Properties properties) {
		var substitutor = new StringSubstitutor(getValueMap());

		Map<String, String> props = new HashMap<>();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			props.put(entry.getKey().toString(), substitutor.replace(entry.getValue().toString()));
		}
		return props;
	}

	Map<String, String> loadConfigurations(String file) throws Exception {
		Properties properties = new Properties();

		try (FileInputStream inputStream = new FileInputStream(file)) {
			properties.load(inputStream);
		}

		return propertiesToMap(properties);
	}

	void createDatabaseIfNotExists(String url, String dbName, String user, String password) throws Exception {
		try (Connection connection = getDbConnection(url, user, password);
				Statement statement = connection.createStatement()) {

			String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + dbName +
					" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
			int result = statement.executeUpdate(createDatabaseQuery);
			if (result == 0) {
				LOGGER.info("Database {} already exists.", dbName);
				return;
			}
			LOGGER.info("Database {} created.", dbName);
		}
	}

	Connection getDbConnection(String dbUrl, String user, String password) throws Exception {
		return DriverManager.getConnection(dbUrl, user, password);
	}

	public Map<String, String> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<String, String> valueMap) {
		this.valueMap = valueMap;
	}
}

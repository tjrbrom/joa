package com.x.hhqs.joa.nacos;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(DbUtils.class);

  public static void main(String[] args) {
    try {
      DbUtils util = new DbUtils();
      if (!util.migrate()) {
        throw new Exception("Migration failed.");
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      System.exit(1);
    }
  }

  boolean migrate() throws Exception {
    FlywayClient client = getFlywayClient();
    client.setValueMap(loadEnvMap());
    return client.migrate();
  }

  FlywayClient getFlywayClient() {
    return new FlywayClient();
  }

  static Map<String, String> loadEnvMap() {
    Map<String, String> map = new HashMap<>();
    map.putAll(System.getenv());
    return map;
  }

}

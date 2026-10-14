CREATE DATABASE IF NOT EXISTS `${joa-alarm-schema}` DEFAULT CHARACTER SET = utf8mb4;

USE `${joa-alarm-schema}`;

DROP TABLE IF EXISTS `AlarmDefinition`;

CREATE TABLE `AlarmDefinition` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `alarm_reporter` int(11) NOT NULL,
   `ns_module_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
   `alarm_type` varchar(255) COLLATE utf8_bin NOT NULL,
   `alarm_type_id` varchar(255) COLLATE utf8_bin NOT NULL,
   `alarm_category` varchar(255) DEFAULT NULL,
   `description` varchar(1000) COLLATE utf8_bin DEFAULT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `alarmDefinitionKey` (`alarm_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


DROP TABLE IF EXISTS `joa_alarm`;

CREATE TABLE `joa_alarm` (
     `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
     `alarm_seq` BIGINT(20) DEFAULT NULL,
     `channel_index` INT(11) DEFAULT NULL,
     `alarm_id` VARCHAR(255) DEFAULT NULL,
     `ne_name` VARCHAR(128) DEFAULT NULL,
     `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
     `json_value` LONGTEXT COLLATE utf8_bin DEFAULT NULL,
     PRIMARY KEY (`id`),
     INDEX `idx_create_time` (`create_time`),
     INDEX `idx_channel_index_id` (`channel_index`, `id`),
     INDEX `idx_alarm_seq_channel_index` (`alarm_seq`, `channel_index`),
     INDEX `idx_channel_index_create_time` (`channel_index`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
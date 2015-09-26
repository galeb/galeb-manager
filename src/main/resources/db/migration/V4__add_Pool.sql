DROP TABLE IF EXISTS `pool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pool` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL DEFAULT -1,
  `global` bit(1) DEFAULT 0,
  `balancepolicy_id` bigint(20) DEFAULT NULL,
  `environment_id` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_pool` (`name`),
  KEY `FK_pool_balancepolicy` (`balancepolicy_id`),
  KEY `FK_pool_environment` (`environment_id`),
  KEY `FK_pool_project` (`project_id`),
  CONSTRAINT `FK_pool_balancepolicy` FOREIGN KEY (`balancepolicy_id`) REFERENCES `balancepolicy` (`id`),
  CONSTRAINT `FK_pool_environment` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_pool_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `pool_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pool_properties` (
  `pool` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`pool`,`properties_key`),
  CONSTRAINT `FK_pool_properties_pool_id` FOREIGN KEY (`pool`) REFERENCES `pool` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


ALTER TABLE `target` DROP FOREIGN KEY `FK_target_parent`;
ALTER TABLE `target` ADD CONSTRAINT `FK_target_parent` FOREIGN KEY (`parent_id`) REFERENCES `pool` (`id`);

ALTER TABLE `rule` DROP FOREIGN KEY `FK_rule_target`;
ALTER TABLE `rule` DROP COLUMN `target_id`;
ALTER TABLE `rule` ADD COLUMN pool_id bigint(20) NOT NULL AFTER `ruletype_id`;
ALTER TABLE `rule` ADD CONSTRAINT `FK_rule_pool` FOREIGN KEY (`pool_id`) REFERENCES `pool` (`id`);


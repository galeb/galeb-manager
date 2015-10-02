ALTER TABLE rule DROP COLUMN `rule_default`;
ALTER TABLE rule DROP COLUMN `rule_order`;

ALTER TABLE virtualhost ADD COLUMN `rule_default_id` bigint(20) DEFAULT NULL;
ALTER TABLE virtualhost ADD CONSTRAINT `FK_virtualhost_rule_default_id` FOREIGN KEY (`rule_default_id`) REFERENCES `rule` (`id`);

DROP TABLE IF EXISTS `virtual_host_rules_ordered`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_host_rules_ordered` (
  `virtual_host` bigint(20) NOT NULL,
  `rules_ordered` integer NOT NULL,
  `rules_ordered_key` bigint(20) NOT NULL,
  PRIMARY KEY (`virtual_host`, `rules_ordered_key`),
  CONSTRAINT `FK_virtual_host_rules_ordered_rule_id` FOREIGN KEY (`virtual_host`) REFERENCES `virtualhost` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


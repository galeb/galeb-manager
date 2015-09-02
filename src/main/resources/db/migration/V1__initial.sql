-- MySQL dump 10.13  Distrib 5.6.25, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: galeb
-- ------------------------------------------------------
-- Server version	5.6.25-0ubuntu0.15.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_account` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_properties`
--

DROP TABLE IF EXISTS `account_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_properties` (
  `account` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`account`,`properties_key`),
  CONSTRAINT `FK_4xg9qchhwpc3r23gm5sxmxnab` FOREIGN KEY (`account`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_properties`
--

LOCK TABLES `account_properties` WRITE;
/*!40000 ALTER TABLE `account_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_roles`
--

DROP TABLE IF EXISTS `account_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_roles` (
  `account` bigint(20) NOT NULL,
  `roles` varchar(255) DEFAULT NULL,
  KEY `FK_avjbyhyjd6nnh52cr6ij2pujp` (`account`),
  CONSTRAINT `FK_avjbyhyjd6nnh52cr6ij2pujp` FOREIGN KEY (`account`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_roles`
--

LOCK TABLES `account_roles` WRITE;
/*!40000 ALTER TABLE `account_roles` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_teams`
--

DROP TABLE IF EXISTS `account_teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_teams` (
  `team_id` bigint(20) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  PRIMARY KEY (`team_id`,`account_id`),
  KEY `FK_m4ye5wq8c6uaj62iittg0kjnc` (`account_id`),
  CONSTRAINT `FK_m4ye5wq8c6uaj62iittg0kjnc` FOREIGN KEY (`account_id`) REFERENCES `team` (`id`),
  CONSTRAINT `FK_snaq2liyp3pnvwus6cgrxb32k` FOREIGN KEY (`team_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_teams`
--

LOCK TABLES `account_teams` WRITE;
/*!40000 ALTER TABLE `account_teams` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balance_policy`
--

DROP TABLE IF EXISTS `balance_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `balance_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `balancepolicytype_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_balancepolicytype` (`name`),
  KEY `FK_balancepolicy_balancepolicytype` (`balancepolicytype_id`),
  CONSTRAINT `FK_balancepolicy_balancepolicytype` FOREIGN KEY (`balancepolicytype_id`) REFERENCES `balance_policy_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balance_policy`
--

LOCK TABLES `balance_policy` WRITE;
/*!40000 ALTER TABLE `balance_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `balance_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balance_policy_properties`
--

DROP TABLE IF EXISTS `balance_policy_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `balance_policy_properties` (
  `balance_policy` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`balance_policy`,`properties_key`),
  CONSTRAINT `FK_af41du505aqvx8ifde9mvtcu` FOREIGN KEY (`balance_policy`) REFERENCES `balance_policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balance_policy_properties`
--

LOCK TABLES `balance_policy_properties` WRITE;
/*!40000 ALTER TABLE `balance_policy_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `balance_policy_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balance_policy_type`
--

DROP TABLE IF EXISTS `balance_policy_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `balance_policy_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_balancepolicytype` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balance_policy_type`
--

LOCK TABLES `balance_policy_type` WRITE;
/*!40000 ALTER TABLE `balance_policy_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `balance_policy_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balance_policy_type_properties`
--

DROP TABLE IF EXISTS `balance_policy_type_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `balance_policy_type_properties` (
  `balance_policy_type` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`balance_policy_type`,`properties_key`),
  CONSTRAINT `FK_fv0680hutb6905ipt9omwun1n` FOREIGN KEY (`balance_policy_type`) REFERENCES `balance_policy_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balance_policy_type_properties`
--

LOCK TABLES `balance_policy_type_properties` WRITE;
/*!40000 ALTER TABLE `balance_policy_type_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `balance_policy_type_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `environment`
--

DROP TABLE IF EXISTS `environment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `environment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_environment` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `environment`
--

LOCK TABLES `environment` WRITE;
/*!40000 ALTER TABLE `environment` DISABLE KEYS */;
/*!40000 ALTER TABLE `environment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `environment_properties`
--

DROP TABLE IF EXISTS `environment_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `environment_properties` (
  `environment` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`environment`,`properties_key`),
  CONSTRAINT `FK_ivonvausm2b3ae4j7tqo2f8q2` FOREIGN KEY (`environment`) REFERENCES `environment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `environment_properties`
--

LOCK TABLES `environment_properties` WRITE;
/*!40000 ALTER TABLE `environment_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `environment_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `farm`
--

DROP TABLE IF EXISTS `farm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `farm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `api` varchar(255) NOT NULL,
  `auto_reload` bit(1) DEFAULT NULL,
  `domain` varchar(255) NOT NULL,
  `environment` bigint(20) NOT NULL,
  `provider` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_farm` (`name`),
  KEY `FK_farm_environment` (`environment`),
  KEY `FK_farm_provider` (`provider`),
  CONSTRAINT `FK_farm_environment` FOREIGN KEY (`environment`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_farm_provider` FOREIGN KEY (`provider`) REFERENCES `provider` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `farm`
--

LOCK TABLES `farm` WRITE;
/*!40000 ALTER TABLE `farm` DISABLE KEYS */;
/*!40000 ALTER TABLE `farm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `farm_properties`
--

DROP TABLE IF EXISTS `farm_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `farm_properties` (
  `farm` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`farm`,`properties_key`),
  CONSTRAINT `FK_fvsbxi9xd2ll3g3ubk5yf6qlk` FOREIGN KEY (`farm`) REFERENCES `farm` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `farm_properties`
--

LOCK TABLES `farm_properties` WRITE;
/*!40000 ALTER TABLE `farm_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `farm_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_project` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_properties`
--

DROP TABLE IF EXISTS `project_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_properties` (
  `project` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`project`,`properties_key`),
  CONSTRAINT `FK_gn253eirumrjc14uu65lr5obc` FOREIGN KEY (`project`) REFERENCES `project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_properties`
--

LOCK TABLES `project_properties` WRITE;
/*!40000 ALTER TABLE `project_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_teams`
--

DROP TABLE IF EXISTS `project_teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_teams` (
  `team_id` bigint(20) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`team_id`,`project_id`),
  KEY `FK_8e6r729xp5tbbuu80s0tyt8ca` (`project_id`),
  CONSTRAINT `FK_35l2xiix5kpyd9amhv1bdmt8q` FOREIGN KEY (`team_id`) REFERENCES `project` (`id`),
  CONSTRAINT `FK_8e6r729xp5tbbuu80s0tyt8ca` FOREIGN KEY (`project_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_teams`
--

LOCK TABLES `project_teams` WRITE;
/*!40000 ALTER TABLE `project_teams` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `provider`
--

DROP TABLE IF EXISTS `provider`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `driver` varchar(255) DEFAULT NULL,
  `provisioning` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_provider` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `provider`
--

LOCK TABLES `provider` WRITE;
/*!40000 ALTER TABLE `provider` DISABLE KEYS */;
/*!40000 ALTER TABLE `provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `provider_properties`
--

DROP TABLE IF EXISTS `provider_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider_properties` (
  `provider` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`provider`,`properties_key`),
  CONSTRAINT `FK_f4vhaschnfyv85kbu2f1kl30e` FOREIGN KEY (`provider`) REFERENCES `provider` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `provider_properties`
--

LOCK TABLES `provider_properties` WRITE;
/*!40000 ALTER TABLE `provider_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `provider_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule`
--

DROP TABLE IF EXISTS `rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL,
  `rule_default` bit(1) DEFAULT NULL,
  `rule_order` int(11) DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `ruletype` bigint(20) NOT NULL,
  `target` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_rule` (`name`),
  KEY `FK_rule_parent` (`parent`),
  KEY `FK_rule_ruletype` (`ruletype`),
  KEY `FK_rule_target` (`target`),
  CONSTRAINT `FK_rule_parent` FOREIGN KEY (`parent`) REFERENCES `virtual_host` (`id`),
  CONSTRAINT `FK_rule_ruletype` FOREIGN KEY (`ruletype`) REFERENCES `rule_type` (`id`),
  CONSTRAINT `FK_rule_target` FOREIGN KEY (`target`) REFERENCES `target` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule`
--

LOCK TABLES `rule` WRITE;
/*!40000 ALTER TABLE `rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_properties`
--

DROP TABLE IF EXISTS `rule_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule_properties` (
  `rule` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`rule`,`properties_key`),
  CONSTRAINT `FK_hfjylajvohli734i7kwkxuh55` FOREIGN KEY (`rule`) REFERENCES `rule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_properties`
--

LOCK TABLES `rule_properties` WRITE;
/*!40000 ALTER TABLE `rule_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `rule_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_type`
--

DROP TABLE IF EXISTS `rule_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_ruletype` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_type`
--

LOCK TABLES `rule_type` WRITE;
/*!40000 ALTER TABLE `rule_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `rule_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_type_properties`
--

DROP TABLE IF EXISTS `rule_type_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule_type_properties` (
  `rule_type` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`rule_type`,`properties_key`),
  CONSTRAINT `FK_55h9kk3vn1lgha9cqkk9y9t05` FOREIGN KEY (`rule_type`) REFERENCES `rule_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_type_properties`
--

LOCK TABLES `rule_type_properties` WRITE;
/*!40000 ALTER TABLE `rule_type_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `rule_type_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target`
--

DROP TABLE IF EXISTS `target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL,
  `balancepolicy_id` bigint(20) DEFAULT NULL,
  `environment_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `targettype_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ref_name_target` (`ref`,`name`),
  KEY `FK_target_balancepolicy` (`balancepolicy_id`),
  KEY `FK_target_environment` (`environment_id`),
  KEY `FK_target_parent` (`parent_id`),
  KEY `FK_target_project` (`project_id`),
  KEY `FK_target_targettype` (`targettype_id`),
  CONSTRAINT `FK_target_balancepolicy` FOREIGN KEY (`balancepolicy_id`) REFERENCES `balance_policy` (`id`),
  CONSTRAINT `FK_target_environment` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_target_parent` FOREIGN KEY (`parent_id`) REFERENCES `target` (`id`),
  CONSTRAINT `FK_target_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`),
  CONSTRAINT `FK_target_targettype` FOREIGN KEY (`targettype_id`) REFERENCES `target_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `target`
--

LOCK TABLES `target` WRITE;
/*!40000 ALTER TABLE `target` DISABLE KEYS */;
/*!40000 ALTER TABLE `target` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target_properties`
--

DROP TABLE IF EXISTS `target_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target_properties` (
  `target` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`target`,`properties_key`),
  CONSTRAINT `FK_9f081qaobl24b36qrt7a88510` FOREIGN KEY (`target`) REFERENCES `target` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `target_properties`
--

LOCK TABLES `target_properties` WRITE;
/*!40000 ALTER TABLE `target_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `target_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target_type`
--

DROP TABLE IF EXISTS `target_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_targettype` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `target_type`
--

LOCK TABLES `target_type` WRITE;
/*!40000 ALTER TABLE `target_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `target_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target_type_properties`
--

DROP TABLE IF EXISTS `target_type_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target_type_properties` (
  `target_type` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`target_type`,`properties_key`),
  CONSTRAINT `FK_fx14ro1ve5e2dh9bfs33wpil` FOREIGN KEY (`target_type`) REFERENCES `target_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `target_type_properties`
--

LOCK TABLES `target_type_properties` WRITE;
/*!40000 ALTER TABLE `target_type_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `target_type_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_team` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team`
--

LOCK TABLES `team` WRITE;
/*!40000 ALTER TABLE `team` DISABLE KEYS */;
/*!40000 ALTER TABLE `team` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_properties`
--

DROP TABLE IF EXISTS `team_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team_properties` (
  `team` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`team`,`properties_key`),
  CONSTRAINT `FK_267af8v0swycwatnls4g0bqxk` FOREIGN KEY (`team`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_properties`
--

LOCK TABLES `team_properties` WRITE;
/*!40000 ALTER TABLE `team_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `virtual_host`
--

DROP TABLE IF EXISTS `virtual_host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_host` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `ref` varchar(255) NOT NULL,
  `status` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL,
  `environment` bigint(20) NOT NULL,
  `project` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_virtualhost` (`name`),
  KEY `FK_virtualhost_environment` (`environment`),
  KEY `FK_virtualhost_project` (`project`),
  CONSTRAINT `FK_virtualhost_environment` FOREIGN KEY (`environment`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_virtualhost_project` FOREIGN KEY (`project`) REFERENCES `project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtual_host`
--

LOCK TABLES `virtual_host` WRITE;
/*!40000 ALTER TABLE `virtual_host` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `virtual_host_aliases`
--

DROP TABLE IF EXISTS `virtual_host_aliases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_host_aliases` (
  `virtualhost_id` bigint(20) NOT NULL,
  `aliases` varchar(255) DEFAULT NULL,
  KEY `FK_6xn1g4l5me1qs5kta41fqnimg` (`virtualhost_id`),
  CONSTRAINT `FK_6xn1g4l5me1qs5kta41fqnimg` FOREIGN KEY (`virtualhost_id`) REFERENCES `virtual_host` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtual_host_aliases`
--

LOCK TABLES `virtual_host_aliases` WRITE;
/*!40000 ALTER TABLE `virtual_host_aliases` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_host_aliases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `virtual_host_properties`
--

DROP TABLE IF EXISTS `virtual_host_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_host_properties` (
  `virtual_host` bigint(20) NOT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `properties_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`virtual_host`,`properties_key`),
  CONSTRAINT `FK_l8ridicm0l2d76u0d4l7uf4y6` FOREIGN KEY (`virtual_host`) REFERENCES `virtual_host` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtual_host_properties`
--

LOCK TABLES `virtual_host_properties` WRITE;
/*!40000 ALTER TABLE `virtual_host_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_host_properties` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-09-01 11:01:08

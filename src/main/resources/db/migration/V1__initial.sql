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
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
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
  CONSTRAINT `FK_account_properties_account_id` FOREIGN KEY (`account`) REFERENCES `account` (`id`)
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
  KEY `FK_account_roles_account` (`account`),
  CONSTRAINT `FK_account_roles_account_id` FOREIGN KEY (`account`) REFERENCES `account` (`id`)
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
  `account_id` bigint(20) NOT NULL,
  `team_id` bigint(20) NOT NULL,
  PRIMARY KEY (`account_id`,`team_id`),
  KEY `FK_account_teams_team_id` (`team_id`),
  CONSTRAINT `FK_account_teams_account_id_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `FK_account_teams_team_id_team_id` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
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
  CONSTRAINT `FK_balance_policy_properties_balancepolicy_id` FOREIGN KEY (`balance_policy`) REFERENCES `balancepolicy` (`id`)
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
  CONSTRAINT `FK_balance_policy_type_properties_balancepolicytype_id` FOREIGN KEY (`balance_policy_type`) REFERENCES `balancepolicytype` (`id`)
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
-- Table structure for table `balancepolicy`
--

DROP TABLE IF EXISTS `balancepolicy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `balancepolicy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  `balancepolicytype_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_balancepolicytype` (`name`),
  KEY `FK_balancepolicy_balancepolicytype` (`balancepolicytype_id`),
  CONSTRAINT `FK_balancepolicy_balancepolicytype` FOREIGN KEY (`balancepolicytype_id`) REFERENCES `balancepolicytype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balancepolicy`
--

LOCK TABLES `balancepolicy` WRITE;
/*!40000 ALTER TABLE `balancepolicy` DISABLE KEYS */;
/*!40000 ALTER TABLE `balancepolicy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `balancepolicytype`
--

DROP TABLE IF EXISTS `balancepolicytype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `balancepolicytype` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_balancepolicytype` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `balancepolicytype`
--

LOCK TABLES `balancepolicytype` WRITE;
/*!40000 ALTER TABLE `balancepolicytype` DISABLE KEYS */;
/*!40000 ALTER TABLE `balancepolicytype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `environment`
--

DROP TABLE IF EXISTS `environment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `environment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
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
  CONSTRAINT `FK_environment_properties_environment_id` FOREIGN KEY (`environment`) REFERENCES `environment` (`id`)
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
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  `api` varchar(255) NOT NULL,
  `auto_reload` bit(1) DEFAULT NULL,
  `domain` varchar(255) NOT NULL,
  `environment_id` bigint(20) NOT NULL,
  `provider_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_farm` (`name`),
  KEY `FK_farm_environment` (`environment_id`),
  KEY `FK_farm_provider` (`provider_id`),
  CONSTRAINT `FK_farm_environment` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_farm_provider` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`id`)
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
  CONSTRAINT `FK_farm_properties_farm_id` FOREIGN KEY (`farm`) REFERENCES `farm` (`id`)
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
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
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
  CONSTRAINT `FK_project_properties_project_id` FOREIGN KEY (`project`) REFERENCES `project` (`id`)
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
  `project_id` bigint(20) NOT NULL,
  `team_id` bigint(20) NOT NULL,
  PRIMARY KEY (`project_id`,`team_id`),
  KEY `FK_project_teams_team_id` (`team_id`),
  CONSTRAINT `FK_project_teams_team_id` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
  CONSTRAINT `FK_project_teams_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
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
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
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
  CONSTRAINT `FK_provider_properties_provider_id` FOREIGN KEY (`provider`) REFERENCES `provider` (`id`)
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
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL,
  `rule_default` bit(1) DEFAULT NULL,
  `rule_order` int(11) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `ruletype_id` bigint(20) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_rule` (`name`),
  KEY `FK_rule_parent` (`parent_id`),
  KEY `FK_rule_ruletype` (`ruletype_id`),
  KEY `FK_rule_target` (`target_id`),
  CONSTRAINT `FK_rule_parent` FOREIGN KEY (`parent_id`) REFERENCES `virtualhost` (`id`),
  CONSTRAINT `FK_rule_ruletype` FOREIGN KEY (`ruletype_id`) REFERENCES `ruletype` (`id`),
  CONSTRAINT `FK_rule_target` FOREIGN KEY (`target_id`) REFERENCES `target` (`id`)
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
  CONSTRAINT `FK_rule_properties_rule_id` FOREIGN KEY (`rule`) REFERENCES `rule` (`id`)
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
  CONSTRAINT `FK_rule_type_properties_ruletype_id` FOREIGN KEY (`rule_type`) REFERENCES `ruletype` (`id`)
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
-- Table structure for table `ruletype`
--

DROP TABLE IF EXISTS `ruletype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ruletype` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_ruletype` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ruletype`
--

LOCK TABLES `ruletype` WRITE;
/*!40000 ALTER TABLE `ruletype` DISABLE KEYS */;
/*!40000 ALTER TABLE `ruletype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target`
--

DROP TABLE IF EXISTS `target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL,
  `balancepolicy_id` bigint(20) DEFAULT NULL,
  `environment_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `targettype_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ref_name_target` (`_ref`,`name`),
  KEY `FK_target_balancepolicy` (`balancepolicy_id`),
  KEY `FK_target_environment` (`environment_id`),
  KEY `FK_target_parent` (`parent_id`),
  KEY `FK_target_project` (`project_id`),
  KEY `FK_target_targettype` (`targettype_id`),
  CONSTRAINT `FK_target_balancepolicy` FOREIGN KEY (`balancepolicy_id`) REFERENCES `balancepolicy` (`id`),
  CONSTRAINT `FK_target_environment` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_target_parent` FOREIGN KEY (`parent_id`) REFERENCES `target` (`id`),
  CONSTRAINT `FK_target_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`),
  CONSTRAINT `FK_target_targettype` FOREIGN KEY (`targettype_id`) REFERENCES `targettype` (`id`)
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
  CONSTRAINT `FK_target_properties_target_id` FOREIGN KEY (`target`) REFERENCES `target` (`id`)
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
  CONSTRAINT `FK_target_type_properties_targettype_id` FOREIGN KEY (`target_type`) REFERENCES `targettype` (`id`)
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
-- Table structure for table `targettype`
--

DROP TABLE IF EXISTS `targettype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `targettype` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_targettype` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `targettype`
--

LOCK TABLES `targettype` WRITE;
/*!40000 ALTER TABLE `targettype` DISABLE KEYS */;
/*!40000 ALTER TABLE `targettype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
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
  CONSTRAINT `FK_team_properties_team_id` FOREIGN KEY (`team`) REFERENCES `team` (`id`)
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
-- Table structure for table `virtual_host_aliases`
--

DROP TABLE IF EXISTS `virtual_host_aliases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_host_aliases` (
  `virtual_host` bigint(20) NOT NULL,
  `aliases` varchar(255) DEFAULT NULL,
  KEY `FK_virtual_host_aliases_virtualhost_id` (`virtual_host`),
  CONSTRAINT `FK_virtual_host_aliases_virtualhost_id` FOREIGN KEY (`virtual_host`) REFERENCES `virtualhost` (`id`)
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
  CONSTRAINT `FK_virtual_host_properties_virtualhost_id` FOREIGN KEY (`virtual_host`) REFERENCES `virtualhost` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtual_host_properties`
--

LOCK TABLES `virtual_host_properties` WRITE;
/*!40000 ALTER TABLE `virtual_host_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_host_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `virtualhost`
--

DROP TABLE IF EXISTS `virtualhost`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtualhost` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_created_at` datetime NOT NULL,
  `_created_by` varchar(255) NOT NULL,
  `_lastmodified_at` datetime NOT NULL,
  `_lastmodified_by` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `_ref` varchar(255) NOT NULL,
  `_status` int(11) NOT NULL,
  `_version` bigint(20) DEFAULT NULL,
  `farm_id` bigint(20) NOT NULL,
  `environment_id` bigint(20) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name_virtualhost` (`name`),
  KEY `FK_virtualhost_environment` (`environment_id`),
  KEY `FK_virtualhost_project` (`project_id`),
  CONSTRAINT `FK_virtualhost_environment` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
  CONSTRAINT `FK_virtualhost_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtualhost`
--

LOCK TABLES `virtualhost` WRITE;
/*!40000 ALTER TABLE `virtualhost` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtualhost` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-09-05 11:13:15

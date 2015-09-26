
LOCK TABLES `environment` WRITE;
/*!40000 ALTER TABLE `environment` DISABLE KEYS */;
INSERT INTO `environment` (`id`, `_created_at`, `_created_by`, `_lastmodified_at`, `_lastmodified_by`, `name`, `_status`, `_version`)
                   VALUES (1,    NOW(),         'admin',       NOW(),             'admin',           'Null Environment', 1, 0);   
/*!40000 ALTER TABLE `environment` ENABLE KEYS */;

LOCK TABLES `balancepolicytype` WRITE;
/*!40000 ALTER TABLE `balancepolicytype` DISABLE KEYS */;
INSERT INTO `balancepolicytype` (`id`, `_created_at`, `_created_by`, `_lastmodified_at`, `_lastmodified_by`, `name`, `_status`, `_version`)
                   VALUES (1,    NOW(),         'admin',       NOW(),             'admin',           'Default', 1, 0);   
/*!40000 ALTER TABLE `balancepolicytype` ENABLE KEYS */;

LOCK TABLES `balancepolicy` WRITE;
/*!40000 ALTER TABLE `balancepolicy` DISABLE KEYS */;
INSERT INTO `balancepolicy` (`id`, `_created_at`, `_created_by`, `_lastmodified_at`, `_lastmodified_by`, `name`, `_status`, `_version`, `balancepolicytype_id`)
                   VALUES (1,    NOW(),         'admin',       NOW(),             'admin',           'Default', 1, 0, 1);   
/*!40000 ALTER TABLE `balancepolicy` ENABLE KEYS */;

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` (`id`, `_created_at`, `_created_by`, `_lastmodified_at`, `_lastmodified_by`, `name`, `_status`, `_version`)
                   VALUES (1,    NOW(),         'admin',       NOW(),             'admin',           'Null Project', 1, 0);   
/*!40000 ALTER TABLE `project` ENABLE KEYS */;

LOCK TABLES `pool` WRITE;
/*!40000 ALTER TABLE `pool` DISABLE KEYS */;
INSERT INTO `pool` (`id`, `_created_at`, `_created_by`, `_lastmodified_at`, `_lastmodified_by`, `name`,     `_status`, `_version`, `farm_id`, `global`, `balancepolicy_id`, `environment_id`, `project_id`, `description`)
            VALUES (1,    NOW(),         'admin',       NOW(),              'admin',            'NoParent', 1,         0,          -1,        '\0',     1,                   1,               1,          NULL);
/*!40000 ALTER TABLE `pool` ENABLE KEYS */;

UNLOCK TABLES;

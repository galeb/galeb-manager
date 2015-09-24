LOCK TABLES `target` WRITE;
/*!40000 ALTER TABLE `target` DISABLE KEYS */;
INSERT INTO `target` (`id`, `_created_at`, `_created_by`, `_lastmodified_at`, `_lastmodified_by`, `name`,     `_status`, `_version`, `farm_id`, `global`, `balancepolicy_id`, `environment_id`, `parent_id`, `project_id`, `targettype_id`, `description`)
              VALUES (1,    NOW(),         'admin',       NOW(),              'admin',            'NoParent', 1,         0,          -1,        '\0',     0,                   0,               1,            0,            3,              NULL);
/*!40000 ALTER TABLE `target` ENABLE KEYS */;
UNLOCK TABLES;

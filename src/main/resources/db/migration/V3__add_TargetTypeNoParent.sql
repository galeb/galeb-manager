LOCK TABLES `targettype` WRITE;
/*!40000 ALTER TABLE `targettype` DISABLE KEYS */;
INSERT INTO `targettype` VALUES (3, NOW(), 'admin', NOW(), 'admin', 'NoParent', 1, 0, NULL);
/*!40000 ALTER TABLE `targettype` ENABLE KEYS */;
UNLOCK TABLES;
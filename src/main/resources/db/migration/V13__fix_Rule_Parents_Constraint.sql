ALTER TABLE `rule_parents` DROP FOREIGN KEY `FK_rule_parent_id`;
ALTER TABLE `rule_parents` ADD CONSTRAINT `FK_rule_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `virtualhost` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `target` DROP FOREIGN KEY `FK_target_targettype`;
ALTER TABLE `target` DROP COLUMN `targettype_id`;
DROP TABLE `targettype`

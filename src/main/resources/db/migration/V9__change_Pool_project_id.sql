ALTER TABLE `pool` MODIFY COLUMN `project_id` bigint(20) NOT NULL;
ALTER TABLE `pool` MODIFY COLUMN `environment_id` bigint(20) NOT NULL;
ALTER TABLE `pool` MODIFY COLUMN `balancepolicy_id` bigint(20) DEFAULT 1;



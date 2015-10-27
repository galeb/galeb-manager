ALTER TABLE `virtual_host_rules_ordered` CHANGE `rules_ordered_key` `rule_id` bigint(20) NOT NULL;
ALTER TABLE `virtual_host_rules_ordered` CHANGE `rules_ordered` `rule_order` int(11) NOT NULL;

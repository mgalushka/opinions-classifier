CREATE TABLE `tweets_all` (
	`id` BIGINT(20) NOT NULL,
	`cluster_id` BIGINT(20) NULL DEFAULT NULL,
	`content_json` TEXT NULL,
	`created_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`),
	INDEX `cluster_id_index` (`cluster_id`),
	INDEX `created_timestamp_index` (`created_timestamp`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


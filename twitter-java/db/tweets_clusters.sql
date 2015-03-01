CREATE TABLE `tweets_clusters` (
	`cluster_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`best_tweet_id` BIGINT(20) NULL DEFAULT NULL,
	`is_displayed` TINYINT(4) NOT NULL DEFAULT '1',
	`cluster_run_id` BIGINT(20) NOT NULL DEFAULT '0',
	`cluster_run_timestamp` TIMESTAMP NULL DEFAULT NULL,
	`created_timestamp` TIMESTAMP NULL DEFAULT NULL,
	`updated_timestamp` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`cluster_id`),
	INDEX `latest_clusters_index` (`is_displayed`, `updated_timestamp`),
	INDEX `luster_run_index` (`cluster_run_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

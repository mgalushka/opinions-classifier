CREATE TABLE `tweets_all` (
	`id` BIGINT(20) NOT NULL,
	`content_json` TEXT NULL,
	`tweet_cleaned` TEXT NULL,
	`features` TEXT NULL,
	`excluded` TINYINT(4) NOT NULL DEFAULT '0',
	`excluded_reason` VARCHAR(256) NULL DEFAULT '0',
	`created_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`),
	INDEX `created_timestamp_index` (`created_timestamp`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

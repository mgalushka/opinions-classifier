CREATE TABLE `tweets_scheduled` (
	`id` BIGINT(20) NOT NULL,
	`account_id` BIGINT(20) NOT NULL,
	`published_id` BIGINT(20) NULL DEFAULT NULL,
	`text` TEXT NULL,
	`media` TEXT NULL,
	`original_json` TEXT NULL,
	`retweet` TINYINT(4) NULL DEFAULT NULL,
	`scheduled` TINYINT(4) NULL DEFAULT NULL,
	`published` TINYINT(4) NOT NULL DEFAULT '0',
	`status` TINYINT(4) NULL DEFAULT NUll,
	`created_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`scheduled_timestamp` TIMESTAMP NULL DEFAULT NULL,
	`published_timestamp` TIMESTAMP NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `published_id_index` (`published_id`),
	UNIQUE INDEX `text_index` (`text`(100))
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

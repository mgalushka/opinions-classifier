CREATE TABLE `tweets_published` (
	`id` BIGINT(20) NOT NULL,
	`original_json` TEXT NULL,
	`published` TEXT NULL,
	`retweet` TINYINT(4) NULL DEFAULT NULL,
	`published_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

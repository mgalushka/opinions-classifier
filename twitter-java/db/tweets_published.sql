CREATE TABLE `tweets_published` (
	`id` BIGINT NOT NULL,
	`original_json` TEXT NULL,
	`published` TEXT NULL,
	`published_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
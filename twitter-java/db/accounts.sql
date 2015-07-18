CREATE TABLE `accounts` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`account` VARCHAR(25) NULL DEFAULT NULL,
	`consumer_key` VARCHAR(25) NULL DEFAULT NULL,
	`consumer_secret` VARCHAR(50) NULL DEFAULT NULL,
	`access_token` VARCHAR(50) NULL DEFAULT NULL,
	`access_token_secret` VARCHAR(50) NULL DEFAULT NULL,
	`terms` VARCHAR(255) NULL DEFAULT NULL,
	`lang` VARCHAR(2) NULL DEFAULT NULL,
	`term_black_list` TEXT NULL DEFAULT NULL,
	`users_black_list` TEXT NULL DEFAULT NULL,
	`is_active` TINYINT(1) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `account_ind` (`account`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


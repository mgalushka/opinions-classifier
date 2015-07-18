CREATE TABLE `clusters` (
	`timestamp` BIGINT(20) UNSIGNED NOT NULL,
	`clusters_serialized` MEDIUMBLOB NOT NULL,
	PRIMARY KEY (`timestamp`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

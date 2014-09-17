CREATE DATABASE `classifier` /*!40100 COLLATE 'utf8_general_ci' */

CREATE TABLE `clusters` (
	`timestamp` BIGINT UNSIGNED NOT NULL,
	`clusters_serialized` MEDIUMBLOB NOT NULL,
	PRIMARY KEY (`timestamp`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


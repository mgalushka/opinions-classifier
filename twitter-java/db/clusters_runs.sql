CREATE TABLE `clusters_runs` (
  `run_id` BIGINT(20) NOT NULL,
  `account_id` BIGINT(20) NOT NULL,
  `tweet_id` BIGINT(20) NOT NULL,
  `cluster_id` BIGINT(20) NOT NULL,
  INDEX `run_id_key` (`run_id`),
  INDEX `cluster_id_key` (`run_id`, `cluster_id`)
)
ENGINE=InnoDB
;

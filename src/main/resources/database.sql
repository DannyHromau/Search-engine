
CREATE DATABASE IF NOT EXISTS `search_engine`;

USE search_engine;

CREATE TABLE `page_data` (
  `id` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`),
  `path` varchar(255) NOT NULL UNIQUE,
  `code` INT NOT NULL,
  `content` MEDIUMBLOB,
  `site_id` INT NOT NULL
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE `lemma` (
  `id` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`),
  `lemma` varchar(255) NOT NULL,
  `frequency` INT NOT NULL,
  `site_id` INT NOT NULL
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE `site` (
  `id` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`),
  `name` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `last_error` varchar(255),
  `status_time` DATETIME,
  `status` enum('INDEXING', 'INDEXED', 'FAILED') DEFAULT NULL
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE `page_index` (
  `id` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`),
  `lemma_id` INT NOT NULL,
  `page_data_id` INT NOT NULL,
  `ran` DOUBLE NOT NULL
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE `field` (
  `id` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`),
  `name` varchar(255) NOT NULL,
  `selector` varchar(255) NOT NULL,
  `weight` DOUBLE NOT NULL
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;
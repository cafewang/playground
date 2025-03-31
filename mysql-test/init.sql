use mysql;
create schema `user`;
CREATE TABLE `user`.`employee` (
  `id` int NOT NULL AUTO_INCREMENT,
  `no` int NOT NULL,
  `name` varchar(45) NULL default "",
  PRIMARY KEY (`id`),
  UNIQUE KEY `no_UNIQUE` (`no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('5', '105');
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('10', '110');
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('15', '115');
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('20', '120');
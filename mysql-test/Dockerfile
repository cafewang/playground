FROM mysql:8.0.41
WORKDIR /docker-entrypoint-initdb.d
RUN echo -e '\
use mysql;\n\
create schema `user`;\n\
CREATE TABLE `user`.`employee` (\n\
  `id` int NOT NULL AUTO_INCREMENT,\n\
  `no` int NOT NULL,\n\
  PRIMARY KEY (`id`),\n\
  UNIQUE KEY `no_UNIQUE` (`no`)\n\
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('5', '105');\n\
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('10', '110');\n\
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('15', '115');\n\
INSERT INTO `user`.`employee` (`id`, `no`) VALUES ('20', '120');\n\
' > init.sql
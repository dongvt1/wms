-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: 127.0.0.1    Database: wms
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `sys_check_rule`
--

DROP TABLE IF EXISTS `sys_check_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_check_rule` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyid',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'rulename',
  `rule_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ruleCode',
  `rule_json` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ruleJSON',
  `rule_description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ruledescribe',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'renewhour间',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_scr_rule_code` (`rule_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_check_rule`
--

LOCK TABLES `sys_check_rule` WRITE;
/*!40000 ALTER TABLE `sys_check_rule` DISABLE KEYS */;
INSERT INTO `sys_check_rule` VALUES ('1224980593992388610','通usecodingrule','common','[{\"digits\":1,\"pattern\":\"^[a-z|A-Z]$\",\"message\":\"No.一位只能yesCharacter母\"},{\"digits\":\"*\",\"pattern\":\"^[0-9|a-z|A-Z|_]{0,}$\",\"message\":\"只能填写数Character、size写Character母、Underline\"},{\"digits\":\"*\",\"pattern\":\"^.{3,}$\",\"message\":\"leastenter3Number of digits\"},{\"digits\":\"*\",\"pattern\":\"^.{3,12}$\",\"message\":\"mostenter12Number of digits\"}]','rule：1、首位只能yesCharacter母；2、只能填写数Character、size写Character母、Underline；3、least3Number of digits，most12Number of digits。','admin','2025-09-13 18:54:19','admin','2020-02-05 16:58:27'),('1225001845524004866','负责ofFunctiontest','test','[{\"digits\":\"*\",\"pattern\":\"^.{3,12}$\",\"message\":\"只能enter3-12位Character符\"},{\"digits\":\"3\",\"pattern\":\"^\\\\d{3}$\",\"message\":\"forward3位mustyes数Character\"},{\"digits\":\"*\",\"pattern\":\"^[^pP]*$\",\"message\":\"不能enterP\"},{\"digits\":\"4\",\"pattern\":\"^@{4}$\",\"message\":\"No.4-7位must都for @\"},{\"digits\":\"2\",\"pattern\":\"^#=$\",\"message\":\"No.8-9位mustyes #=\"},{\"digits\":\"1\",\"pattern\":\"^O$\",\"message\":\"No.10位mustfor大写ofO\"},{\"digits\":\"*\",\"pattern\":\"^.*。$\",\"message\":\"mustby。ending\"}]','Include长度校验、特殊Character符校验wait','admin','2020-02-07 11:57:31','admin','2020-02-05 18:22:54');
/*!40000 ALTER TABLE `sys_check_rule` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:31

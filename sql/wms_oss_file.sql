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
-- Table structure for table `oss_file`
--

DROP TABLE IF EXISTS `oss_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oss_file` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyid',
  `file_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'documentname',
  `url` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'documentaddress',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'CreatorLog inname',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'UpdaterLog inname',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='Oss File';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oss_file`
--

LOCK TABLES `oss_file` WRITE;
/*!40000 ALTER TABLE `oss_file` DISABLE KEYS */;
INSERT INTO `oss_file` VALUES ('1739471555639607297','LOGO-mini.png','http://minio.jeecg.com/jeecgtest/LOGO-mini_1703557309951.png','jeecg','2023-12-26 10:21:51',NULL,NULL),('1739471895508254721','儿童身high.png','http://minio.jeecg.com/jeecgtest/儿童身high_1703557391840.png','jeecg','2023-12-26 10:23:12',NULL,NULL),('1739472521256468482','Big screen.png','https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/Big screen_1703557541165.png','jeecg','2023-12-26 10:25:42',NULL,NULL),('1739472619130552321','Big screen11.png','https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/Big screen11_1703557564068.png','jeecg','2023-12-26 10:26:05',NULL,NULL);
/*!40000 ALTER TABLE `oss_file` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:34

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
-- Table structure for table `jimu_report_link`
--

DROP TABLE IF EXISTS `jimu_report_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jimu_report_link` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'primary keyid',
  `report_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Building Blocksdesign器id',
  `parameter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'parameter',
  `eject_type` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Pop-up method（0 whenforwardpage 1 new window）',
  `link_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Linkname',
  `api_method` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'askmethod0-get,1-post',
  `link_type` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Link方式(0 网络Report 1 network connection 2 chartLinkage)',
  `api_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'outside网api',
  `link_chart_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'LinkagechartofID',
  `expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'expression',
  `requirement` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'condition',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `uniq_link_reportid` (`report_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='hyperlinkConfigurationsurface';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jimu_report_link`
--

LOCK TABLES `jimu_report_link` WRITE;
/*!40000 ALTER TABLE `jimu_report_link` DISABLE KEYS */;
INSERT INTO `jimu_report_link` VALUES ('1080630642578849792','1080630641874206720','{\"main\":\"aa\",\"sub\":\"bb\",\"subReport\":[{\"mainField\":\"id\",\"subParam\":\"orderId\",\"tableIndex\":1}]}',NULL,'555',NULL,'4',NULL,NULL,NULL,NULL),('907480951604711424','907480464532770816','{\"main\":\"aa\",\"sub\":\"bb\",\"subReport\":[{\"mainField\":\"id\",\"subParam\":\"orderId\",\"tableIndex\":1}]}',NULL,'555',NULL,'4',NULL,NULL,NULL,NULL),('929546942631428096','928540173805338624','{\"main\":\"receipt\",\"sub\":\"receiptProject\",\"subReport\":[{\"mainField\":\"id\",\"subParam\":\"customId\",\"tableIndex\":1}]}',NULL,'Payment details',NULL,'4',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `jimu_report_link` ENABLE KEYS */;
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

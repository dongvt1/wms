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
-- Table structure for table `onl_auth_page`
--

DROP TABLE IF EXISTS `onl_auth_page`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_auth_page` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT ' primary key',
  `cgform_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'onlinesurfaceid',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Field名/buttoncoding',
  `type` int DEFAULT NULL COMMENT '1Field 2button',
  `control` int DEFAULT NULL COMMENT '3Editable 5visible(仅support两种statevalue3,5)',
  `page` int DEFAULT NULL COMMENT '3list 5surfaceone(仅support两种statevalue3,5)',
  `status` int DEFAULT NULL COMMENT '1efficient 0none效',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_onl_auth_page_code` (`code`) USING BTREE,
  KEY `idx_onl_auth_page_cgform_id` (`cgform_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_auth_page`
--

LOCK TABLES `onl_auth_page` WRITE;
/*!40000 ALTER TABLE `onl_auth_page` DISABLE KEYS */;
INSERT INTO `onl_auth_page` VALUES ('1580815057246892034','56870166aba54ebfacb20ba6c770bd73','order_date',1,5,3,0,'2022-10-14 14:57:34','admin',NULL,NULL),('1580815057309806593','56870166aba54ebfacb20ba6c770bd73','order_date',1,5,5,0,'2022-10-14 14:57:34','admin',NULL,NULL),('1580815057309806594','56870166aba54ebfacb20ba6c770bd73','order_date',1,3,5,0,'2022-10-14 14:57:34','admin',NULL,NULL);
/*!40000 ALTER TABLE `onl_auth_page` ENABLE KEYS */;
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

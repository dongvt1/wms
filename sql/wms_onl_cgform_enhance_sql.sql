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
-- Table structure for table `onl_cgform_enhance_sql`
--

DROP TABLE IF EXISTS `onl_cgform_enhance_sql`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_cgform_enhance_sql` (
  `ID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyID',
  `BUTTON_CODE` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'buttoncoding',
  `CGB_SQL` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'SQLcontent',
  `CGB_SQL_NAME` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Sqlname',
  `CONTENT` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Remark',
  `CGFORM_HEAD_ID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'surfaceoneID',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `idx_oces_CGFORM_HEAD_ID` (`CGFORM_HEAD_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_cgform_enhance_sql`
--

LOCK TABLES `onl_cgform_enhance_sql` WRITE;
/*!40000 ALTER TABLE `onl_cgform_enhance_sql` DISABLE KEYS */;
INSERT INTO `onl_cgform_enhance_sql` VALUES ('0ebf418bd02f486342123eaf84cd39ad','add','',NULL,'','18f064d1ef424c93ba7a16148851664f'),('5ab418a13fd0bbf30ee9dd04203f3c28','add','',NULL,'','4adec929a6594108bef5b35ee9966e9f'),('8750b93ba5332460c76c492359d7a06b','edit','',NULL,'','18f064d1ef424c93ba7a16148851664f'),('edfab059050b19328ac81e6833b5ebc2','delete','',NULL,'','18f064d1ef424c93ba7a16148851664f');
/*!40000 ALTER TABLE `onl_cgform_enhance_sql` ENABLE KEYS */;
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

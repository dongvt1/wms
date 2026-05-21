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
-- Table structure for table `onl_cgform_button`
--

DROP TABLE IF EXISTS `onl_cgform_button`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_cgform_button` (
  `ID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyID',
  `BUTTON_CODE` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'buttoncoding',
  `BUTTON_ICON` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'buttonicon',
  `BUTTON_NAME` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'buttonname',
  `BUTTON_STATUS` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'buttonstate',
  `BUTTON_STYLE` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'button样式',
  `EXP` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'expression',
  `CGFORM_HEAD_ID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'surfaceoneID',
  `OPT_TYPE` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'buttontype',
  `ORDER_NUM` int DEFAULT NULL COMMENT 'sort',
  `OPT_POSITION` varchar(3) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'button位set1side 2bottom',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `idx_ocb_CGFORM_HEAD_ID` (`CGFORM_HEAD_ID`) USING BTREE,
  KEY `idx_ocb_BUTTON_CODE` (`BUTTON_CODE`) USING BTREE,
  KEY `idx_ocb_BUTTON_STATUS` (`BUTTON_STATUS`) USING BTREE,
  KEY `idx_ocb_ORDER_NUM` (`ORDER_NUM`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='Onlinesurfaceone自definitionbutton';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_cgform_button`
--

LOCK TABLES `onl_cgform_button` WRITE;
/*!40000 ALTER TABLE `onl_cgform_button` DISABLE KEYS */;
INSERT INTO `onl_cgform_button` VALUES ('007de67711d08337f7c246b6cfe306ae','jihuo',NULL,'activation','1','link',NULL,'9ab817fd4c2e4e7ba6652c4fa46af389','js',2,'2'),('108a564643763de3f4c81bc2deb463df','bt1',NULL,'activation','1','button',NULL,'05a3a30dada7411c9109306aa4117068','js',NULL,'2'),('5173e0b138c808f03d17d08ec4e66f3a','hangjihuo',NULL,'OKactivation','0','link',NULL,'9ab817fd4c2e4e7ba6652c4fa46af389','js',NULL,'2'),('7c140322fb6b1da7a5daed8b6edc0fb7','tjbpm',NULL,'提交process','1','link',NULL,'05a3a30dada7411c9109306aa4117068','js',NULL,'2'),('a45bc1c6fba96be6b0c91ffcdd6b54aa','genereate_person_config','icon-edit','generateConfiguration','1','link',NULL,'e2faf977fdaf4b25a524f58c2441a51c','js',NULL,'2'),('cc1d12de57a1a41d3986ed6d13e3ac11','Linkbuttontest','icon-edit','自definitionlink','1','link',NULL,'d35109c3632c4952a19ecc094943dd71','js',NULL,'2'),('e2a339b9fdb4091bee98408c233ab36d','zuofei',NULL,'void','1','form',NULL,'05a3a30dada7411c9109306aa4117068','js',NULL,'2'),('e95e84b749761b574a9cc0967c06c2a9','test1',NULL,'弹出surfaceone','1','button',NULL,'9ab817fd4c2e4e7ba6652c4fa46af389','js',1,'2'),('ebcc48ef0bde4433a6faf940a5e170c1','buttonbuttontest','icon-edit','自definitionbutton','1','button',NULL,'d35109c3632c4952a19ecc094943dd71','js',NULL,'2');
/*!40000 ALTER TABLE `onl_cgform_button` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:33

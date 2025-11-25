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
-- Table structure for table `onl_cgreport_head`
--

DROP TABLE IF EXISTS `onl_cgreport_head`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_cgreport_head` (
  `id` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `code` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'Reportcoding',
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'Report名Character',
  `cgr_sql` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'ReportSQL',
  `return_val_field` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'returnvalueField',
  `return_txt_field` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'returntextField',
  `return_type` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '1' COMMENT 'returntype，one选or多选',
  `db_source` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'dynamicdata源',
  `content` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'describe',
  `tenant_id` int DEFAULT '0' COMMENT 'tenantID',
  `low_app_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '关联ofapplicationID',
  `update_time` datetime DEFAULT NULL COMMENT '修changehour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修changepeopleid',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creatorid',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `index_onlinereport_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_cgreport_head`
--

LOCK TABLES `onl_cgreport_head` WRITE;
/*!40000 ALTER TABLE `onl_cgreport_head` DISABLE KEYS */;
INSERT INTO `onl_cgreport_head` VALUES ('1256627801873821698','report002','统计Log in每dayLog in次数','select DATE_FORMAT(create_time,  \'%Y-%m-%d\') as date,count(*) as num from sys_log group by DATE_FORMAT(create_time, \'%Y-%m-%d\')',NULL,NULL,'1',NULL,NULL,0,NULL,'2022-10-13 16:47:57','admin','2020-05-03 00:53:10','admin'),('1260179852088135681','tj_user_report','统一efficientsystemuse户','select id,realname,username,sex,birthday,avatar,phone,email from sys_user',NULL,NULL,'1',NULL,NULL,0,NULL,'2023-10-17 16:25:56','admin','2020-05-12 20:07:44','admin'),('1590154651759259649','withparamreport','带parameterReport','select * from demo where sex = \'${sex}\'',NULL,NULL,'1',NULL,NULL,0,NULL,'2024-01-03 11:08:34','admin','2022-11-09 09:29:47','admin'),('1705487386450534402','seelog','Checkday志','select * from sys_log',NULL,NULL,'1','local_mysql',NULL,0,NULL,'2025-09-13 17:15:34','admin','2023-09-23 15:40:54','admin'),('6c7f59741c814347905a938f06ee003c','report_user','统计onlineuse户','select realname,username,sex,birthday,avatar,phone,email from sys_user',NULL,NULL,'1',NULL,NULL,0,NULL,'2022-10-25 11:41:18','admin','2019-03-25 11:20:45','admin'),('87b55a515d3441b6b98e48e5b35474a6','demo','Report Demo','select * from demo',NULL,NULL,'1',NULL,NULL,0,NULL,'2020-05-03 01:14:35','admin','2019-03-12 11:25:16','admin');
/*!40000 ALTER TABLE `onl_cgreport_head` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:35

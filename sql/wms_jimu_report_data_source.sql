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
-- Table structure for table `jimu_report_data_source`
--

DROP TABLE IF EXISTS `jimu_report_data_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jimu_report_data_source` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'data源name',
  `report_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Report_id',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'coding',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Remark',
  `db_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Database type',
  `db_driver` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '驱动kind',
  `db_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'data源address',
  `db_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'use户名',
  `db_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'password',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `connect_times` int DEFAULT '0' COMMENT 'Number of failed connections',
  `tenant_id` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '多tenantlogo',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'type(report:Report;drag:Dashboard)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_jmdatasource_report_id` (`report_id`) USING BTREE,
  KEY `idx_jmdatasource_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jimu_report_data_source`
--

LOCK TABLES `jimu_report_data_source` WRITE;
/*!40000 ALTER TABLE `jimu_report_data_source` DISABLE KEYS */;
INSERT INTO `jimu_report_data_source` VALUES ('1324261983692902402','jeewx','1324261770294071296','',NULL,'MYSQL','com.mysql.jdbc.Driver','jdbc:mysql://127.0.0.1:3306/jeewx-boot?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8','root','root','jeecg','2020-11-05 16:07:15',NULL,'2020-11-05 16:07:15',0,NULL,'report'),('26d21fe4f27920d2f56abc8d90a8e527','oracle','1308645288868712448','',NULL,'ORACLE','oracle.jdbc.OracleDriver','jdbc:oracle:thin:@192.168.1.199:1521:helowin','jeecgbootbpm','jeecg196283','admin','2021-01-05 19:26:24',NULL,'2021-01-05 19:26:24',1,NULL,'report'),('707437208002265088','MySQL5.7',NULL,NULL,NULL,'MYSQL5.7','com.mysql.cj.jdbc.Driver','jdbc:mysql://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai','root','@JimuReportSMy3f94QGFM=','admin','2022-07-14 13:49:44','admin','2025-06-05 15:51:54',0,NULL,'drag'),('8f90daf47d15d35ca6cf420748b8b9ba','localhost','1316944968992034816','',NULL,'MYSQL5.7','com.mysql.cj.jdbc.Driver','jdbc:mysql://127.0.0.1:3306/jeecg-boot?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8','root','root','admin','2021-01-13 14:34:00',NULL,'2021-01-13 14:34:00',0,NULL,'report');
/*!40000 ALTER TABLE `jimu_report_data_source` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:32

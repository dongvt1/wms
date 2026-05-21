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
-- Table structure for table `sys_tenant_pack`
--

DROP TABLE IF EXISTS `sys_tenant_pack`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant_pack` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'primary keyid',
  `tenant_id` int DEFAULT NULL COMMENT 'tenantid',
  `pack_name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'product包名',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'turn onstate(0 Not turned on 1turn on)',
  `remarks` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Remark',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` date DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` date DEFAULT NULL COMMENT 'renewhour间',
  `pack_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'coding,defaultAdd toof三indivualadministratorneedset upcoding',
  `pack_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'custom' COMMENT 'product包type(default defaultproduct包 custom 自definitionproduct包)',
  `iz_sysn` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自动point配给use户(0no 1yes)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx__stp_tenant_id_pack_code` (`tenant_id`,`pack_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='tenantproduct包';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_tenant_pack`
--

LOCK TABLES `sys_tenant_pack` WRITE;
/*!40000 ALTER TABLE `sys_tenant_pack` DISABLE KEYS */;
INSERT INTO `sys_tenant_pack` VALUES ('1714517098074152962',0,'defaultset餐','1',NULL,'ceshi','2023-10-18','admin','2025-08-12',NULL,'default',NULL),('1955184115322142722',1000,'defaultset餐','1',NULL,'ceshi','2023-10-18','admin','2025-08-12',NULL,'custom',NULL),('1955187901394534401',1000,'upgradeenterprise业set餐','1',NULL,'admin','2025-08-12','admin','2025-08-12',NULL,'custom',NULL),('1955222289125720066',1001,'defaultset餐','1',NULL,'ceshi','2023-10-18','admin','2025-08-12',NULL,'custom',NULL);
/*!40000 ALTER TABLE `sys_tenant_pack` ENABLE KEYS */;
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

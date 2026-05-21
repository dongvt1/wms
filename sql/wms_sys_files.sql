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
-- Table structure for table `sys_files`
--

DROP TABLE IF EXISTS `sys_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_files` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyid',
  `file_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'documentname',
  `url` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'documentaddress',
  `file_type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'documenttype（folder:document夹 excel:excel doc:word ppt:ppt image:picture  archive:otherdocument video:video pdf:pdf）',
  `store_type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'documentuploadtype(temp/Local upload(临hourdocument) manage/knowledge base)',
  `parent_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'parentid',
  `tenant_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'tenantid',
  `file_size` double(13,2) DEFAULT NULL COMMENT 'documentsize（kb）',
  `iz_folder` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesnodocument夹(1：yes  0：no)',
  `iz_root_folder` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesnofor1级document夹，allowfornull (1：yes )',
  `iz_star` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesno标星(1：yes  0：no)',
  `down_count` int DEFAULT NULL COMMENT 'Down载次数',
  `read_count` int DEFAULT NULL COMMENT 'Reading times',
  `share_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'shareLink',
  `share_perms` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'sharePermissions(1.关闭share 2.allow所have联系peopleCheck 3.allow任何peopleCheck)',
  `enable_down` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesnoallowDown载(1：yes  0：no)',
  `enable_updat` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesnoallow修change(1：yes  0：no)',
  `del_flag` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'deletestate(0-normal,1-deleteto回收站)',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'CreatorLog inname',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'UpdaterLog inname',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_tenant_id` (`tenant_id`) USING BTREE,
  KEY `index_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='knowledge base-document管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_files`
--

LOCK TABLES `sys_files` WRITE;
/*!40000 ALTER TABLE `sys_files` DISABLE KEYS */;
INSERT INTO `sys_files` VALUES ('1580814573568143361','11.jpg','comment/11_1665730539114.jpg','image','temp',NULL,NULL,10956.00,'0','0','0',NULL,NULL,NULL,NULL,NULL,NULL,'0','admin','2022-10-14 14:55:39',NULL,NULL),('1584493984691740674','jeecg-bootloopholes.pdf','comment/jeecg-bootloopholes_1666607779077.pdf','pdf','temp',NULL,NULL,842789.00,'0','0','0',NULL,NULL,NULL,NULL,NULL,NULL,'0','admin','2022-10-24 18:36:19',NULL,NULL),('1714455459928985601','Lowcode平tower.png','comment/Lowcode平tower_1697593009343.png','image','temp',NULL,'1000',1249631.00,'0','0','0',NULL,NULL,NULL,NULL,NULL,NULL,'0','admin','2023-10-18 09:36:49',NULL,NULL);
/*!40000 ALTER TABLE `sys_files` ENABLE KEYS */;
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

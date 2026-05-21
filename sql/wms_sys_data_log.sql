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
-- Table structure for table `sys_data_log`
--

DROP TABLE IF EXISTS `sys_data_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_data_log` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'id',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'CreatorLog inname',
  `create_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator真实name',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'UpdaterLog inname',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `data_table` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'table name',
  `data_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'dataID',
  `data_content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'datacontent',
  `data_version` int DEFAULT NULL COMMENT 'VersionNumber',
  `type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT 'json' COMMENT 'type',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sdl_data_table_id` (`data_table`,`data_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_data_log`
--

LOCK TABLES `sys_data_log` WRITE;
/*!40000 ALTER TABLE `sys_data_log` DISABLE KEYS */;
INSERT INTO `sys_data_log` VALUES ('1942113821121011714','admin','administrator','2025-07-07 14:49:52',NULL,NULL,'test_order_main','1833472350097121281','子surface[Orderproduct明细]：修change了1条data',1,'comment'),('1943500714282205185',NULL,NULL,'2025-07-11 10:40:53',NULL,NULL,'test_note','1943500714139598850',' record created',1,'comment'),('1950132464605356035','admin','administrator','2025-07-29 17:53:06',NULL,NULL,'test_note','1943500714139598850','  Willnamefor【area】ofFieldcontent null 修changefor 140311；    Willnamefor【use户名】ofFieldcontent ceshi 修changefor zhangsan；    Willnamefor【Reason for leave】ofFieldcontent null 修changefor <p>through过上述讨论, 我们不得不面right一indivual非常尴尬of事实, 那就yes, It seems like this, generally来讲, 我们都must务必慎重of考虑考虑. 就我indivualpeople来说, Random nonsenseright我of意义, It must be said that it is very important.&nbsp;<br>eachpeople都不得不面right这些question. exist面right这种questionhour, Random nonsense, 发生了meetinglike何, 不发生又meetinglike何. Summarizeof来说,&nbsp;<br>这种事实right本people来说意义重大, 相信right这indivual世界也yeshave一定意义of.The so-called random nonsense, 关键yesxneed</p>；    Willnamefor【Birthday】ofFieldcontent null 修changefor 2025-07-29；    Willnamefor【gender】ofFieldcontent null 修changefor null；    Willnamefor【age】ofFieldcontent 11 修changefor 0',1,'comment'),('1950132495949389825','admin','administrator','2025-07-29 17:53:13',NULL,NULL,'test_note','1943500714139598850','  Willnamefor【use户名】ofFieldcontent zhangsan 修changefor admin',1,'comment'),('1950375804898873345','admin','administrator','2025-07-30 10:00:03',NULL,NULL,'test_order_product','1732300515406647298','  Willnamefor【价grid】ofFieldcontent 3.0 修changefor 3000.；    Willnamefor【producttype】ofFieldcontent null 修changefor null；    Willnamefor【quantity】ofFieldcontent 3 修changefor 10；    Willnamefor【product名Character】ofFieldcontent 3 修changefor applecell phone',1,'comment'),('1950801683948924929',NULL,NULL,'2025-07-31 14:12:20',NULL,NULL,'test_note','1943500714139598850','  Willnamefor【age】ofFieldcontent 0 修changefor 1212',1,'comment'),('1950801716647718913',NULL,NULL,'2025-07-31 14:12:28',NULL,NULL,'test_note','1923203898831777793','  Willnamefor【use户名】ofFieldcontent admin 修changefor jeecg',1,'comment'),('1966815580124278785','admin','administrator','2025-09-13 18:45:51',NULL,NULL,'test_note','1966815579977478145',' record created',1,'comment'),('1966815600902860801','admin','administrator','2025-09-13 18:45:56',NULL,NULL,'test_note','1966815579977478145','  Willnamefor【age】ofFieldcontent 0 修changefor 11',1,'comment');
/*!40000 ALTER TABLE `sys_data_log` ENABLE KEYS */;
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

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
-- Table structure for table `test_note`
--

DROP TABLE IF EXISTS `test_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_note` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'primary key',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Department',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'use户名',
  `age` int DEFAULT NULL COMMENT 'age',
  `sex` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'gender',
  `birthday` datetime DEFAULT NULL COMMENT 'Birthday',
  `contents` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Reason for leave',
  `year` date DEFAULT NULL COMMENT 'Year',
  `sheng` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'area',
  `month` date DEFAULT NULL COMMENT 'moon',
  `begin_time` date DEFAULT NULL COMMENT 'starthour间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_note`
--

LOCK TABLES `test_note` WRITE;
/*!40000 ALTER TABLE `test_note` DISABLE KEYS */;
INSERT INTO `test_note` VALUES ('1257876639515222017','admin','2020-05-06 11:35:35',NULL,NULL,'A01','不agree',20,'1','2020-05-06 00:00:00','999',NULL,NULL,NULL,NULL),('1304309860578455553','admin','2020-09-11 14:44:38','admin','2025-06-25 17:51:19','A01','zhangsan',18,'1','2020-09-11 00:00:00','<p>2223333</p>',NULL,'',NULL,NULL),('1923203898831777793','admin','2025-05-16 10:28:35','admin','2025-07-31 14:12:28','A01','jeecg',10,'2',NULL,'',NULL,'',NULL,NULL),('1943500714139598850','admin','2025-07-11 10:40:53','admin','2025-07-31 14:12:20','A01','admin',1212,'1','2025-07-29 00:00:00','<p>through过上述讨论, 我们不得不面right一indivual非常尴尬of事实, 那就yes, It seems like this, generally来讲, 我们都must务必慎重of考虑考虑. 就我indivualpeople来说, Random nonsenseright我of意义, It must be said that it is very important.&nbsp;<br>eachpeople都不得不面right这些question. exist面right这种questionhour, Random nonsense, 发生了meetinglike何, 不发生又meetinglike何. Summarizeof来说,&nbsp;<br>这种事实right本people来说意义重大, 相信right这indivual世界也yeshave一定意义of.The so-called random nonsense, 关键yesxneed</p>',NULL,'140311',NULL,NULL),('1966815579977478145','admin','2025-09-13 18:45:50','admin','2025-09-13 18:45:55','A01','ceshi',11,'1','2025-09-19 00:00:00','',NULL,'',NULL,NULL);
/*!40000 ALTER TABLE `test_note` ENABLE KEYS */;
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

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
-- Table structure for table `demo`
--

DROP TABLE IF EXISTS `demo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo` (
  `id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyID',
  `name` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Name',
  `key_word` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'keywords',
  `punch_time` datetime DEFAULT NULL COMMENT '打卡hour间',
  `salary_money` decimal(10,3) DEFAULT NULL COMMENT 'salary',
  `bonus_money` double(10,2) DEFAULT NULL COMMENT 'bonus',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'gender {male:1,female:2}',
  `age` int DEFAULT NULL COMMENT 'age',
  `birthday` date DEFAULT NULL COMMENT 'Birthday',
  `email` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Mail',
  `content` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Profile',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修changepeople',
  `update_time` datetime DEFAULT NULL COMMENT '修changehour间',
  `sys_org_code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Departmentcoding',
  `tenant_id` int DEFAULT '0',
  `update_count` int DEFAULT NULL COMMENT 'Optimistic lock test',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo`
--

LOCK TABLES `demo` WRITE;
/*!40000 ALTER TABLE `demo` DISABLE KEYS */;
INSERT INTO `demo` VALUES ('1353563050407936002','little Red Riding Hood4','44','2021-01-26 12:39:04',NULL,NULL,'1',22,'2021-01-25',NULL,NULL,'admin','2021-01-25 12:39:14','admin','2022-11-09 11:20:46','A01',0,NULL),('1400734875399024641','名Character',NULL,'2022-09-08 10:56:30',33.000,NULL,'2',23,'2022-09-30','111@333.com','333','admin','2021-06-04 16:43:13','admin','2022-11-09 11:20:43','A01',0,NULL),('1586651771328786433','Sun Yifei','222',NULL,6000.000,NULL,'1',1,'2022-10-30',NULL,NULL,'admin','2022-10-30 17:30:36',NULL,NULL,'A01',1,NULL),('1586651850919899137','Long Jiahao',NULL,'2022-10-08 17:30:46',5000.000,NULL,'1',1,'2022-10-30','111@1.com',NULL,'admin','2022-10-30 17:30:54',NULL,NULL,'A01',1,NULL),('1586651922650886146','Long Jianlin',NULL,'2022-10-24 17:30:58',9000111.000,NULL,'1',1,'2022-10-30','2@1.com',NULL,'admin','2022-10-30 17:31:12','admin','2024-06-20 18:22:30','A01',1,NULL),('1589491272526827521','Single tableExample',NULL,NULL,NULL,NULL,'1',1,NULL,NULL,NULL,'admin','2022-11-07 13:33:45',NULL,NULL,'A01',0,NULL),('1590178491193339906','2323',NULL,'2022-11-04 11:04:38',NULL,NULL,'1',1,'2022-11-24',NULL,NULL,'admin','2022-11-09 11:04:31','admin','2023-03-04 22:38:55','A01',0,NULL);
/*!40000 ALTER TABLE `demo` ENABLE KEYS */;
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

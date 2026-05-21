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
-- Table structure for table `jeecg_order_main`
--

DROP TABLE IF EXISTS `jeecg_order_main`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeecg_order_main` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary key',
  `order_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'OrderNumber',
  `ctype` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Ordertype',
  `order_date` datetime DEFAULT NULL COMMENT 'order date',
  `order_money` double(10,3) DEFAULT NULL COMMENT 'Order amount',
  `content` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'OrderRemark',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修changepeople',
  `update_time` datetime DEFAULT NULL COMMENT '修changehour间',
  `bpm_status` varchar(3) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'processstate',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeecg_order_main`
--

LOCK TABLES `jeecg_order_main` WRITE;
/*!40000 ALTER TABLE `jeecg_order_main` DISABLE KEYS */;
INSERT INTO `jeecg_order_main` VALUES ('163e2efcbc6d7d54eb3f8a137da8a75a','B100',NULL,NULL,3000.000,NULL,'jeecg-boot','2019-03-29 18:43:59',NULL,NULL,NULL),('3a867ebf2cebce9bae3f79676d8d86f3','importB100','2222',NULL,3000.000,NULL,'jeecg-boot','2019-03-29 18:43:59','admin','2019-04-08 17:35:13',NULL),('4cba137333127e8e31df7ad168cc3732','Qingdao orderA0001','2','2019-04-03 10:56:07',NULL,NULL,'admin','2019-04-03 10:56:11','admin','2022-09-22 10:55:39',NULL),('54e739bef5b67569c963c38da52581ec','NC911','1','2019-02-18 09:58:51',40.000,NULL,'admin','2019-02-18 09:58:47','admin','2019-02-18 09:58:59',NULL),('6a719071a29927a14f19482f8693d69a','c100',NULL,NULL,5000.000,NULL,'jeecg-boot','2019-03-29 18:43:59',NULL,NULL,NULL),('8ab1186410a65118c4d746eb085d3bed','import400','1','2019-02-18 09:58:51',40.000,NULL,'admin','2019-02-18 09:58:47','admin','2019-02-18 09:58:59',NULL),('9a57c850e4f68cf94ef7d8585dbaf7e6','halou001','1','2019-04-04 17:30:32',500.000,NULL,'admin','2019-04-04 17:30:41','admin','2022-09-22 10:56:25',NULL),('a2cce75872cc8fcc47f78de9ffd378c2','importB100',NULL,NULL,3000.000,NULL,'jeecg-boot','2019-03-29 18:43:59',NULL,NULL,NULL),('b190737bd04cca8360e6f87c9ef9ec4e','B0018888','1',NULL,NULL,NULL,'admin','2019-02-15 18:39:29','admin','2020-05-02 18:15:09',NULL),('d908bfee3377e946e59220c4a4eb414a','SSSS001',NULL,NULL,599.000,NULL,'admin','2019-04-01 15:43:03','admin','2019-04-01 16:26:52',NULL),('e73434dad84ebdce2d4e0c2a2f06d8ea','import200',NULL,NULL,3000.000,NULL,'jeecg-boot','2019-03-29 18:43:59',NULL,NULL,NULL),('eb13ab35d2946a2b0cfe3452bca1e73f','BJ9980','1',NULL,90.000,NULL,'admin','2019-02-16 17:36:42','admin','2019-02-16 17:46:16',NULL),('f71f7f8930b5b6b1703d9948d189982b','BY911',NULL,'2019-04-06 19:08:39',NULL,NULL,'admin','2019-04-01 16:36:02','admin','2019-04-01 16:36:08',NULL),('f8889aaef6d1bccffd98d2889c0aafb5','A100',NULL,'2018-10-10 00:00:00',6000.000,NULL,'jeecg-boot','2019-03-29 18:43:59',NULL,NULL,NULL);
/*!40000 ALTER TABLE `jeecg_order_main` ENABLE KEYS */;
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

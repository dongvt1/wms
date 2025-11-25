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
-- Table structure for table `sys_sms`
--

DROP TABLE IF EXISTS `sys_sms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_sms` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'ID',
  `es_title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'remove息title',
  `es_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Send method：Reference enumerationMessageTypeEnum',
  `es_receiver` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '接收people',
  `es_param` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发送所需parameterJsonFormat',
  `es_content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '推送content',
  `es_send_time` datetime DEFAULT NULL COMMENT '推送hour间',
  `es_send_status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '推送state 0Not pushed 1Push successful 2Push failed -1Failure will not be sent again',
  `es_send_num` int DEFAULT NULL COMMENT 'Send times Exceed5Never send again',
  `es_result` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Push failed原因',
  `remark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Remark',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'CreatorLog inname',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'UpdaterLog inname',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ss_es_type` (`es_type`) USING BTREE,
  KEY `idx_ss_es_receiver` (`es_receiver`) USING BTREE,
  KEY `idx_ss_es_send_time` (`es_send_time`) USING BTREE,
  KEY `idx_ss_es_send_status` (`es_send_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_sms`
--

LOCK TABLES `sys_sms` WRITE;
/*!40000 ALTER TABLE `sys_sms` DISABLE KEYS */;
INSERT INTO `sys_sms` VALUES ('402880e74dc2f361014dc2f8411e0001','remove息推送test333','2','411944058@qq.com',NULL,'open三Hello，yourOrder4028d881436d514601436d521ae80165paid!','2015-06-05 17:06:01','3',NULL,NULL,'Certification失败mistakeofuse户名or者password','admin','2015-06-05 17:05:59','admin','2015-11-19 22:30:39'),('402880ea533647b00153364e74770001','发indivual问候','3','admin',NULL,'Hello','2016-03-02 00:00:00','2',NULL,NULL,NULL,'admin','2016-03-02 15:50:24','admin','2018-07-05 19:53:01'),('402880ee5a17e711015a17f3188e013f','remove息推送test333','2','411944058@qq.com',NULL,'open三Hello，yourOrder4028d881436d514601436d521ae80165paid!',NULL,'2',NULL,NULL,NULL,'admin','2017-02-07 17:41:31','admin','2017-03-10 11:37:05'),('402880f05ab649b4015ab64b9cd80012','remove息推送test333','2','411944058@qq.com',NULL,'open三Hello，yourOrder4028d881436d514601436d521ae80165paid!','2017-11-16 15:58:15','3',NULL,NULL,NULL,'admin','2017-03-10 11:38:13','admin','2017-07-31 17:24:54'),('402880f05ab7b035015ab7c4462c0004','remove息推送test333','2','411944058@qq.com',NULL,'open三Hello，yourOrder4028d881436d514601436d521ae80165paid!','2017-11-16 15:58:15','3',NULL,NULL,NULL,'admin','2017-03-10 18:29:37',NULL,NULL),('402881f3646a472b01646a4a5af00001','urge：HRApproval','3','admin',NULL,'admin，Hello！\r\n请forward待办Task办理事item！HRApproval\r\n\r\n\r\n===========================\r\n此remove息Depend onsystem发出','2018-07-05 19:53:35','2',NULL,NULL,NULL,'admin','2018-07-05 19:53:35','admin','2018-07-07 13:45:24'),('402881f3647da06c01647da43a940014','urge：HRApproval','3','admin',NULL,'admin，Hello！\r\n请forward待办Task办理事item！HRApproval\r\n\r\n\r\n===========================\r\n此remove息Depend onsystem发出','2018-07-09 14:04:32','2',NULL,NULL,NULL,'admin','2018-07-09 14:04:32','admin','2018-07-09 18:51:30');
/*!40000 ALTER TABLE `sys_sms` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:31

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
-- Table structure for table `sys_sms_template`
--

DROP TABLE IF EXISTS `sys_sms_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_sms_template` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary key',
  `template_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'templatetitle',
  `template_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'templateCODE',
  `template_type` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'templatetype：1Short message 2mail 3WeChat',
  `template_category` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '模版Classification：noticeNotices and Announcements otherother',
  `template_content` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'templatecontent',
  `template_test_json` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'templatetestjson',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'CreatorLog inname',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'UpdaterLog inname',
  `use_status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesnomakeusemiddle 1yes0no',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sst_template_code` (`template_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_sms_template`
--

LOCK TABLES `sys_sms_template` WRITE;
/*!40000 ALTER TABLE `sys_sms_template` DISABLE KEYS */;
INSERT INTO `sys_sms_template` VALUES ('1199606397416775681','System messagesnotify','sys_ts_note','2',NULL,'<h1>&nbsp; &nbsp; systemnotify</h1>\n<ul>\n<li>notifyhour间：&nbsp; ${ts_date}</li>\n<li>notifycontent：&nbsp; ${ts_content}</li>\n</ul>',NULL,'2019-11-27 16:30:27','admin','2019-11-27 19:36:50','admin',NULL),('1199615897335095298','processurge','bpm_cuiban','2',NULL,'<h1>&nbsp; &nbsp;processurge提醒</h1>\n<ul>\n<li>processname：&nbsp; ${bpm_name}</li>\n<li>urgeTask：&nbsp; ${bpm_task}</li>\n<li>urgehour间 :&nbsp; &nbsp; ${datetime}</li>\n<li>urgecontent :&nbsp; &nbsp; ${remark}</li>\n</ul>',NULL,'2019-11-27 17:08:12','admin','2019-11-27 19:36:45','admin',NULL),('1199648914107625473','process办理超hour提醒','bpm_chaoshi_tip','2',NULL,'<h1>&nbsp; &nbsp;process办理超hour提醒</h1>\n<ul>\n<li>&nbsp; &nbsp;超hour提醒information：&nbsp; &nbsp; 您have待处理of超hourTask，Please deal with it as soon as possible！</li>\n<li>&nbsp; &nbsp;超hourTasktitle：&nbsp; &nbsp; ${title}</li>\n<li>&nbsp; &nbsp;超hourTasknode：&nbsp; &nbsp; ${task}</li>\n<li>&nbsp; &nbsp;Task处理people：&nbsp; &nbsp; &nbsp; &nbsp;${user}</li>\n<li>&nbsp; &nbsp;Taskstarthour间：&nbsp; &nbsp; ${time}</li>\n</ul>',NULL,'2019-11-27 19:19:24','admin','2019-11-27 19:36:37','admin',NULL),('4028608164691b000164693108140003','urge：${taskName}','SYS001','1',NULL,'${userName}，Hello！\r\n请forward待办Task办理事item！${taskName}\r\n\r\n\r\n===========================\r\n此remove息Depend onsystem发出','{\r\n\"taskName\":\"HRApproval\",\r\n\"userName\":\"admin\"\r\n}','2018-07-05 14:46:18','admin','2018-07-05 18:31:34','admin',NULL);
/*!40000 ALTER TABLE `sys_sms_template` ENABLE KEYS */;
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

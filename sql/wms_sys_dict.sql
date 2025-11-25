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
-- Table structure for table `sys_dict`
--

DROP TABLE IF EXISTS `sys_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `dict_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'Character典name',
  `dict_code` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'Character典coding',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'describe',
  `del_flag` int DEFAULT NULL COMMENT 'deletestate',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'renewhour间',
  `type` int(1) unsigned zerofill DEFAULT '0' COMMENT 'Character典type0forstring,1fornumber',
  `tenant_id` int DEFAULT '0' COMMENT 'tenantID',
  `low_app_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'LowcodeapplicationID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sd_dict_code` (`dict_code`) USING BTREE,
  KEY `uk_sd_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict`
--

LOCK TABLES `sys_dict` WRITE;
/*!40000 ALTER TABLE `sys_dict` DISABLE KEYS */;
INSERT INTO `sys_dict` VALUES ('0b5d19e1fce4b2e6647e6b4a17760c14','noticetype','msg_category','remove息type1:Notices and Announcements2:System messages',0,'admin','2019-04-22 18:01:35',NULL,NULL,0,0,NULL),('1174509082208395266','Position level','position_rank','PositionsurfaceRankCharacter典',0,'admin','2019-09-19 10:22:41',NULL,NULL,0,0,NULL),('1174511106530525185','mechanismtype','org_category','mechanismtype 1company，2department，3post，4子company',0,'admin','2019-09-19 10:30:43',NULL,NULL,0,0,NULL),('1178295274528845826','Form permissions policy','form_perms_type','',0,'admin','2019-09-29 21:07:39','admin','2019-09-29 21:08:26',NULL,0,NULL),('1199517671259906049','Urgency','urgent_level','dayProcedure计划Urgency',0,'admin','2019-11-27 10:37:53',NULL,NULL,0,0,NULL),('1199518099888414722','dayProcedure计划type','eoa_plan_type','',0,'admin','2019-11-27 10:39:36',NULL,NULL,0,0,NULL),('1199520177767587841','Classification栏目type','eoa_cms_menu_type','',0,'admin','2019-11-27 10:47:51','admin','2019-11-27 10:49:35',0,0,NULL),('1199525215290306561','dayProcedure计划state','eoa_plan_status','',0,'admin','2019-11-27 11:07:52','admin','2019-11-27 11:10:11',0,0,NULL),('1209733563293962241','Database type','database_type','',0,'admin','2019-12-25 15:12:12',NULL,NULL,0,0,NULL),('1232913193820581889','OnlinesurfaceonebusinessClassification','ol_form_biz_type','',0,'admin','2020-02-27 14:19:46','admin','2020-02-27 14:20:23',0,0,NULL),('1242298510024429569','Reminder method','remindMode','',0,'admin','2020-03-24 11:53:40','admin','2020-03-24 12:03:22',0,0,NULL),('1250687930947620866','定hourTaskstate','quartz_status','',0,'admin','2020-04-16 15:30:14','',NULL,NULL,0,NULL),('1280401766745718786','tenantstate','tenant_status','tenantstate',0,'admin','2020-07-07 15:22:25',NULL,NULL,0,0,NULL),('1356445645198135298','switch','is_open','',0,'admin','2021-02-02 11:33:38','admin','2021-02-02 15:28:12',0,0,NULL),('1600042215909134338','所属OK业','trade','OK业',0,'admin','2022-12-06 16:19:26','admin','2022-12-06 16:20:50',0,0,NULL),('1600044537800331266','companyscale','company_size','companyscale',0,'admin','2022-12-06 16:28:40','admin','2022-12-06 16:30:23',0,0,NULL),('1606645341269299201','Rank','company_rank','companyRank',0,'admin','2022-12-24 21:37:54','admin','2022-12-24 21:38:25',0,0,NULL),('1606646440684457986','companydepartment','company_department','companydepartment',0,'admin','2022-12-24 21:42:16','admin','2024-03-18 14:21:56',0,0,NULL),('1693196536609755137','ddd','ddd',NULL,1,'admin','2023-08-20 17:41:27',NULL,NULL,0,0,NULL),('1784843187992084482','client终endtype','client_type',NULL,0,'jeecg','2024-04-29 15:12:31',NULL,NULL,0,0,NULL),('1890229208685322242','Modelsupply者','model_provider',NULL,0,'jeecg','2025-02-14 10:38:57',NULL,NULL,0,0,NULL),('1891456510739890177','Modeltype','model_type',NULL,0,'jeecg','2025-02-17 19:55:48',NULL,NULL,0,0,NULL),('1891671216561975297','knowledge basetype','airag_know_type',NULL,1,'jeecg','2025-02-18 10:08:58',NULL,NULL,0,0,NULL),('1891672414555860993','knowledge basedocumenttype','know_doc_type',NULL,0,'jeecg','2025-02-18 10:13:44',NULL,NULL,0,0,NULL),('1894701158027554818','AIApplication type','ai_app_type',NULL,0,'jeecg','2025-02-26 18:48:53',NULL,NULL,0,0,NULL),('1934846825077878786','announcementClassification','notice_type',NULL,0,'admin','2025-06-17 13:33:25',NULL,NULL,0,0,NULL),('1937393911539384322','模版Classification','msgCategory',NULL,0,'admin','2025-06-24 14:14:38',NULL,NULL,0,0,NULL),('1939572486447292418','front page关联','relation_type',NULL,0,'admin','2025-06-30 14:31:31',NULL,NULL,0,0,NULL),('1964944899916697602','use户Position','user_position','use户Position',0,'admin','2025-09-08 14:52:26',NULL,NULL,0,0,NULL),('236e8a4baff0db8c62c00dd95632834f','synchronousWorkflowengine','activiti_sync','synchronousWorkflowengine',0,'admin','2019-05-15 15:27:33',NULL,NULL,0,0,NULL),('2e02df51611a4b9632828ab7e5338f00','Permission policy','perms_type','Permission policy',0,'admin','2019-04-26 18:26:55',NULL,NULL,0,0,NULL),('2f0320997ade5dd147c90130f7218c3e','推送kind别','msg_type','',0,'admin','2019-03-17 21:21:32','admin','2019-03-26 19:57:45',0,0,NULL),('3486f32803bb953e7155dab3513dc68b','deletestate','del_flag',NULL,0,'admin','2019-01-18 21:46:26','admin','2019-03-30 11:17:11',0,0,NULL),('3d9a351be3436fbefb1307d4cfb49bf2','gender','sex',NULL,0,NULL,'2019-01-04 14:56:32','admin','2019-03-30 11:28:27',1,0,NULL),('4274efc2292239b6f000b153f50823ff','全局Permission policy','global_perms_type','全局Permission policy',0,'admin','2019-05-10 17:54:05',NULL,NULL,0,0,NULL),('4c03fca6bf1f0299c381213961566349','Onlinechartexhibition示template','online_graph_display_template','Onlinechartexhibition示template',0,'admin','2019-04-12 17:28:50',NULL,NULL,0,0,NULL),('4c753b5293304e7a445fd2741b46529d','Character典state','dict_item_status',NULL,0,'admin','2020-06-18 23:18:42','admin','2019-03-30 19:33:52',1,0,NULL),('4d7fec1a7799a436d26d02325eff295e','priority','priority','priority',0,'admin','2019-03-16 17:03:34','admin','2019-04-16 17:39:23',0,0,NULL),('4e4602b3e3686f0911384e188dc7efb4','conditional rules','rule_conditions','',0,'admin','2019-04-01 10:15:03','admin','2019-04-01 10:30:47',0,0,NULL),('4f69be5f507accea8d5df5f11346181a','发送remove息type','msgType',NULL,0,'admin','2019-04-11 14:27:09',NULL,NULL,0,0,NULL),('68168534ff5065a152bfab275c2136f8','efficientnone效state','valid_status','efficientnone效state',0,'admin','2020-09-26 19:21:14','admin','2019-04-26 19:21:23',0,0,NULL),('6b78e3f59faec1a4750acff08030a79b','use户type','user_type',NULL,0,NULL,'2019-01-04 14:59:01','admin','2019-03-18 23:28:18',0,0,NULL),('72cce0989df68887546746d8f09811aa','Onlinesurfaceonetype','cgform_table_type','',0,'admin','2019-01-27 10:13:02','admin','2019-03-30 11:37:36',0,0,NULL),('78bda155fe380b1b3f175f1e88c284c6','processstate','bpm_status','processstate',0,'admin','2019-05-09 16:31:52',NULL,NULL,0,0,NULL),('83bfb33147013cc81640d5fd9eda030c','day志type','log_type',NULL,0,'admin','2019-03-18 23:22:19',NULL,NULL,1,0,NULL),('845da5006c97754728bf48b6a10f79cc','state','status',NULL,0,'admin','2019-03-18 21:45:25','admin','2019-03-18 21:58:25',0,0,NULL),('880a895c98afeca9d9ac39f29e67c13e','Operation type','operate_type','Operation type',0,'admin','2019-07-22 10:54:29',NULL,NULL,0,0,NULL),('8dfe32e2d29ea9430a988b3b558bf233','releasestate','send_status','releasestate',0,'admin','2019-04-16 17:40:42',NULL,NULL,0,0,NULL),('a7adbcd86c37f7dbc9b66945c82ef9e6','1yes0no','yn','',0,'admin','2019-05-22 19:29:29',NULL,NULL,0,0,NULL),('a9d9942bd0eccb6e89de92d130ec4c4a','remove息发送state','msgSendStatus',NULL,0,'admin','2019-04-12 18:18:17',NULL,NULL,0,0,NULL),('ac2f7c0c5c5775fcea7e2387bcb22f01','menutype','menu_type',NULL,0,'admin','2020-12-18 23:24:32','admin','2019-04-01 15:27:06',1,0,NULL),('ad7c65ba97c20a6805d5dcdf13cdaf36','onlineTtype','ceshi_online',NULL,0,'admin','2019-03-22 16:31:49','admin','2019-03-22 16:34:16',0,0,NULL),('bd1b8bc28e65d6feefefb6f3c79f42fd','Onlinechartdatatype','online_graph_data_type','Onlinechartdatatype',0,'admin','2019-04-12 17:24:24','admin','2019-04-12 17:24:57',0,0,NULL),('c36169beb12de8a71c8683ee7c28a503','departmentstate','depart_status',NULL,0,'admin','2019-03-18 21:59:51',NULL,NULL,0,0,NULL),('c5a14c75172783d72cbee6ee7f5df5d1','Onlinecharttype','online_graph_type','Onlinecharttype',0,'admin','2019-04-12 17:04:06',NULL,NULL,0,0,NULL),('d6e1152968b02d69ff358c75b48a6ee1','processtype','bpm_process_type',NULL,0,'admin','2021-02-22 19:26:54','admin','2019-03-30 18:14:44',0,0,NULL),('fc6cd58fde2e8481db10d3a1e68ce70c','use户state','user_status',NULL,0,'admin','2019-03-18 21:57:25','admin','2019-03-18 23:11:58',1,0,NULL);
/*!40000 ALTER TABLE `sys_dict` ENABLE KEYS */;
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

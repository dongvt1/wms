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
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyid',
  `username` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Log in账Number',
  `realname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实Name',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'password',
  `salt` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'md5password盐',
  `avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头picture',
  `birthday` date DEFAULT NULL COMMENT 'Birthday',
  `sex` tinyint(1) DEFAULT NULL COMMENT 'gender(0-default未知,1-male,2-female)',
  `email` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '电子mail',
  `phone` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Telephone',
  `org_code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Log inmeeting话ofmechanismcoding',
  `status` tinyint(1) DEFAULT NULL COMMENT 'gender(1-normal,2-freeze)',
  `del_flag` tinyint(1) DEFAULT NULL COMMENT 'deletestate(0-normal,1-已delete)',
  `third_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'No.三方Log inof唯一logo',
  `third_type` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'No.三方type',
  `activiti_sync` tinyint(1) DEFAULT NULL COMMENT 'synchronousWorkflowengine(1-synchronous,0-不synchronous)',
  `work_no` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '工Number，unique key',
  `telephone` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '座机Number',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'renewhour间',
  `user_identity` tinyint(1) DEFAULT NULL COMMENT '身share（1ordinary member 2Superior）',
  `depart_ids` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '负责department',
  `client_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'equipmentID',
  `login_tenant_id` int DEFAULT NULL COMMENT '上次Log inchoosetenantID',
  `bpm_status` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'processOnboardingResignstate',
  `sign_enable` tinyint(1) DEFAULT NULL COMMENT 'yesnoenableindivual性签名（0 no 1yes）',
  `sign` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'indivual性签名',
  `main_dep_post_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '主post（departmentpostid）',
  `position_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Position(Character典)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_sys_user_work_no` (`work_no`) USING BTREE,
  UNIQUE KEY `uniq_sys_user_username` (`username`) USING BTREE,
  UNIQUE KEY `uniq_sys_user_phone` (`phone`) USING BTREE,
  UNIQUE KEY `uniq_sys_user_email` (`email`) USING BTREE,
  KEY `idx_su_status` (`status`) USING BTREE,
  KEY `idx_su_del_flag` (`del_flag`) USING BTREE,
  KEY `idx_su_del_username` (`username`,`del_flag`) USING BTREE,
  KEY `idx_su_main_dep_post_id` (`main_dep_post_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='use户surface';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES ('1714471285016895490','ceshi','testuse户','a9932bb12d2cbc5a','AF4vhXUz',NULL,'2024-04-11',NULL,'winter@jeecg.org','15201111112',NULL,1,0,NULL,NULL,1,'123',NULL,'admin','2023-10-18 10:39:42','ceshi','2025-05-08 16:11:05',1,'',NULL,0,NULL,NULL,NULL,NULL,NULL),('3d464b4ea0d2491aab8a7bde74c57e95','zhangsan','open三','02ea098224c7d0d2077c14b9a3a1ed16','x5xRdeKB','https://static.jeecg.com/temp/jmlogo_1606575041993.png','2024-04-11',NULL,'111@1.com','13426411111','financedepartment',1,0,NULL,NULL,1,'0005',NULL,'admin','2020-05-14 21:26:24','admin','2024-04-26 13:25:37',1,'',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('a75d45a015c44384a04449ee80dc3503','jeecg','jeecg','eee378a1258530cb','mIgiYJow','https://static.jeecg.com/temp/国炬软pieceslogo_1606575029126.png',NULL,1,'418799587@qq.com','18611788525','A02A01',1,0,NULL,NULL,1,'00002',NULL,'admin','2019-02-13 16:02:36','admin','2023-10-18 13:51:36',1,'',NULL,1001,NULL,NULL,NULL,NULL,NULL),('e9ca23d68d884d4ebb19d07889727dae','admin','administrator','cb362cfeefbf3d8d','RCGTeGiH','https://static.jeecg.com/temp/国炬软pieceslogo_1606575029126.png','1986-02-01',1,'jeecg@163.com','18611111111','A01',1,0,NULL,NULL,1,'00001',NULL,NULL,'2019-06-21 17:54:10','admin','2025-09-13 18:23:04',2,'',NULL,1000,NULL,0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
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

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
-- Table structure for table `sys_permission_data_rule`
--

DROP TABLE IF EXISTS `sys_permission_data_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission_data_rule` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'ID',
  `permission_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'menuID',
  `rule_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'rulename',
  `rule_column` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Field',
  `rule_conditions` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'condition',
  `rule_value` varchar(300) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'rule value',
  `status` varchar(3) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Permissionsefficientstate1have0no',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '修changehour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修changepeople',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_spdr_permission_id` (`permission_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission_data_rule`
--

LOCK TABLES `sys_permission_data_rule` WRITE;
/*!40000 ALTER TABLE `sys_permission_data_rule` DISABLE KEYS */;
INSERT INTO `sys_permission_data_rule` VALUES ('1260935285157511170','4148ec82b6acd69f470bea75fe41c357','createBy','createBy','=','#{sys_user_code}','0','2020-05-14 22:09:34','jeecg','2020-05-14 22:13:52','admin'),('1260936345293012993','4148ec82b6acd69f470bea75fe41c357','age','age','>','20','1','2020-05-14 22:13:46','admin',NULL,NULL),('1260937192290762754','4148ec82b6acd69f470bea75fe41c357','sysOrgCode','sysOrgCode','RIGHT_LIKE','#{sys_org_code}','1','2020-05-14 22:17:08','admin',NULL,NULL),('32b62cb04d6c788d9d92e3ff5e66854e','8d4683aacaa997ab86b966b464360338','000','00','!=','00','1','2019-04-02 18:36:08','admin',NULL,NULL),('40283181614231d401614234fe670003','40283181614231d401614232cd1c0001','createBy','createBy','=','#{sys_user_code}','1','2018-01-29 21:57:04','admin',NULL,NULL),('4028318161424e730161424fca6f0004','4028318161424e730161424f61510002','createBy','createBy','=','#{sys_user_code}','1','2018-01-29 22:26:20','admin',NULL,NULL),('402880e6487e661a01487e732c020005','402889fb486e848101486e93a7c80014','SYS_ORG_CODE','SYS_ORG_CODE','LIKE','010201%','1','2014-09-16 20:32:30','admin',NULL,NULL),('402880e6487e661a01487e8153ee0007','402889fb486e848101486e93a7c80014','create_by','create_by','','#{SYS_USER_CODE}','1','2014-09-16 20:47:57','admin',NULL,NULL),('402880ec5ddec439015ddf9225060038','40288088481d019401481d2fcebf000d','complexrelation','','USE_SQL_RULES','name like \'%open%\' or age > 10','1',NULL,NULL,'2017-08-14 15:10:25','demo'),('402880ec5ddfdd26015ddfe3e0570011','4028ab775dca0d1b015dca3fccb60016','complexsqlConfiguration','','USE_SQL_RULES','table_name like \'%test%\' or is_tree = \'Y\'','1',NULL,NULL,'2017-08-14 16:38:55','demo'),('402880f25b1e2ac7015b1e5fdebc0012','402880f25b1e2ac7015b1e5cdc340010','只能看自己data','create_by','=','#{sys_user_code}','1','2017-03-30 16:40:51','admin',NULL,NULL),('402881875b19f141015b19f8125e0014','40288088481d019401481d2fcebf000d','可看Down属businessdata','sys_org_code','LIKE','#{sys_org_code}','1',NULL,NULL,'2017-08-14 15:04:32','demo'),('402881e45394d66901539500a4450001','402881e54df73c73014df75ab670000f','sysCompanyCode','sysCompanyCode','=','#{SYS_COMPANY_CODE}','1','2016-03-21 01:09:21','admin',NULL,NULL),('402881e45394d6690153950177cb0003','402881e54df73c73014df75ab670000f','sysOrgCode','sysOrgCode','=','#{SYS_ORG_CODE}','1','2016-03-21 01:10:15','admin',NULL,NULL),('402881e56266f43101626727aff60067','402881e56266f43101626724eb730065','Sale自己看自己ofdata','createBy','=','#{sys_user_code}','1','2018-03-27 19:11:16','admin',NULL,NULL),('402881e56266f4310162672fb1a70082','402881e56266f43101626724eb730065','Salethrough理看所haveDown级data','sysOrgCode','LIKE','#{sys_org_code}','1','2018-03-27 19:20:01','admin',NULL,NULL),('402881e56266f431016267387c9f0088','402881e56266f43101626724eb730065','只看Amountgreater than1000ofdata','money','>=','1000','1','2018-03-27 19:29:37','admin',NULL,NULL),('402881f3650de25101650dfb5a3a0010','402881e56266f4310162671d62050044','22','','USE_SQL_RULES','22','1','2018-08-06 14:45:01','admin',NULL,NULL),('402889fb486e848101486e913cd6000b','402889fb486e848101486e8e2e8b0007','userName','userName','=','admin','1','2014-09-13 18:31:25','admin',NULL,NULL),('402889fb486e848101486e98d20d0016','402889fb486e848101486e93a7c80014','title','title','=','12','1',NULL,NULL,'2014-09-13 22:18:22','scott'),('402889fe47fcb29c0147fcb6b6220001','8a8ab0b246dc81120146dc8180fe002b','12','12','>','12','1','2014-08-22 15:55:38','8a8ab0b246dc81120146dc8181950052',NULL,NULL),('4028ab775dca0d1b015dca4183530018','4028ab775dca0d1b015dca3fccb60016','table namelimit','isDbSynch','=','Y','1',NULL,NULL,'2017-08-14 16:43:45','demo'),('4028ef815595a881015595b0ccb60001','40288088481d019401481d2fcebf000d','You can only look at yourself','create_by','=','#{sys_user_code}','1',NULL,NULL,'2017-08-14 15:03:56','demo'),('4028ef81574ae99701574aed26530005','4028ef81574ae99701574aeb97bd0003','use户名','userName','!=','admin','1','2016-09-21 12:07:18','admin',NULL,NULL),('f852d85d47f224990147f2284c0c0005',NULL,'less than','test','<=','11','1','2014-08-20 14:43:52','8a8ab0b246dc81120146dc8181950052',NULL,NULL);
/*!40000 ALTER TABLE `sys_permission_data_rule` ENABLE KEYS */;
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

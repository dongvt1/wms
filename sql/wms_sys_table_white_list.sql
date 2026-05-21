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
-- Table structure for table `sys_table_white_list`
--

DROP TABLE IF EXISTS `sys_table_white_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_table_white_list` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'primary keyid',
  `table_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'allowoftable name',
  `field_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'allowofField名，多indivualuse逗Numberpoint割',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT 'state，1=enable，0=Disable',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'renewhour间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_sys_table_white_list_table_name` (`table_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='systemsurface白名one';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_table_white_list`
--

LOCK TABLES `sys_table_white_list` WRITE;
/*!40000 ALTER TABLE `sys_table_white_list` DISABLE KEYS */;
INSERT INTO `sys_table_white_list` VALUES ('1701578033271521282','sys_user','phone,work_no,id,email,realname,username','1','admin','2023-09-12 10:46:32','admin','2023-12-31 16:55:30'),('1701581935488385025','oa_officialdoc_organcode','id,organ_name','1','admin','2023-09-12 11:02:02',NULL,NULL),('1701581977733414913','demo','id,name','1','admin','2023-09-12 11:02:12',NULL,NULL),('1701582035472203777','sys_permission','id,name','1','admin','2023-09-12 11:02:26',NULL,NULL),('1701582087619985409','onl_drag_comp','id,comp_name','1','admin','2023-09-12 11:02:38',NULL,NULL),('1701582136420712450','sys_depart','id,org_code,depart_name','1','admin','2023-09-12 11:02:50','admin','2023-10-18 09:36:40'),('1701582163599802370','design_form','id,desform_name,desform_code','1','admin','2023-09-12 11:02:56',NULL,NULL),('1701582190187495426','onl_cgform_head','table_txt,table_name','1','admin','2023-09-12 11:03:03',NULL,NULL),('1701582254301626370','oa_wps_file','id,name','1','admin','2023-09-12 11:03:18',NULL,NULL),('1714453996678926338','onl_cgreport_head','code','1','admin','2023-10-18 09:31:00',NULL,NULL),('1714455418728337410','sys_category','id,name','1','admin','2023-10-18 09:36:40',NULL,NULL),('1714471625900564482','sys_position','name,id','1','ceshi','2023-10-18 10:41:04',NULL,NULL),('1769610154632491009','sys_dict','dict_code','1','admin','2024-03-18 14:21:53',NULL,NULL),('1778692300030484482','test_shoptype_tree','type_name,id','1','admin','2024-04-12 15:51:05',NULL,NULL),('1782650226206269441','sys_tenant','name,id','1','admin','2024-04-23 13:58:29',NULL,NULL),('1800712552062898178','tj_user_report','name,username','1','admin','2024-06-12 10:11:43',NULL,NULL),('1801076145102925826','sys_data_source','code,name','1','admin','2024-06-13 10:16:30',NULL,NULL),('1801097090085564420','sys_role','role_name,role_code','1','jeecg','2024-06-13 11:39:44','admin','2024-09-10 11:47:35'),('1805416360756006913','wu_liao','wul_name,id','1','admin','2024-06-25 09:42:58',NULL,NULL),('1897919397122269185','ces_shop_type','name,pid,id,has_child','1','admin','2025-03-07 15:57:01',NULL,NULL),('1907407400953659394','airag_flow','name,id','1','admin','2025-04-02 20:18:57',NULL,NULL),('1907407401083682817','airag_model','name,id','1','admin','2025-04-02 20:18:57',NULL,NULL),('1950438522834546690','sys_sms_template','template_code','1','admin','2025-07-30 14:09:16',NULL,NULL),('1966817706103730178','sys_check_rule','rule_code','1','admin','2025-09-13 18:54:17',NULL,NULL);
/*!40000 ALTER TABLE `sys_table_white_list` ENABLE KEYS */;
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

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
-- Table structure for table `onl_cgform_head`
--

DROP TABLE IF EXISTS `onl_cgform_head`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_cgform_head` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyID',
  `table_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'table name',
  `table_type` int NOT NULL COMMENT 'surfacetype: 0Single table、1main table、2Schedule',
  `table_version` int DEFAULT '1' COMMENT 'surfaceVersion',
  `table_txt` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'surfaceillustrate',
  `is_checkbox` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'yesno带checkbox',
  `is_db_synch` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'N' COMMENT 'synchronousdata库state',
  `is_page` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'yesnoPagination',
  `is_tree` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'yesnoyesTree',
  `id_sequence` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'primary keygenerate序List',
  `id_type` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'primary keytype',
  `query_mode` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Querymodel',
  `relation_type` int DEFAULT NULL COMMENT '映射relation 0one to many  1One to one',
  `sub_table_str` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '子surface',
  `tab_order_num` int DEFAULT NULL COMMENT 'Schedulesort序Number',
  `tree_parent_id_field` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Tree形surfaceonefatherid',
  `tree_id_field` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Treesurfaceprimary keyField',
  `tree_fieldname` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Tree开surfaceoneListField',
  `form_category` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'bdfl_ptbd' COMMENT 'surfaceoneClassification',
  `form_template` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'PCsurfaceonetemplate',
  `form_template_mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'surfaceonetemplate样式(moveend)',
  `scroll` int DEFAULT '0' COMMENT 'yesnohave横向滚动条',
  `copy_version` int DEFAULT NULL COMMENT '复制VersionNumber',
  `copy_type` int DEFAULT '0' COMMENT '复制surfacetype1for复制surface 0for原始surface',
  `physic_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '原始surfaceID',
  `ext_config_json` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'ExpandJSON',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修changepeople',
  `update_time` datetime DEFAULT NULL COMMENT '修changehour间',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `theme_template` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'theme template',
  `is_des_form` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'yesnousedesign器surfaceone',
  `des_form_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'design器surfaceonecoding',
  `tenant_id` int DEFAULT '0' COMMENT 'tenantID',
  `low_app_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '关联ofapplicationID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_och_cgform_head_id` (`table_name`) USING BTREE,
  KEY `idx_och_table_name` (`form_template`) USING BTREE,
  KEY `idx_och_form_template_mobile` (`form_template_mobile`) USING BTREE,
  KEY `idx_och_table_version` (`table_version`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_cgform_head`
--

LOCK TABLES `onl_cgform_head` WRITE;
/*!40000 ALTER TABLE `onl_cgform_head` DISABLE KEYS */;
INSERT INTO `onl_cgform_head` VALUES ('05a3a30dada7411c9109306aa4117068','test_note',1,27,'Leave request form@JSEnhanceExample','Y','Y','Y','N',NULL,'UUID','single',NULL,NULL,NULL,NULL,NULL,NULL,'temp','1',NULL,1,NULL,0,NULL,'{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":0,\"commentStatus\":1,\"tableFixedAction\":1,\"tableFixedActionType\":\"right\",\"formLabelLengthShow\":0,\"formLabelLength\":null,\"enableExternalLink\":0,\"externalLinkActions\":\"add,edit,detail\"}','admin','2025-09-13 18:45:32','admin','2020-05-06 11:34:31','normal','N','',0,NULL),('3d447fa919b64f6883a834036c14aa67','test_enhance_select',1,7,'Three-level linkage control','N','Y','Y','N',NULL,'UUID','single',NULL,NULL,NULL,NULL,NULL,NULL,'bdfl_include','1',NULL,0,NULL,0,NULL,'{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":\"\",\"commentStatus\":0,\"tableFixedAction\":1,\"tableFixedActionType\":\"right\",\"formLabelLengthShow\":0,\"formLabelLength\":null,\"enableExternalLink\":0,\"externalLinkActions\":\"add,edit,detail\"}','admin','2025-05-15 18:26:26','admin','2020-02-20 16:19:00','normal','N','',0,NULL),('402880e570a5d7000170a5d700f50000','test_order_product$1',1,11,'Orderproduct明细','N','N','Y','N',NULL,'UUID','single',NULL,NULL,NULL,NULL,NULL,NULL,'bdfl_include','1',NULL,0,1,1,'deea5a8ec619460c9245ba85dbc59e80',NULL,NULL,NULL,'admin','2020-03-04 21:58:16',NULL,NULL,NULL,0,NULL),('402880e5721355dd01721355dd390000','ces_order_goods$1',1,1,'Ordercommodity','Y','N','Y','N',NULL,'UUID','single',NULL,NULL,NULL,NULL,NULL,NULL,'temp','1',NULL,1,1,1,'86bf17839a904636b7ed96201b2fa6ea',NULL,NULL,NULL,'admin','2020-05-14 21:18:14','normal',NULL,NULL,0,NULL),('402881fd812267500181226750e90000','ces_shop_goods$1',1,1,'commodity','Y','N','Y','N',NULL,'UUID','single',NULL,NULL,NULL,NULL,NULL,NULL,'temp','1',NULL,1,7,1,'53a3e82b54b946c2b904f605875a275c',NULL,NULL,NULL,'admin','2022-06-02 11:13:25','normal',NULL,NULL,0,NULL),('402881fd812267500181226787d90001','test_note$1',1,2,'Leave request form@JSEnhanceExample','Y','N','Y','N',NULL,'UUID','single',NULL,NULL,NULL,NULL,NULL,NULL,'temp','1',NULL,1,12,1,'05a3a30dada7411c9109306aa4117068','{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":\"\"}','admin','2022-06-02 11:13:48','admin','2022-06-02 11:13:39','normal','N','',0,NULL),('41de7884bf9a42b7a2c5918f9f765dff','test_order_customer',3,9,'Orderclient','Y','Y','Y','N',NULL,'UUID','single',0,NULL,2,NULL,NULL,NULL,'temp','1',NULL,1,NULL,0,NULL,'{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":\"\",\"commentStatus\":0}','admin','2022-11-23 12:01:41','admin','2019-04-20 11:41:19','normal','N','',0,NULL),('56870166aba54ebfacb20ba6c770bd73','test_order_main',2,38,'testOrdermain table','Y','Y','Y','N',NULL,'UUID','single',NULL,'test_order_product,test_order_customer',NULL,NULL,NULL,NULL,'bdfl_include','2',NULL,0,NULL,0,NULL,'{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":900,\"commentStatus\":0,\"tableFixedAction\":0,\"tableFixedActionType\":\"right\"}','admin','2024-01-02 21:44:58','admin','2019-04-20 11:38:39','erp','N','',0,NULL),('997ee931515a4620bc30a9c1246429a9','test_shoptype_tree',1,2,'commodityClassification','Y','Y','Y','Y',NULL,'UUID','single',NULL,NULL,NULL,'pid','has_child','type_name','temp','1',NULL,1,NULL,0,NULL,NULL,'admin','2020-05-03 00:57:47','admin','2020-05-03 00:56:56','normal',NULL,NULL,0,NULL),('d35109c3632c4952a19ecc094943dd71','test_demo',1,39,'testuse户surface','Y','Y','Y','N',NULL,'UUID','group',NULL,NULL,NULL,NULL,NULL,NULL,'bdfl_include','1',NULL,0,NULL,0,NULL,'{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":900,\"commentStatus\":0,\"tableFixedAction\":0,\"tableFixedActionType\":\"right\"}','admin','2023-09-16 21:25:25','admin','2019-03-15 14:24:35','normal','N','',0,NULL),('deea5a8ec619460c9245ba85dbc59e80','test_order_product',3,12,'Orderproduct明细','N','Y','Y','N',NULL,'UUID','single',0,'',1,NULL,NULL,NULL,'bdfl_include','1',NULL,0,NULL,0,NULL,NULL,'admin','2022-10-29 17:13:01','admin','2019-04-20 11:41:19','normal',NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `onl_cgform_head` ENABLE KEYS */;
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

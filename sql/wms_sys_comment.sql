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
-- Table structure for table `sys_comment`
--

DROP TABLE IF EXISTS `sys_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_comment` (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `table_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'table name',
  `table_data_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'dataid',
  `from_user_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '来源use户id',
  `to_user_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发送给use户id(allowfornull)',
  `comment_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Commentid(allowfornull，不fornullhour，则forreply)',
  `comment_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Reply content',
  `create_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_table_data_id` (`table_name`,`table_data_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='systemCommentreplysurface';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_comment`
--

LOCK TABLES `sys_comment` WRITE;
/*!40000 ALTER TABLE `sys_comment` DISABLE KEYS */;
INSERT INTO `sys_comment` VALUES ('1580814554312093698','v3_hello','1580529718871674882','e9ca23d68d884d4ebb19d07889727dae','','','1212\n:open_mouth:','admin','2022-10-14 14:55:35',NULL,NULL),('1580814573433925634','v3_hello','1580529718871674882','e9ca23d68d884d4ebb19d07889727dae','','','upload了appendix','admin','2022-10-14 14:55:39',NULL,NULL),('1580814621358043137','v3_hello','1580529718871674882','e9ca23d68d884d4ebb19d07889727dae','','','What are you doing?','admin','2022-10-14 14:55:51',NULL,NULL),('1584490724803174402','v3_hello','1580529718871674882','e9ca23d68d884d4ebb19d07889727dae','','','1212:nerd_face:','admin','2022-10-24 18:23:22',NULL,NULL),('1584490998162743298','v3_hello','1580510370266238978','e9ca23d68d884d4ebb19d07889727dae','','','123123','admin','2022-10-24 18:24:27',NULL,NULL),('1584491122888761345','v3_hello','1580510370266238978','e9ca23d68d884d4ebb19d07889727dae','','','333','admin','2022-10-24 18:24:57',NULL,NULL),('1584493914588143617','v3_hello','1580529718871674882','e9ca23d68d884d4ebb19d07889727dae','','','2222','admin','2022-10-24 18:36:02',NULL,NULL),('1584493923496845313','v3_hello','1580529718871674882','e9ca23d68d884d4ebb19d07889727dae','','','333','admin','2022-10-24 18:36:04',NULL,NULL),('1584493984364584961','v3_hello','1580510370266238978','e9ca23d68d884d4ebb19d07889727dae','','','upload了appendix','admin','2022-10-24 18:36:19',NULL,NULL),('1714455459606024193','ceshi_note','1586278360710615042','e9ca23d68d884d4ebb19d07889727dae','','','upload了appendix','admin','2023-10-18 09:36:49',NULL,NULL),('1714455471815643138','ceshi_note','1586278360710615042','e9ca23d68d884d4ebb19d07889727dae','','','121','admin','2023-10-18 09:36:52',NULL,NULL),('1765261100976934914','ceshi_note','1737728721647525890','e9ca23d68d884d4ebb19d07889727dae','','','挺好of','admin','2024-03-06 14:20:18',NULL,NULL),('1765261127610765313','ceshi_note','1737728721647525890','e9ca23d68d884d4ebb19d07889727dae','','',':woozy_face:','admin','2024-03-06 14:20:24',NULL,NULL),('1800557341876895745','ceshi_aaa','1782647168684478466','e9ca23d68d884d4ebb19d07889727dae','','',':face_with_head_bandage::nauseated_face:','admin','2024-06-11 23:54:58',NULL,NULL),('1800557929826041858','ceshi_aaa','1782647168684478466','e9ca23d68d884d4ebb19d07889727dae','','',':cold_face:','admin','2024-06-11 23:57:18',NULL,NULL),('1800557935693873154','ceshi_aaa','1782647168684478466','e9ca23d68d884d4ebb19d07889727dae','','','1212','admin','2024-06-11 23:57:19',NULL,NULL),('1800557955415490562','ceshi_aaa','1782647168684478466','e9ca23d68d884d4ebb19d07889727dae','','','upload了appendix','admin','2024-06-11 23:57:24',NULL,NULL),('1800558013942808578','ceshi_aaa','1782647168684478466','e9ca23d68d884d4ebb19d07889727dae','','','upload了appendix','admin','2024-06-11 23:57:38',NULL,NULL),('1805421586141544450','aa_order','1805421421888405506','e9ca23d68d884d4ebb19d07889727dae','','','写of不错','admin','2024-06-25 10:03:44',NULL,NULL),('1805421721126830082','aa_order','1805421421888405506','e9ca23d68d884d4ebb19d07889727dae','','','upload了appendix','admin','2024-06-25 10:04:16',NULL,NULL);
/*!40000 ALTER TABLE `sys_comment` ENABLE KEYS */;
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

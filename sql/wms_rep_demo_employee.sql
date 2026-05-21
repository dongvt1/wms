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
-- Table structure for table `rep_demo_employee`
--

DROP TABLE IF EXISTS `rep_demo_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rep_demo_employee` (
  `id` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary key',
  `num` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '编Number',
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Name',
  `sex` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'gender',
  `birthday` datetime DEFAULT NULL COMMENT '出Birthday期',
  `nation` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'nationality',
  `political` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'political outlook',
  `native_place` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Birthplace',
  `height` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '身high',
  `weight` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'weight',
  `health` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'health status',
  `id_card` varchar(80) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '身share证Number',
  `education` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Educational qualifications',
  `school` varchar(80) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Graduation school',
  `major` varchar(80) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'major',
  `address` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系address',
  `zip_code` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'post code',
  `email` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Email',
  `phone` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'cell phoneNumber',
  `foreign_language` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'outside语语种',
  `foreign_language_level` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'outside语level',
  `computer_level` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'computerlevel',
  `graduation_time` datetime DEFAULT NULL COMMENT '毕业hour间',
  `arrival_time` datetime DEFAULT NULL COMMENT 'arrive职hour间',
  `positional_titles` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'job title',
  `education_experience` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'educateexperience',
  `work_experience` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'Work experience',
  `create_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT '创建hour间',
  `update_by` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '修changepeople',
  `update_time` datetime DEFAULT NULL COMMENT '修changehour间',
  `del_flag` tinyint(1) DEFAULT NULL COMMENT 'deletelogo0-normal,1-已delete',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rep_demo_employee`
--

LOCK TABLES `rep_demo_employee` WRITE;
/*!40000 ALTER TABLE `rep_demo_employee` DISABLE KEYS */;
INSERT INTO `rep_demo_employee` VALUES ('1','001','open三','male','2000-02-04 13:36:19','Han nationality','group员','Beijing','170','65','good','110101200002044853','College','Beijing科技','computer','Beijing朝Positivedistrict','1001','zhang@163.com','18011111111','English','Level three','Level three','2019-02-04 13:41:17','2020-02-04 13:41:31','project manager','2018Year9moon—2019Year7moon：BeijinglanguagecultureUniversity比较文学研究所攻读PhD学位，get比较文学PhD学位','2019Year5moon---to今 XXcompany     网络systemproject师  \n2019Year5moon---to今 XXcompany     网络systemproject师',NULL,'2020-02-04 15:18:03',NULL,NULL,NULL),('2','002','Wang Hong','female','2000-02-04 13:36:19','Han nationality','group员','Beijing','170','65','good','110101200002044853','College','Beijing科技','computer','Beijing朝Positivedistrict','1001','zhang@163.com','18011111111','English','Level three','Level three','2019-02-04 13:41:17','2020-02-04 13:41:31','project manager','2018Year9moon—2019Year7moon：BeijinglanguagecultureUniversity比较文学研究所攻读PhD学位，get比较文学PhD学位','2019Year5moon---to今 XXcompany     网络systemproject师  \n2019Year5moon---to今 XXcompany     网络systemproject师',NULL,'2020-02-04 18:39:27',NULL,NULL,NULL);
/*!40000 ALTER TABLE `rep_demo_employee` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:34

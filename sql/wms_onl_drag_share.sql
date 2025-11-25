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
-- Table structure for table `onl_drag_share`
--

DROP TABLE IF EXISTS `onl_drag_share`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_drag_share` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'primary key',
  `drag_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'onlineDashboarddesign器id',
  `preview_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Previewaddress',
  `preview_lock` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'passwordLock',
  `last_update_time` datetime DEFAULT NULL COMMENT '最backrenewhour间',
  `term_of_validity` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'efficient期(0:永久efficient，1:1sky，7:7sky)',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'yesno过期(0Not expired，1Expired)',
  `preview_lock_status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'yesnoforpasswordLock(0 no,1yes)',
  `share_token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'sharetoken',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_ods_drag_id` (`drag_id`) USING BTREE COMMENT 'Dashboardid唯一index'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='DashboardPreviewsharesurface';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_drag_share`
--

LOCK TABLES `onl_drag_share` WRITE;
/*!40000 ALTER TABLE `onl_drag_share` DISABLE KEYS */;
INSERT INTO `onl_drag_share` VALUES ('1062674826894385152','1060068114432577536','https://bootapi.jeecg.com/drag/share/view/1060068114432577536','','2025-03-20 20:17:27','0','0','0',NULL),('1062674928555925504','1060099988005638144','https://bootapi.jeecg.com/drag/share/view/1060099988005638144','','2025-03-20 20:17:37','0','0','0',NULL),('1062674948000718848','1060099975032655872','https://bootapi.jeecg.com/drag/share/view/1060099975032655872','','2025-03-20 20:17:41','0','0','0',NULL),('1062674963582558208','1060099939494318080','https://bootapi.jeecg.com/drag/share/view/1060099939494318080','','2025-03-20 20:17:45','0','0','0',NULL),('1062674981450293248','1060099927951593472','https://bootapi.jeecg.com/drag/share/view/1060099927951593472','','2025-03-20 20:17:49','0','0','0',NULL),('1062674998021988352','1060068147949260800','https://bootapi.jeecg.com/drag/share/view/1060068147949260800','','2025-03-20 20:17:53','0','0','0',NULL),('1062675012123238400','1060068100662677504','https://bootapi.jeecg.com/drag/share/view/1060068100662677504','','2025-03-20 20:17:57','0','0','0',NULL),('1062675025788280832','1060099867109019648','https://bootapi.jeecg.com/drag/share/view/1060099867109019648','','2025-03-20 20:18:00','0','0','0',NULL),('1062675046642360320','1060068138562408448','https://bootapi.jeecg.com/drag/share/view/1060068138562408448','','2025-03-20 20:18:05','0','0','0',NULL),('1062675062794625024','1060068124528267264','https://bootapi.jeecg.com/drag/share/view/1060068124528267264','','2025-03-20 20:18:09','0','0','0',NULL),('1062677638072115200','1060100026798755840','https://bootapi.jeecg.com/drag/share/view/1060100026798755840','','2025-03-20 20:28:23','0','0','0',NULL),('1062677655184875520','1060100061204631552','https://bootapi.jeecg.com/drag/share/view/1060100061204631552','','2025-03-20 20:28:27','0','0','0',NULL);
/*!40000 ALTER TABLE `onl_drag_share` ENABLE KEYS */;
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

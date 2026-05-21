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
-- Table structure for table `rep_demo_dxtj`
--

DROP TABLE IF EXISTS `rep_demo_dxtj`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rep_demo_dxtj` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'primary key',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Name',
  `gtime` datetime DEFAULT NULL COMMENT '雇佣date',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Position',
  `jphone` varchar(125) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家庭Telephone',
  `birth` datetime DEFAULT NULL COMMENT '出Birthday期',
  `hukou` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '户口所exist地',
  `laddress` varchar(125) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系address',
  `jperson` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'urgent联系people',
  `sex` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'xingbie',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rep_demo_dxtj`
--

LOCK TABLES `rep_demo_dxtj` WRITE;
/*!40000 ALTER TABLE `rep_demo_dxtj` DISABLE KEYS */;
INSERT INTO `rep_demo_dxtj` VALUES ('1338808084247613441','open三','2019-11-06 00:00:00','1','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809169074982920','openSmall哲','2019-11-06 00:00:00','2','18034596971','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809448658898952','Yan Ni','2019-11-06 00:00:00','2','18034596972','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809620973490184','strangeness','2019-11-06 00:00:00','2','18034596973','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809652606930952','He Jiang','2019-11-06 00:00:00','2','18034596974','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','2'),('1338809685200867336','village子明','2019-11-06 00:00:00','3','18034596975','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','2'),('1338809710203113481','Suntech','2019-11-06 00:00:00','4','18034596977','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809749470187528','Zheng Kai','2019-11-06 00:00:00','4','18034596978','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809774971555849','未名garden','2019-11-06 00:00:00','4','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809805199904777','Han Han','2019-11-06 00:00:00','5','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809830017601544','迪Korea热拉','2019-11-06 00:00:00','6','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1338809864356368393','open一山','2019-11-06 00:00:00','6','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157602480137','open三','2019-11-06 00:00:00','1','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157602480146','open大大','2019-11-06 00:00:00','2','18034596971','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674439','Guo Meimei','2019-11-06 00:00:00','2','18034596972','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674448','Do not worry','2019-11-06 00:00:00','2','18034596973','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674457','Lu Yu','2019-11-06 00:00:00','2','18034596974','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','2'),('1339160157606674466','high尚','2019-11-06 00:00:00','3','18034596975','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','2'),('1339160157606674475','尚Beijing','2019-11-06 00:00:00','4','18034596977','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674484','Yang Yinghua','2019-11-06 00:00:00','4','18034596978','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674493','李Korea','2019-11-06 00:00:00','4','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674502','Han Lulu','2019-11-06 00:00:00','5','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674511','Li Kaize','2019-11-06 00:00:00','6','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1'),('1339160157606674520','王明Positive','2019-11-06 00:00:00','6','18034596970','1988-12-15 00:00:00','Beijingcity朝Positivedistrict奥运village街道Asia运villageSmalldistrict','18034596972','Wang Liang','1');
/*!40000 ALTER TABLE `rep_demo_dxtj` ENABLE KEYS */;
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

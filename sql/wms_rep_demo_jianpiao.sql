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
-- Table structure for table `rep_demo_jianpiao`
--

DROP TABLE IF EXISTS `rep_demo_jianpiao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rep_demo_jianpiao` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bnum` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `ftime` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `sfkong` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `kaishi` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `jieshu` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `hezairen` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `jpnum` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `shihelv` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `s_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rep_demo_jianpiao`
--

LOCK TABLES `rep_demo_jianpiao` WRITE;
/*!40000 ALTER TABLE `rep_demo_jianpiao` DISABLE KEYS */;
INSERT INTO `rep_demo_jianpiao` VALUES (1,'K7725','21:13','no','Qinhuangdao','Handan','300','258','86',1),(2,'k99','16:55','no','Baotou','Guangzhou','800','700','88',1),(3,'G6737','05:34','no','Beijing西','Handan东','500','256','51',1),(4,'K7705','07:03','no','Beijing','Handan','400','200','50',1),(5,'G437','06:27','no','Beijing西','Lanzhou West','800','586','73',1),(6,'G673','06:32','no','Beijing西','Handan东','300','289','87',1),(7,'G507','06:43','no','Beijing西','Handan东','300','200','67',1),(8,'G89','06:53','no','Beijing西','Chengdu East','800','500','62',1),(9,'K7712','09:43','no','Beijing西','Xi an North','400','200','50',1),(10,'G405','10:05','no','Beijing西','Kunming South','300','200','67',1),(11,'G6701','10:38','no','Beijing西','Shijiazhuang','300','200','67',1),(12,'G487','10:52','no','Beijing西','Nanchang West','800','700','88',1),(13,'G607','11:14','no','Beijing西','Taiyuan South','400','200','50',1),(14,'G667','11:19','no','Beijing西','Xian North','400','200','50',1),(15,'Z49','11:28','no','Beijing西','Chengdu','400','200','50',1),(16,'Z49','11:28','no','Beijing西','Shanghai','300','200','80',1),(17,'Z49','11:56','no','Beijing西','Shanghai','200','180','95',1),(18,'Z49','11:36','no','Beijing南','Great sun','200','180','96',1),(19,'Z123','12:00','no','Beijing南','Chongqing','1000','1000','100',1),(20,'G78','13:56','no','Beijing东','Xiamen North','800','700','90',1),(21,'G56','18:36','no','Shanghai西','Shenzhen','800','700','90',1),(22,'H78','12:00','no','Shanghai','Beijing西','800','700','90',1),(23,'H78','12:00','no','Shanghai','Beijing西','800','700','90',1),(24,'H78','12:00','no','Shanghai','Beijing西','800','700','90',1),(25,'H78','12:00','no','Beijing西','Nanchang','800','700','90',1),(26,'G70','7:23','yes','Beijing西','Xiamen','500','450','95',1),(27,'G14','9:50','yes','Beijing西','Shanghai','800','700','95',1),(28,'G90','8:30','yes','Beijing南','Wuchang','1000','1000','100',1),(29,'G25','7:56','yes','Xiamen North','fuzhou','500','100','20',1),(30,'G50','14:23','no','Beijing西','Shenzhen','500','100','20',1),(31,'G10','13:00','no','Beijing西','Shenzhen','500','100','20',1),(32,'G10','13:00','no','Beijing西','Shenzhen','500','100','20',1),(33,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(34,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(35,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(36,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(37,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(38,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(39,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(40,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(41,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(42,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(43,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(44,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(45,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(46,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(47,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(48,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(49,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(50,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(51,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(52,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(53,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(54,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(55,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(56,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(57,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(58,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(59,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(60,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(61,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(62,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(63,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(64,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(65,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(66,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(67,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(68,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(69,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(70,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(71,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(72,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(73,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(74,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(75,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(76,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(77,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(78,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(79,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(80,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(81,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(82,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(83,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(84,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(85,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1),(86,'G10','13:00','no','Beijing西','Shenzhen','200','100','50',1);
/*!40000 ALTER TABLE `rep_demo_jianpiao` ENABLE KEYS */;
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

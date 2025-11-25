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
-- Table structure for table `sys_gateway_route`
--

DROP TABLE IF EXISTS `sys_gateway_route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_gateway_route` (
  `id` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `router_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '路Depend onID',
  `name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Serve名',
  `uri` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Serveaddress',
  `predicates` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'assertion',
  `filters` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'filter',
  `retryable` int DEFAULT NULL COMMENT 'yesno重试:0-no 1-yes',
  `strip_prefix` int DEFAULT NULL COMMENT 'yesno忽略prefix0-no 1-yes',
  `persistable` int DEFAULT NULL COMMENT 'yesnofor保留data:0-no 1-yes',
  `show_api` int DEFAULT NULL COMMENT 'yesnoexistinterfacedocumentmiddleexhibition示:0-no 1-yes',
  `status` int DEFAULT NULL COMMENT 'state:0-none效 1-efficient',
  `create_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Department',
  `del_flag` int DEFAULT NULL COMMENT 'deletestate',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_gateway_route`
--

LOCK TABLES `sys_gateway_route` WRITE;
/*!40000 ALTER TABLE `sys_gateway_route` DISABLE KEYS */;
INSERT INTO `sys_gateway_route` VALUES ('1331051599401857026','jeecg-demo-websocket','jeecg-demo-websocket','lb:ws://jeecg-demo','[{\"args\":[\"/vxeSocket/**\"],\"name\":\"Path\"}]','[{\"args\":[{\"value\":\"#{@ipKeyResolver}\",\"key\":\"key-resolver\"},{\"value\":20,\"key\":\"redis-rate-limiter.replenishRate\"},{\"value\":20,\"key\":\"redis-rate-limiter.burstCapacity\"}],\"name\":\"RequestRateLimiter\",\"title\":\"限流filter\"}]',NULL,NULL,NULL,NULL,1,'admin','2020-11-24 09:46:46',NULL,NULL,NULL,0),('1805444036892016641','jeecg-erp','jeecg-erp','lb://jeecg-erp','[{\"args\":[\"/erp/**\"],\"name\":\"Path\"}]','[]',NULL,NULL,NULL,NULL,1,'admin','2024-06-25 11:32:57',NULL,NULL,NULL,0),('jeecg-cloud-websocket','jeecg-system-websocket','jeecg-system-websocket','lb:ws://jeecg-system','[{\"args\":[\"/websocket/**\",\"/eoaSocket/**\",\"/newsWebsocket/**\",\"/dragChannelSocket/**\"],\"name\":\"Path\"}]','[]',NULL,NULL,NULL,NULL,1,'admin','2020-11-16 19:41:51',NULL,NULL,NULL,0),('jeecg-demo','jeecg-demo','jeecg-demo','lb://jeecg-demo','[{\"args\":[\"/mock/**\",\"/bigscreen/template1/**\",\"/bigscreen/template2/**\",\"/test/**\",\"/hello/**\"],\"name\":\"Path\"}]','[]',NULL,NULL,NULL,NULL,1,'admin','2020-11-16 19:41:51',NULL,NULL,NULL,0),('jeecg-system','jeecg-system','jeecg-system','lb://jeecg-system','[{\"args\":[\"/sys/**\",\"/online/**\",\"/bigscreen/**\",\"/jmreport/**\",\"/druid/**\",\"/generic/**\",\"/actuator/**\",\"/drag/**\",\"/oauth2/**\",\"/defa/**\",\"/demo/**\",\"/jimubi/**\",\"/airag/**\",\"/openapi/**\"],\"name\":\"Path\"}]','[]',NULL,NULL,NULL,NULL,1,'admin','2020-11-16 19:41:51',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `sys_gateway_route` ENABLE KEYS */;
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

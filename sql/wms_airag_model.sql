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
-- Table structure for table `airag_model`
--

DROP TABLE IF EXISTS `airag_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `airag_model` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Creator',
  `create_time` datetime DEFAULT NULL COMMENT 'Creation date',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Updater',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Department',
  `tenant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'tenantid',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'name',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'supplier',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Modelname',
  `credential` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Credential information',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'APIdomain name',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Modeltype',
  `model_params` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Modelparameter',
  `activate_flag` int DEFAULT NULL COMMENT 'yesnoactivation（1=yes，0=no）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `airag_model`
--

LOCK TABLES `airag_model` WRITE;
/*!40000 ALTER TABLE `airag_model` DISABLE KEYS */;
INSERT INTO `airag_model` VALUES ('1890232564262739969','jeecg','2025-02-14 10:52:16','admin','2025-09-13 17:54:55','A04',NULL,'OpenAI','OPENAI','gpt-4o-mini','{\"apiKey\":\"???\"}','https://api.gpt.ge','LLM','{\"temperature\":0.2,\"topP\":0.7,\"presencePenalty\":0.5,\"frequencyPenalty\":0.5,\"maxTokens\":null}',0),('1891459707122499586','jeecg','2025-02-17 20:08:30','admin','2025-09-13 17:54:50','A04',NULL,'OpenAIvector','OPENAI','text-embedding-ada-002','{\"apiKey\":\"???\"}','https://api.v3.cm/v1','EMBED',NULL,0),('1897481367743143938','jeecg','2025-03-06 10:56:26','admin','2025-09-13 17:54:45','A04',NULL,'deepseek','DEEPSEEK','deepseek-chat','{\"apiKey\":\"???\"}','https://api.deepseek.com/v1','LLM',NULL,0),('1897883052995006466','jeecg','2025-03-07 13:32:35','admin','2025-09-13 17:54:41','A04',NULL,'Wisdom spectrum','ZHIPU','glm-4-flash','{\"apiKey\":\"???\"}','https://open.bigmodel.cn/','LLM',NULL,0),('1897884353107611650','jeecg','2025-03-07 13:37:45','admin','2025-09-13 17:54:36','A04',NULL,'Wisdom spectrumvector','ZHIPU','Embedding-3','{\"apiKey\":\"???\"}','https://open.bigmodel.cn','EMBED','{\"temperature\":0.7,\"topP\":0.7,\"presencePenalty\":null,\"frequencyPenalty\":null,\"maxTokens\":null}',0);
/*!40000 ALTER TABLE `airag_model` ENABLE KEYS */;
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

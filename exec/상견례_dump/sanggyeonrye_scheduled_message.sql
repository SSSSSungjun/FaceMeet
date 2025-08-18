-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: i13d201.p.ssafy.io    Database: sanggyeonrye
-- ------------------------------------------------------
-- Server version	8.0.43-0ubuntu0.22.04.1

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
-- Table structure for table `scheduled_message`
--

DROP TABLE IF EXISTS `scheduled_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scheduled_message` (
  `scheduled_message_id` bigint NOT NULL AUTO_INCREMENT,
  `setting_id` bigint DEFAULT NULL,
  `fcm_topic_id` bigint DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `message_data` json DEFAULT NULL,
  `scheduled_time` datetime NOT NULL,
  `status` enum('SCHEDULED','PENDING','SENT','FAILED','CANCELLED') DEFAULT 'SCHEDULED',
  `sent_at` datetime DEFAULT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`scheduled_message_id`),
  KEY `idx_setting_id` (`setting_id`),
  KEY `idx_scheduled_time` (`scheduled_time`),
  KEY `idx_status` (`status`),
  KEY `scheduled_message_ibfk_2` (`fcm_topic_id`),
  CONSTRAINT `scheduled_message_ibfk_1` FOREIGN KEY (`setting_id`) REFERENCES `setting` (`setting_id`) ON DELETE CASCADE,
  CONSTRAINT `scheduled_message_ibfk_2` FOREIGN KEY (`fcm_topic_id`) REFERENCES `fcm_topic` (`fcm_topic_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scheduled_message`
--

LOCK TABLES `scheduled_message` WRITE;
/*!40000 ALTER TABLE `scheduled_message` DISABLE KEYS */;
INSERT INTO `scheduled_message` VALUES (25,6,1,'오픈 기념 매칭권 이벤트','ㅋㅋ','{\"body\": \"ㅋㅋ\", \"type\": \"SCHEDULED_EVENT\", \"title\": \"오픈 기념 매칭권 이벤트\", \"settingId\": \"6\", \"timestamp\": \"2025-08-17T22:03:51.004102642\", \"triggerTime\": \"2025-08-17T22:04:02.477\", \"additionalProp1\": \"string\", \"additionalProp2\": \"string\", \"additionalProp3\": \"string\"}','2025-08-17 22:04:02','SENT',NULL,NULL,'2025-08-17 22:00:35'),(26,6,1,'매칭권 이벤트 사전 알림','1분 뒤에 선착순 매칭권 이벤트가 시작됩니다!','{\"body\": \"1분 뒤에 선착순 매칭권 이벤트가 시작됩니다!\", \"type\": \"PRE_MESSAGE\", \"title\": \"매칭권 이벤트 사전 알림\", \"settingId\": \"6\", \"timestamp\": \"2025-08-17T22:03:02.480492162\"}','2025-08-17 22:03:03','SENT',NULL,NULL,'2025-08-17 22:03:03'),(27,7,1,'오픈 기념 매칭권 이벤트','빨리 받아보세요!','{\"body\": \"빨리 받아보세요!\", \"type\": \"SCHEDULED_EVENT\", \"title\": \"오픈 기념 매칭권 이벤트\", \"settingId\": \"7\", \"timestamp\": \"2025-08-17T22:38:26.298085514\", \"triggerTime\": \"2025-08-17T22:39:03.920\"}','2025-08-17 22:39:04','SENT',NULL,NULL,'2025-08-17 22:35:49'),(28,7,1,'매칭권 이벤트 사전 알림','1분 뒤에 선착순 매칭권 이벤트가 시작됩니다!','{\"body\": \"1분 뒤에 선착순 매칭권 이벤트가 시작됩니다!\", \"type\": \"PRE_MESSAGE\", \"title\": \"매칭권 이벤트 사전 알림\", \"settingId\": \"7\", \"timestamp\": \"2025-08-17T22:38:03.922794833\"}','2025-08-17 22:38:05','SENT',NULL,NULL,'2025-08-17 22:38:05');
/*!40000 ALTER TABLE `scheduled_message` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:44

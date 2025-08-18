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
-- Table structure for table `user_notification_history`
--

DROP TABLE IF EXISTS `user_notification_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_notification_history` (
  `user_notification_history_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `scheduled_message_id` bigint NOT NULL,
  `received_at` datetime DEFAULT NULL,
  `is_read` bit(1) DEFAULT NULL,
  PRIMARY KEY (`user_notification_history_id`),
  UNIQUE KEY `user_id` (`user_id`,`scheduled_message_id`),
  KEY `fk_user_notification_history_user_idx` (`user_id`),
  KEY `fk_user_notification_history_scheduled_message1_idx` (`scheduled_message_id`),
  CONSTRAINT `fk_user_notification_history_scheduled_message1` FOREIGN KEY (`scheduled_message_id`) REFERENCES `scheduled_message` (`scheduled_message_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_notification_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_notification_history`
--

LOCK TABLES `user_notification_history` WRITE;
/*!40000 ALTER TABLE `user_notification_history` DISABLE KEYS */;
INSERT INTO `user_notification_history` VALUES (130,6,26,'2025-08-17 22:03:03',_binary '\0'),(131,13,26,'2025-08-17 22:03:03',_binary '\0'),(132,1,26,'2025-08-17 22:03:03',_binary '\0'),(133,11,26,'2025-08-17 22:03:03',_binary '\0'),(134,19,26,'2025-08-17 22:03:03',_binary ''),(135,2,26,'2025-08-17 22:03:03',_binary '\0'),(136,22,26,'2025-08-17 22:03:03',_binary '\0'),(137,31,26,'2025-08-17 22:03:03',_binary '\0'),(138,22,25,'2025-08-17 22:04:02',_binary '\0'),(140,6,25,'2025-08-17 22:04:02',_binary '\0'),(142,35,27,'2025-08-17 22:39:04',_binary '\0'),(143,6,27,'2025-08-17 22:39:04',_binary '\0'),(145,6,28,'2025-08-17 22:38:05',_binary '\0'),(146,13,28,'2025-08-17 22:38:05',_binary '\0'),(147,1,28,'2025-08-17 22:38:05',_binary '\0'),(148,11,28,'2025-08-17 22:38:05',_binary '\0'),(149,19,28,'2025-08-17 22:38:05',_binary ''),(150,22,28,'2025-08-17 22:38:05',_binary '\0'),(151,32,28,'2025-08-17 22:38:05',_binary '\0'),(152,35,28,'2025-08-17 22:38:05',_binary ''),(153,22,27,'2025-08-17 22:39:04',_binary '\0');
/*!40000 ALTER TABLE `user_notification_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:51

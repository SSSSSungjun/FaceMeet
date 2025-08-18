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
-- Table structure for table `chat_room_member`
--

DROP TABLE IF EXISTS `chat_room_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_room_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `is_visible` bit(1) NOT NULL,
  `joined_at` datetime NOT NULL,
  `left_at` datetime DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  KEY `fk_CHAT_ROOM_MEMBER_CHAT_ROOM1_idx` (`room_id`),
  KEY `fk_CHAT_ROOM_MEMBER_USER1_idx` (`user_id`),
  CONSTRAINT `fk_CHAT_ROOM_MEMBER_CHAT_ROOM1` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`room_id`),
  CONSTRAINT `fk_CHAT_ROOM_MEMBER_USER1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_room_member`
--

LOCK TABLES `chat_room_member` WRITE;
/*!40000 ALTER TABLE `chat_room_member` DISABLE KEYS */;
INSERT INTO `chat_room_member` VALUES (1,1,3,_binary '','2025-08-14 16:44:29',NULL),(2,1,4,_binary '','2025-08-14 16:44:29',NULL),(3,2,2,_binary '','2025-08-14 16:56:41',NULL),(4,2,5,_binary '','2025-08-14 16:56:41',NULL),(5,3,1,_binary '','2025-08-14 16:56:48',NULL),(6,3,3,_binary '','2025-08-14 16:56:48',NULL),(7,4,4,_binary '','2025-08-14 16:56:51',NULL),(8,4,2,_binary '','2025-08-14 16:56:51',NULL),(9,5,11,_binary '','2025-08-14 17:18:37',NULL),(10,5,3,_binary '','2025-08-14 17:18:37',NULL),(11,6,3,_binary '','2025-08-14 17:25:11',NULL),(12,6,7,_binary '','2025-08-14 17:25:11',NULL),(13,7,12,_binary '','2025-08-14 17:26:37',NULL),(14,7,2,_binary '','2025-08-14 17:26:37',NULL),(15,8,13,_binary '','2025-08-14 17:37:22',NULL),(16,8,4,_binary '','2025-08-14 17:37:22',NULL),(17,9,14,_binary '','2025-08-14 17:39:19',NULL),(18,9,13,_binary '','2025-08-14 17:39:19',NULL),(19,10,14,_binary '','2025-08-14 17:49:59',NULL),(20,10,2,_binary '','2025-08-14 17:49:59',NULL),(21,11,10,_binary '','2025-08-14 17:52:48',NULL),(22,11,7,_binary '','2025-08-14 17:52:48',NULL),(23,12,2,_binary '','2025-08-14 17:57:06',NULL),(24,12,11,_binary '','2025-08-14 17:57:06',NULL),(25,13,11,_binary '','2025-08-14 18:33:09',NULL),(26,13,10,_binary '','2025-08-14 18:33:09',NULL),(27,14,16,_binary '','2025-08-14 18:40:56',NULL),(28,14,7,_binary '','2025-08-14 18:40:56',NULL),(29,15,7,_binary '','2025-08-15 14:10:21',NULL),(30,15,2,_binary '','2025-08-15 14:10:21',NULL),(31,16,19,_binary '','2025-08-15 15:01:42',NULL),(32,16,16,_binary '','2025-08-15 15:01:42',NULL),(33,17,2,_binary '','2025-08-15 15:01:42',NULL),(34,17,10,_binary '','2025-08-15 15:01:42',NULL),(35,18,19,_binary '','2025-08-15 21:12:51',NULL),(36,18,10,_binary '','2025-08-15 21:12:51',NULL),(37,19,19,_binary '','2025-08-15 21:28:13',NULL),(38,19,2,_binary '','2025-08-15 21:28:13',NULL),(39,20,19,_binary '','2025-08-15 21:39:21',NULL),(40,20,3,_binary '','2025-08-15 21:39:21',NULL),(41,21,21,_binary '','2025-08-15 23:32:22',NULL),(42,21,14,_binary '','2025-08-15 23:32:22',NULL),(43,22,22,_binary '','2025-08-16 19:45:53',NULL),(44,22,11,_binary '','2025-08-16 19:45:53',NULL),(45,23,11,_binary '','2025-08-16 19:58:41',NULL),(46,23,16,_binary '','2025-08-16 19:58:41',NULL),(47,24,11,_binary '','2025-08-16 20:14:27',NULL),(48,24,21,_binary '','2025-08-16 20:14:27',NULL),(49,25,19,_binary '','2025-08-16 20:48:01',NULL),(50,25,21,_binary '','2025-08-16 20:48:01',NULL),(51,26,7,_binary '','2025-08-17 18:44:26',NULL),(52,26,22,_binary '','2025-08-17 18:44:26',NULL),(53,27,2,_binary '','2025-08-17 19:36:22',NULL),(54,27,23,_binary '','2025-08-17 19:36:22',NULL),(55,28,6,_binary '','2025-08-17 19:41:13',NULL),(56,28,22,_binary '','2025-08-17 19:41:13',NULL),(57,29,22,_binary '','2025-08-17 19:46:55',NULL),(58,29,23,_binary '','2025-08-17 19:46:55',NULL),(59,30,24,_binary '','2025-08-17 19:56:09',NULL),(60,30,22,_binary '','2025-08-17 19:56:09',NULL),(61,31,19,_binary '','2025-08-17 20:27:16',NULL),(62,31,22,_binary '','2025-08-17 20:27:16',NULL),(63,32,26,_binary '','2025-08-17 20:56:17',NULL),(64,32,22,_binary '','2025-08-17 20:56:17',NULL),(65,33,27,_binary '','2025-08-17 21:22:29',NULL),(66,33,2,_binary '','2025-08-17 21:22:29',NULL),(67,34,35,_binary '','2025-08-17 22:37:01',NULL),(68,34,22,_binary '','2025-08-17 22:37:01',NULL),(69,35,32,_binary '','2025-08-18 00:26:18',NULL),(70,35,22,_binary '','2025-08-18 00:26:18',NULL),(71,36,34,_binary '','2025-08-18 03:33:04',NULL),(72,36,22,_binary '','2025-08-18 03:33:04',NULL);
/*!40000 ALTER TABLE `chat_room_member` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:31:03

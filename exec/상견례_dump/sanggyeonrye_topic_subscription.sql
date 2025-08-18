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
-- Table structure for table `topic_subscription`
--

DROP TABLE IF EXISTS `topic_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_subscription` (
  `topic_subscription_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `fcm_topic_id` bigint NOT NULL,
  `subscribed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`topic_subscription_id`),
  UNIQUE KEY `unique_user_topic` (`user_id`,`fcm_topic_id`),
  UNIQUE KEY `UK4elg3xx4bj7e5huu3obor5wlo` (`user_id`,`fcm_topic_id`),
  KEY `fk_table1_fcm_topic1_idx` (`fcm_topic_id`),
  KEY `fk_table1_user1_idx` (`user_id`),
  CONSTRAINT `fk_table1_fcm_topic1` FOREIGN KEY (`fcm_topic_id`) REFERENCES `fcm_topic` (`fcm_topic_id`),
  CONSTRAINT `fk_table1_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_subscription`
--

LOCK TABLES `topic_subscription` WRITE;
/*!40000 ALTER TABLE `topic_subscription` DISABLE KEYS */;
INSERT INTO `topic_subscription` VALUES (1,6,1,NULL),(8,13,1,NULL),(44,1,1,NULL),(47,11,1,NULL),(52,19,1,NULL),(54,22,1,NULL),(66,32,1,NULL),(67,35,1,NULL),(68,34,1,NULL);
/*!40000 ALTER TABLE `topic_subscription` ENABLE KEYS */;
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

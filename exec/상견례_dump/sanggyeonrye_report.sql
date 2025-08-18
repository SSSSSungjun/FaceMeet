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
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `report_id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  `reporter_id` bigint NOT NULL,
  `reported_id` bigint NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `img` varchar(150) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `is_solved` bit(1) NOT NULL,
  PRIMARY KEY (`report_id`),
  KEY `fk_REPORT_CHAT_ROOM1_idx` (`room_id`),
  KEY `fk_REPORT_USER1_idx` (`reporter_id`),
  KEY `fk_REPORT_USER2_idx` (`reported_id`),
  KEY `fk_report_REPORT_CATEGORY1_idx` (`category_id`),
  CONSTRAINT `fk_REPORT_CHAT_ROOM1` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`room_id`),
  CONSTRAINT `fk_REPORT_USER1` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_REPORT_USER2` FOREIGN KEY (`reported_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKjrimfnqckhy8mxrhivp3x0e8x` FOREIGN KEY (`category_id`) REFERENCES `report_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
INSERT INTO `report` VALUES (1,34,2,35,22,'상처받으뮤','','2025-08-17 22:37:46',_binary '');
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:47

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
-- Table structure for table `eye_parts`
--

DROP TABLE IF EXISTS `eye_parts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eye_parts` (
  `eye_parts_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) DEFAULT NULL,
  `desc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`eye_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eye_parts`
--

LOCK TABLES `eye_parts` WRITE;
/*!40000 ALTER TABLE `eye_parts` DISABLE KEYS */;
INSERT INTO `eye_parts` VALUES (1,'큰 눈','감수성이 강하고 낙천적이며 정열적이다. 여성의 경우 화려한 것을 좋아하는 경향이 있다.'),(2,'작은 눈','선경지명은 부족하지만 빈틈이 없다.'),(3,'올라간 눈','감정이 격하며, 여성은 남자를 휘어잡는 성향이 있다. 출세가 빠르고 호탕하며 진취적이다. 고집이 세다.'),(4,'내려간 눈','세심하고 소극적이다. 온화하며 가정적인 편이나, 간혹 바람기가 있을 수 있다.'),(5,'둥근 눈','명랑하고 감각이 예리하다. 부화뇌동하기 쉬우며, 유혹에 빠지기 쉽다. 적극적인 성격이다.'),(6,'가는 눈','깊은 내면을 지니며 친구가 매우 적다. 그러나 우정은 오래 지속된다.');
/*!40000 ALTER TABLE `eye_parts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:31:02

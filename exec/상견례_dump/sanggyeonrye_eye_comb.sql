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
-- Table structure for table `eye_comb`
--

DROP TABLE IF EXISTS `eye_comb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eye_comb` (
  `eye_comb_id` bigint NOT NULL AUTO_INCREMENT,
  `eye_parts_id1` bigint NOT NULL,
  `eye_parts_id2` bigint NOT NULL,
  `eye_parts_id3` bigint NOT NULL,
  `desc` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`eye_comb_id`),
  UNIQUE KEY `uk_eye_parts_combination` (`eye_parts_id1`,`eye_parts_id2`,`eye_parts_id3`),
  KEY `fk_eye_comb_eye_parts_idx` (`eye_parts_id1`),
  KEY `fk_eye_comb_eye_parts1_idx` (`eye_parts_id2`),
  KEY `fk_eye_comb_eye_parts2_idx` (`eye_parts_id3`),
  CONSTRAINT `fk_eye_comb_eye_parts` FOREIGN KEY (`eye_parts_id1`) REFERENCES `eye_parts` (`eye_parts_id`),
  CONSTRAINT `fk_eye_comb_eye_parts1` FOREIGN KEY (`eye_parts_id2`) REFERENCES `eye_parts` (`eye_parts_id`),
  CONSTRAINT `fk_eye_comb_eye_parts2` FOREIGN KEY (`eye_parts_id3`) REFERENCES `eye_parts` (`eye_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eye_comb`
--

LOCK TABLES `eye_comb` WRITE;
/*!40000 ALTER TABLE `eye_comb` DISABLE KEYS */;
INSERT INTO `eye_comb` VALUES (1,1,3,5,'감정 기복 크고 정열적이며 적극적인 성향'),(2,1,3,6,'조용하지만 고집 있고 진취적인 집중형'),(3,1,4,5,'감성 풍부하고 명랑하며 가정적인 성격'),(4,1,4,6,'내성적이고 섬세하며 신뢰 깊은 관계 지향'),(5,2,3,5,'섬세하지만 출세욕 강하고 적극적인 면 존재'),(6,2,3,6,'고집 있고 신중하며 깊은 인간관계를 선호'),(7,2,4,5,'조용하지만 감각적이고 유혹에 흔들리기 쉬움'),(8,2,4,6,'내향적이고 조용하며 안정된 우정을 중시');
/*!40000 ALTER TABLE `eye_comb` ENABLE KEYS */;
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

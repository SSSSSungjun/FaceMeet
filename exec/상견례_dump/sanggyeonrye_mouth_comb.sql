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
-- Table structure for table `mouth_comb`
--

DROP TABLE IF EXISTS `mouth_comb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mouth_comb` (
  `mouth_comb_id` bigint NOT NULL AUTO_INCREMENT,
  `mouth_parts_id1` bigint NOT NULL,
  `mouth_parts_id2` bigint NOT NULL,
  `mouth_parts_id3` bigint NOT NULL,
  `desc` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`mouth_comb_id`),
  UNIQUE KEY `uk_mouth_parts_combination` (`mouth_parts_id1`,`mouth_parts_id2`,`mouth_parts_id3`),
  KEY `fk_mouth_comb_mouth_parts1_idx` (`mouth_parts_id1`),
  KEY `fk_mouth_comb_mouth_parts2_idx` (`mouth_parts_id2`),
  KEY `fk_mouth_comb_mouth_parts3_idx` (`mouth_parts_id3`),
  CONSTRAINT `fk_mouth_comb_mouth_parts1` FOREIGN KEY (`mouth_parts_id1`) REFERENCES `mouth_parts` (`mouth_parts_id`),
  CONSTRAINT `fk_mouth_comb_mouth_parts2` FOREIGN KEY (`mouth_parts_id2`) REFERENCES `mouth_parts` (`mouth_parts_id`),
  CONSTRAINT `fk_mouth_comb_mouth_parts3` FOREIGN KEY (`mouth_parts_id3`) REFERENCES `mouth_parts` (`mouth_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mouth_comb`
--

LOCK TABLES `mouth_comb` WRITE;
/*!40000 ALTER TABLE `mouth_comb` DISABLE KEYS */;
INSERT INTO `mouth_comb` VALUES (1,1,3,5,'외향적이고 냉정하지만 밝고 인기 많은 성격'),(2,1,3,6,'외향적이지만 냉정하고 의지 강한 부정적 경향'),(3,1,4,5,'명랑하고 따뜻하며 감정 풍부하고 배려심 있음'),(4,1,4,6,'외향적이고 인내심 강하지만 다소 부정적임'),(5,2,3,5,'내성적이지만 명랑하고 냉정한 면도 공존'),(6,2,3,6,'내향적이며 차갑고 의지 강하나 부정적 성향'),(7,2,4,5,'소심하지만 감정 풍부하고 인기 있는 편'),(8,2,4,6,'내향적이며 감정 깊고 부정적 경향도 있음');
/*!40000 ALTER TABLE `mouth_comb` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:49

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
-- Table structure for table `nose_comb`
--

DROP TABLE IF EXISTS `nose_comb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nose_comb` (
  `nose_comb_id` bigint NOT NULL AUTO_INCREMENT,
  `nose_parts_id1` bigint NOT NULL,
  `nose_parts_id2` bigint NOT NULL,
  `nose_parts_id3` bigint NOT NULL,
  `nose_parts_id4` bigint NOT NULL,
  `desc` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`nose_comb_id`),
  UNIQUE KEY `uk_nose_parts_combination` (`nose_parts_id1`,`nose_parts_id2`,`nose_parts_id3`,`nose_parts_id4`),
  KEY `fk_nose_comb_nose_parts1_idx` (`nose_parts_id1`),
  KEY `fk_nose_comb_nose_parts2_idx` (`nose_parts_id2`),
  KEY `fk_nose_comb_nose_parts3_idx` (`nose_parts_id3`),
  KEY `fk_nose_comb_nose_parts4_idx` (`nose_parts_id4`),
  CONSTRAINT `fk_nose_comb_nose_parts1` FOREIGN KEY (`nose_parts_id1`) REFERENCES `nose_parts` (`nose_parts_id`),
  CONSTRAINT `fk_nose_comb_nose_parts2` FOREIGN KEY (`nose_parts_id2`) REFERENCES `nose_parts` (`nose_parts_id`),
  CONSTRAINT `fk_nose_comb_nose_parts3` FOREIGN KEY (`nose_parts_id3`) REFERENCES `nose_parts` (`nose_parts_id`),
  CONSTRAINT `fk_nose_comb_nose_parts4` FOREIGN KEY (`nose_parts_id4`) REFERENCES `nose_parts` (`nose_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nose_comb`
--

LOCK TABLES `nose_comb` WRITE;
/*!40000 ALTER TABLE `nose_comb` DISABLE KEYS */;
INSERT INTO `nose_comb` VALUES (1,1,3,5,7,'자부심 강하고 활력 있으며 밝고 주관 뚜렷함'),(2,1,3,5,8,'자부심 강하고 활력 있으나 내성적 성향 있음'),(3,1,3,6,7,'자부심 강하고 신중하며 주관 뚜렷한 성격'),(4,1,3,6,8,'자부심 강하고 조용하며 내면적 성향 지님'),(5,1,4,5,7,'외향적이고 소심하지만 밝고 주관 강한 편'),(6,1,4,5,8,'외향적이고 소심하지만 내성적이고 밝음'),(7,1,4,6,7,'외향적이나 소심하고 신중하며 주관 강함'),(8,1,4,6,8,'외향적이면서 소심하고 신중하며 내성적임'),(9,2,3,5,7,'자신감 부족하나 명랑하고 사고력 뛰어난 성향'),(10,2,3,5,8,'자신감 부족하고 내성적이나 사고력 뛰어남'),(11,2,3,6,7,'내향적이며 신중하고 주관 강한 성격'),(12,2,3,6,8,'내향적이고 신중하며 체력 약한 편'),(13,2,4,5,7,'자신감 부족하지만 체력 있고 밝고 주관 셈'),(14,2,4,5,8,'자신감 부족하고 소심하지만 밝고 내성적임'),(15,2,4,6,7,'소극적이고 체력 있지만 주관 강한 편'),(16,2,4,6,8,'소심하고 신중하며 내성적인 성향');
/*!40000 ALTER TABLE `nose_comb` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:59

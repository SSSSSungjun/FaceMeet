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
-- Table structure for table `eyebrow_comb`
--

DROP TABLE IF EXISTS `eyebrow_comb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eyebrow_comb` (
  `eyebrow_comb_id` bigint NOT NULL AUTO_INCREMENT,
  `eyebrow_parts_id1` bigint NOT NULL,
  `eyebrow_parts_id2` bigint NOT NULL,
  `eyebrow_parts_id3` bigint NOT NULL,
  `eyebrow_parts_id4` bigint NOT NULL,
  `desc` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`eyebrow_comb_id`),
  UNIQUE KEY `uk_eyebrow_parts_combination` (`eyebrow_parts_id1`,`eyebrow_parts_id2`,`eyebrow_parts_id3`,`eyebrow_parts_id4`),
  KEY `fk_eyebrow_comb_eyebrow_parts1_idx` (`eyebrow_parts_id1`),
  KEY `fk_eyebrow_comb_eyebrow_parts2_idx` (`eyebrow_parts_id2`),
  KEY `fk_eyebrow_comb_eyebrow_parts3_idx` (`eyebrow_parts_id3`),
  KEY `fk_eyebrow_comb_eyebrow_parts4_idx` (`eyebrow_parts_id4`),
  CONSTRAINT `fk_eyebrow_comb_eyebrow_parts1` FOREIGN KEY (`eyebrow_parts_id1`) REFERENCES `eyebrow_parts` (`eyebrow_parts_id`),
  CONSTRAINT `fk_eyebrow_comb_eyebrow_parts2` FOREIGN KEY (`eyebrow_parts_id2`) REFERENCES `eyebrow_parts` (`eyebrow_parts_id`),
  CONSTRAINT `fk_eyebrow_comb_eyebrow_parts3` FOREIGN KEY (`eyebrow_parts_id3`) REFERENCES `eyebrow_parts` (`eyebrow_parts_id`),
  CONSTRAINT `fk_eyebrow_comb_eyebrow_parts4` FOREIGN KEY (`eyebrow_parts_id4`) REFERENCES `eyebrow_parts` (`eyebrow_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eyebrow_comb`
--

LOCK TABLES `eyebrow_comb` WRITE;
/*!40000 ALTER TABLE `eyebrow_comb` DISABLE KEYS */;
INSERT INTO `eyebrow_comb` VALUES (1,1,3,5,8,'적극적이며 창의적이고 안정적인 신뢰형'),(2,1,3,5,9,'창의적이고 단호하며 추진력 있는 성향'),(3,1,3,6,8,'진취적이며 여유롭고 포용력 있는 리더형'),(4,1,3,6,9,'예술적 감각과 자존심 강한 추진형'),(5,1,3,7,8,'자기주장 강하고 포용력 있는 리더형'),(6,1,3,7,9,'감성적이며 고집 있는 단호한 성향'),(7,1,4,5,8,'직설적이며 따뜻한 가정적 성격'),(8,1,4,5,9,'직설적이고 고집 있는 현실적 리더형'),(9,1,4,6,8,'승부욕 강하고 균형 잡힌 추진형'),(10,1,4,6,9,'결단력 강하고 진취적인 마인드'),(11,1,4,7,8,'고집 있지만 균형감 있는 리더형'),(12,1,4,7,9,'단호하고 자기중심적이지만 추진력 강함'),(13,2,3,5,8,'섬세하고 온화하며 창의적인 성향'),(14,2,3,5,9,'예민하지만 예술 감각과 가정성 지님'),(15,2,3,6,8,'섬세하면서도 자존심 강한 자기주도형'),(16,2,3,6,9,'신중하며 독립적인 예술 감성형'),(17,2,3,7,8,'섬세하고 창의적인 조용한 리더형'),(18,2,3,7,9,'예민하고 독립심 강한 내향적 성향'),(19,2,4,5,8,'조용하고 안정 지향적인 가정형'),(20,2,4,5,9,'섬세하고 실속 중시하는 내향적 성격'),(21,2,4,6,8,'내면 강한 목표 지향적 조용한 추진형'),(22,2,4,6,9,'진취적이며 신중한 독립형 인물'),(23,2,4,7,8,'조용하지만 자기주장 분명한 리더형'),(24,2,4,7,9,'신중하고 단단한 현실 중심 독립형');
/*!40000 ALTER TABLE `eyebrow_comb` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:58

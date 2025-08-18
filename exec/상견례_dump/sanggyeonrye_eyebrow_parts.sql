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
-- Table structure for table `eyebrow_parts`
--

DROP TABLE IF EXISTS `eyebrow_parts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eyebrow_parts` (
  `eyebrow_parts_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) DEFAULT NULL,
  `desc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`eyebrow_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eyebrow_parts`
--

LOCK TABLES `eyebrow_parts` WRITE;
/*!40000 ALTER TABLE `eyebrow_parts` DISABLE KEYS */;
INSERT INTO `eyebrow_parts` VALUES (1,'굵은 눈썹','남성적이고 적극적이다. 하지만 기가 세서 주변에서 고집이 세다는 평을 받을 수 있다.'),(2,'가는 눈썹','보통 여성적이다. 소극적이고 성격이 약하다는 평을 받는다.'),(3,'긴 눈썹','상대방을 잘 헤아린다. 여유가 많고 창의성과 예술적 재능을 나타낸다.'),(4,'짧은 눈썹','성격이 급하고 감정 표현이 직설적이다. 인복이 약하고 재물 관리에 어려움을 겪을 수 있다.'),(5,'일자 눈썹','인생과 건강 모두 굴곡 없는 관상. 안정적인 생활을 중시하고 가정적인 성향을 가지고 있다.'),(6,'올라간 눈썹','남자다운 성향이 강하다. 목표를 이루기 위해 노력하는 승부사 기질이 많다. 자존심이 강한 편이다.'),(7,'내려간 눈썹','자기주장이 강하고 고집이 세다. 의견을 확실하게 말하며 중년에 어려움을 겪지만 유종의 미를 거둘 수 있다.'),(8,'사이가 넓은 눈썹','온화한 성격으로 장수하며 시야가 넓다.'),(9,'사이가 좁은 눈썹','단명하는 경우가 많다.');
/*!40000 ALTER TABLE `eyebrow_parts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:56

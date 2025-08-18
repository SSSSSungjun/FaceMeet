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
-- Table structure for table `nose_parts`
--

DROP TABLE IF EXISTS `nose_parts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nose_parts` (
  `nose_parts_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) DEFAULT NULL,
  `desc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`nose_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nose_parts`
--

LOCK TABLES `nose_parts` WRITE;
/*!40000 ALTER TABLE `nose_parts` DISABLE KEYS */;
INSERT INTO `nose_parts` VALUES (1,'높은 코','자부심 높음, 호전인 성격, 향상심 강함, 명예획득을 중요시 생각,  미적 감각이 예리'),(2,'낮은 코','자신감 부족, 임의로운 성격, 성질 급하며 화를 잘 내기도 하며 손해를 보는 경우가 있다.'),(3,'긴 코','생각이 깊고 신념을 가짐, 사교성 좋음, 성급해서 화를 내는 것이 결점, 인정 많고 오래 살 수 있는 활력 가짐.'),(4,'짧은 코','체력이 튼튼, 점잖음, 기가 약하며 사소한 일에 고민함, 경솔한 행동을 하는 경향'),(5,'코 끝이 올라간','밝고 명량, 쉽게 말을 걸기 편함, 덜렁대는 경향'),(6,'코 끝이 내려간','침착함, 사교성이 부족함, 절약가 (저축 좋아함)'),(7,'폭 넓은 코','체력이 튼튼, 자신의 주관을 적극적으로 말함, 돈이나 명예의 집착 강해서 남들에게 빈축을 살 수도'),(8,'폭 좁은 코','체력이 약함, 성격이 내성적');
/*!40000 ALTER TABLE `nose_parts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:50

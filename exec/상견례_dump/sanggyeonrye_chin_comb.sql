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
-- Table structure for table `chin_comb`
--

DROP TABLE IF EXISTS `chin_comb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chin_comb` (
  `chin_comb_id` bigint NOT NULL AUTO_INCREMENT,
  `chin_parts_id1` bigint NOT NULL,
  `chin_parts_id2` bigint NOT NULL,
  `desc` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`chin_comb_id`),
  UNIQUE KEY `uk_chin_parts_combination` (`chin_parts_id1`,`chin_parts_id2`),
  KEY `fk_chin_comb_chin_parts1_idx` (`chin_parts_id1`),
  KEY `fk_chin_comb_chin_parts2_idx` (`chin_parts_id2`),
  CONSTRAINT `fk_chin_comb_chin_parts1` FOREIGN KEY (`chin_parts_id1`) REFERENCES `chin_parts` (`chin_parts_id`),
  CONSTRAINT `fk_chin_comb_chin_parts2` FOREIGN KEY (`chin_parts_id2`) REFERENCES `chin_parts` (`chin_parts_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chin_comb`
--

LOCK TABLES `chin_comb` WRITE;
/*!40000 ALTER TABLE `chin_comb` DISABLE KEYS */;
INSERT INTO `chin_comb` VALUES (1,1,4,'온화하고 성실하며 현실적인 성격'),(2,1,5,'성실하지만 소극적이며 온순한 인상'),(3,2,4,'예민하지만 현실적이며 내면이 깊음'),(4,2,5,'예민하고 소극적이며 걱정이 많은 편'),(5,3,4,'의지가 강하며 현실적이고 인기 있는 성격'),(6,3,5,'강한 의지에 비해 내향적이고 조용함');
/*!40000 ALTER TABLE `chin_comb` ENABLE KEYS */;
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

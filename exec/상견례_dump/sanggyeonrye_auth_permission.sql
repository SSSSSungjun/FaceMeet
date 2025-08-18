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
-- Table structure for table `auth_permission`
--

DROP TABLE IF EXISTS `auth_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `content_type_id` int NOT NULL,
  `codename` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_permission_content_type_id_codename_01ab375a_uniq` (`content_type_id`,`codename`),
  CONSTRAINT `auth_permission_content_type_id_2f476e4b_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_permission`
--

LOCK TABLES `auth_permission` WRITE;
/*!40000 ALTER TABLE `auth_permission` DISABLE KEYS */;
INSERT INTO `auth_permission` VALUES (1,'Can add chin',1,'add_chin'),(2,'Can change chin',1,'change_chin'),(3,'Can delete chin',1,'delete_chin'),(4,'Can view chin',1,'view_chin'),(5,'Can add faceshape',2,'add_faceshape'),(6,'Can change faceshape',2,'change_faceshape'),(7,'Can delete faceshape',2,'delete_faceshape'),(8,'Can view faceshape',2,'view_faceshape'),(9,'Can add mouth',3,'add_mouth'),(10,'Can change mouth',3,'change_mouth'),(11,'Can delete mouth',3,'delete_mouth'),(12,'Can view mouth',3,'view_mouth'),(13,'Can add nose',4,'add_nose'),(14,'Can change nose',4,'change_nose'),(15,'Can delete nose',4,'delete_nose'),(16,'Can view nose',4,'view_nose'),(17,'Can add eyebrow',5,'add_eyebrow'),(18,'Can change eyebrow',5,'change_eyebrow'),(19,'Can delete eyebrow',5,'delete_eyebrow'),(20,'Can view eyebrow',5,'view_eyebrow'),(21,'Can add eye',6,'add_eye'),(22,'Can change eye',6,'change_eye'),(23,'Can delete eye',6,'delete_eye'),(24,'Can view eye',6,'view_eye'),(25,'Can add log entry',7,'add_logentry'),(26,'Can change log entry',7,'change_logentry'),(27,'Can delete log entry',7,'delete_logentry'),(28,'Can view log entry',7,'view_logentry'),(29,'Can add permission',8,'add_permission'),(30,'Can change permission',8,'change_permission'),(31,'Can delete permission',8,'delete_permission'),(32,'Can view permission',8,'view_permission'),(33,'Can add group',9,'add_group'),(34,'Can change group',9,'change_group'),(35,'Can delete group',9,'delete_group'),(36,'Can view group',9,'view_group'),(37,'Can add user',10,'add_user'),(38,'Can change user',10,'change_user'),(39,'Can delete user',10,'delete_user'),(40,'Can view user',10,'view_user'),(41,'Can add content type',11,'add_contenttype'),(42,'Can change content type',11,'change_contenttype'),(43,'Can delete content type',11,'delete_contenttype'),(44,'Can view content type',11,'view_contenttype'),(45,'Can add user',12,'add_user'),(46,'Can change user',12,'change_user'),(47,'Can delete user',12,'delete_user'),(48,'Can view user',12,'view_user'),(49,'Can add blacklist',13,'add_blacklist'),(50,'Can change blacklist',13,'change_blacklist'),(51,'Can delete blacklist',13,'delete_blacklist'),(52,'Can view blacklist',13,'view_blacklist'),(53,'Can add event',14,'add_event'),(54,'Can change event',14,'change_event'),(55,'Can delete event',14,'delete_event'),(56,'Can view event',14,'view_event'),(57,'Can add report',15,'add_report'),(58,'Can change report',15,'change_report'),(59,'Can delete report',15,'delete_report'),(60,'Can view report',15,'view_report'),(61,'Can add session',16,'add_session'),(62,'Can change session',16,'change_session'),(63,'Can delete session',16,'delete_session'),(64,'Can view session',16,'view_session'),(65,'Can add blacklist category',17,'add_blacklistcategory'),(66,'Can change blacklist category',17,'change_blacklistcategory'),(67,'Can delete blacklist category',17,'delete_blacklistcategory'),(68,'Can view blacklist category',17,'view_blacklistcategory'),(69,'Can add report category',18,'add_reportcategory'),(70,'Can change report category',18,'change_reportcategory'),(71,'Can delete report category',18,'delete_reportcategory'),(72,'Can view report category',18,'view_reportcategory'),(73,'Can add setting',19,'add_setting'),(74,'Can change setting',19,'change_setting'),(75,'Can delete setting',19,'delete_setting'),(76,'Can view setting',19,'view_setting'),(77,'Can add Token',20,'add_token'),(78,'Can change Token',20,'change_token'),(79,'Can delete Token',20,'delete_token'),(80,'Can view Token',20,'view_token'),(81,'Can add Token',21,'add_tokenproxy'),(82,'Can change Token',21,'change_tokenproxy'),(83,'Can delete Token',21,'delete_tokenproxy'),(84,'Can view Token',21,'view_tokenproxy'),(85,'Can add matching',22,'add_matching'),(86,'Can change matching',22,'change_matching'),(87,'Can delete matching',22,'delete_matching'),(88,'Can view matching',22,'view_matching'),(89,'Can add face analysis',23,'add_faceanalysis'),(90,'Can change face analysis',23,'change_faceanalysis'),(91,'Can delete face analysis',23,'delete_faceanalysis'),(92,'Can view face analysis',23,'view_faceanalysis'),(93,'Can add eye comb',24,'add_eyecomb'),(94,'Can change eye comb',24,'change_eyecomb'),(95,'Can delete eye comb',24,'delete_eyecomb'),(96,'Can view eye comb',24,'view_eyecomb'),(97,'Can add eyebrow comb',25,'add_eyebrowcomb'),(98,'Can change eyebrow comb',25,'change_eyebrowcomb'),(99,'Can delete eyebrow comb',25,'delete_eyebrowcomb'),(100,'Can view eyebrow comb',25,'view_eyebrowcomb'),(101,'Can add mouth comb',26,'add_mouthcomb'),(102,'Can change mouth comb',26,'change_mouthcomb'),(103,'Can delete mouth comb',26,'delete_mouthcomb'),(104,'Can view mouth comb',26,'view_mouthcomb'),(105,'Can add nose comb',27,'add_nosecomb'),(106,'Can change nose comb',27,'change_nosecomb'),(107,'Can delete nose comb',27,'delete_nosecomb'),(108,'Can view nose comb',27,'view_nosecomb'),(109,'Can add chin comb',28,'add_chincomb'),(110,'Can change chin comb',28,'change_chincomb'),(111,'Can delete chin comb',28,'delete_chincomb'),(112,'Can view chin comb',28,'view_chincomb'),(113,'Can add chat room',29,'add_chatroom'),(114,'Can change chat room',29,'change_chatroom'),(115,'Can delete chat room',29,'delete_chatroom'),(116,'Can view chat room',29,'view_chatroom'),(117,'Can add message',30,'add_message'),(118,'Can change message',30,'change_message'),(119,'Can delete message',30,'delete_message'),(120,'Can view message',30,'view_message'),(121,'Can add django job',31,'add_djangojob'),(122,'Can change django job',31,'change_djangojob'),(123,'Can delete django job',31,'delete_djangojob'),(124,'Can view django job',31,'view_djangojob'),(125,'Can add django job execution',32,'add_djangojobexecution'),(126,'Can change django job execution',32,'change_djangojobexecution'),(127,'Can delete django job execution',32,'delete_djangojobexecution'),(128,'Can view django job execution',32,'view_djangojobexecution');
/*!40000 ALTER TABLE `auth_permission` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:54

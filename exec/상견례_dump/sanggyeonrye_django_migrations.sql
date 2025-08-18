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
-- Table structure for table `django_migrations`
--

DROP TABLE IF EXISTS `django_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_migrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_migrations`
--

LOCK TABLES `django_migrations` WRITE;
/*!40000 ALTER TABLE `django_migrations` DISABLE KEYS */;
INSERT INTO `django_migrations` VALUES (1,'contenttypes','0001_initial','2025-07-28 07:22:50.450341'),(2,'auth','0001_initial','2025-07-28 07:22:50.489263'),(3,'admin','0001_initial','2025-07-28 07:22:50.540189'),(4,'admin','0002_logentry_remove_auto_add','2025-07-28 07:22:50.561937'),(5,'admin','0003_logentry_add_action_flag_choices','2025-07-28 07:22:50.594416'),(6,'articles','0001_initial','2025-07-28 07:22:50.627933'),(7,'articles','0002_alter_chin_options_alter_eye_options_and_more','2025-07-28 07:22:50.654424'),(8,'articles','0003_alter_chin_table_alter_eye_table_alter_eyebrow_table_and_more','2025-07-28 07:31:21.171126'),(9,'contenttypes','0002_remove_content_type_name','2025-07-29 01:40:55.459674'),(10,'auth','0002_alter_permission_name_max_length','2025-07-29 01:40:55.542291'),(11,'auth','0003_alter_user_email_max_length','2025-07-29 01:40:55.596555'),(12,'auth','0004_alter_user_username_opts','2025-07-29 01:40:55.616947'),(13,'auth','0005_alter_user_last_login_null','2025-07-29 01:40:55.671022'),(14,'auth','0006_require_contenttypes_0002','2025-07-29 01:40:55.683208'),(15,'auth','0007_alter_validators_add_error_messages','2025-07-29 01:40:55.712923'),(16,'auth','0008_alter_user_username_max_length','2025-07-29 01:40:55.769417'),(17,'auth','0009_alter_user_last_name_max_length','2025-07-29 01:40:55.819006'),(18,'auth','0010_alter_group_name_max_length','2025-07-29 01:40:55.872215'),(19,'auth','0011_update_proxy_permissions','2025-07-29 01:40:55.933070'),(20,'auth','0012_alter_user_first_name_max_length','2025-07-29 01:40:55.993465'),(21,'adminpage','0001_initial','2025-07-29 07:59:55.793903'),(22,'sessions','0001_initial','2025-07-29 08:26:36.348154'),(23,'authtoken','0001_initial','2025-07-31 05:19:41.681479'),(24,'authtoken','0002_auto_20160226_1747','2025-07-31 05:19:41.731752'),(25,'authtoken','0003_tokenproxy','2025-07-31 05:19:41.750776'),(26,'authtoken','0004_alter_tokenproxy_options','2025-07-31 05:19:41.772852'),(27,'django_apscheduler','0001_initial','2025-08-08 05:50:29.573702'),(28,'django_apscheduler','0002_auto_20180412_0758','2025-08-08 05:50:29.689763'),(29,'django_apscheduler','0003_auto_20200716_1632','2025-08-08 05:50:29.722030'),(30,'django_apscheduler','0004_auto_20200717_1043','2025-08-08 05:50:30.232841'),(31,'django_apscheduler','0005_migrate_name_to_id','2025-08-08 05:50:30.307612'),(32,'django_apscheduler','0006_remove_djangojob_name','2025-08-08 05:50:30.442277'),(33,'django_apscheduler','0007_auto_20200717_1404','2025-08-08 05:50:30.599293'),(34,'django_apscheduler','0008_remove_djangojobexecution_started','2025-08-08 05:50:30.698310'),(35,'django_apscheduler','0009_djangojobexecution_unique_job_executions','2025-08-08 05:50:30.780430');
/*!40000 ALTER TABLE `django_migrations` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:31:03

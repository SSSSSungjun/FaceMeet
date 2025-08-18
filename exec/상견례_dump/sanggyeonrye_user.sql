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
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(250) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `nickname` varchar(45) DEFAULT NULL,
  `gender` enum('f','m','u') DEFAULT NULL,
  `face_id` bigint DEFAULT NULL,
  `birth` datetime DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `is_online` bit(1) DEFAULT NULL,
  `last_seen` datetime DEFAULT NULL,
  `prefer_age_lower` int DEFAULT NULL,
  `prefer_age_upper` int DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime NOT NULL,
  `provider` varchar(255) NOT NULL,
  `social_id` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  KEY `fk_user_face1_idx` (`face_id`),
  CONSTRAINT `fk_user_face1` FOREIGN KEY (`face_id`) REFERENCES `face` (`face_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 16:19:23','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(2,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 16:19:57','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(3,'prgrm0505@gmail.com','USER','김소은','기못은','m',NULL,'2001-10-08 00:00:00','대한민국 경상북도 구미시 임수동 94-1',36.1071347,128.4164602,_binary '','2025-08-15 20:55:09',20,36,_binary '\0','2025-08-14 16:19:57','kakao','4371921810'),(4,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 16:20:11','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(5,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 16:21:30','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(6,'rlaeodrkd@naver.com','ADMIN','김소은','조조','f',NULL,'2001-06-25 00:00:00','대한민국 구미시 인동사거리',36.1068536,128.4189682,_binary '','2025-08-17 22:36:41',20,35,_binary '\0','2025-08-14 16:43:03','naver','zD3FepSVnPFGsx_NC-azWbgqdQu0EhCJkTH2c_Y1vG0'),(7,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 17:08:43','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(8,'deleted_user@facemeet.deleted','ADMIN',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 17:09:46','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(9,'gunbam715@gmail.com','USER','천지윤',NULL,'f',NULL,'2000-07-15 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0','2025-08-14 17:12:57','kakao','4369119443'),(10,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 17:16:15','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(11,'kgggri3114@naver.com','ADMIN','김규미','미미','f',63,'1998-07-14 00:00:00','대한민국 경상북도 구미시 진평동 518-2',36.1026194380587,128.41936245560646,_binary '\0','2025-08-18 08:29:54',27,34,_binary '\0','2025-08-14 17:17:00','naver','GVTB8-FALahzwGrgVDl4Lo5yMjahnqnwpT7IusdXq8g'),(12,'sso7894@naver.com','USER','박세은','윤석열','f',NULL,'2002-08-13 00:00:00','대한민국 대전광역시 서구 월평동 837',36.3552564,127.3642821,_binary '\0','2025-08-16 16:59:35',20,65,_binary '\0','2025-08-14 17:24:28','naver','Q4Hqlzddjob1BL7TYe5AKYTnFdsMpWzGSXlVlYTOdw8'),(13,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-14 17:35:31','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(14,'asj1580@naver.com','USER','안소정','ㄱ.이재용','f',NULL,'2002-05-23 00:00:00','대한민국 경상북도 포항시 북구 천마로 161',36.08977464810569,129.39615324139595,_binary '\0','2025-08-14 18:52:05',62,65,_binary '\0','2025-08-14 17:36:07','naver','cMboVJTimoNFvDDCmRgZVwg_8IbOokloZeqMVTWMJvY'),(16,'cofl1748@naver.com','USER','김동현','닉네임','m',NULL,'2002-03-17 00:00:00','대한민국 경상북도 구미시 옥계남로 76-23',36.1361526,128.426827,_binary '\0','2025-08-15 16:50:36',20,65,_binary '\0','2025-08-14 18:35:17','naver','nsi_hNGzkzjZT3xcYNDLLQW73zjdIPqvI3-H2vyUsfk'),(17,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-15 10:34:10','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(18,'ssatester23@gmail.com','ADMIN','김관상',NULL,'f',NULL,'2001-01-01 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0','2025-08-15 10:41:42','kakao','4396613063'),(19,'gunbam715@naver.com','USER','천지윤','긴닉네임은 어디까지 되는지 실험해보고 싶었어요. 띄어쓰기도 잘 되는지, 특수문자','f',61,'2000-07-15 00:00:00','대한민국 과천시 과천동 363-1번지 C동 101호 과천시 경기도 KR',37.4533634,127.0025949,_binary '','2025-08-18 01:47:07',20,65,_binary '\0','2025-08-15 15:00:35','naver','hpUvdtT15ftzx3JBbKkz2Fe7GnboJiRdgOz7MgLA6ns'),(20,'schabc8436@naver.com','USER','박진영',NULL,'f',NULL,'1979-12-29 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '\0','2025-08-15 15:44:09','naver','IH1K-y2A9B7pceBgiy5WU1kAqSdoIt4nZmJxyYbQG7M'),(21,'sungung1026@naver.com','USER','박성준','윤성쥰','m',NULL,'1999-10-26 00:00:00','대한민국 경상북도 구미시 진평동 1039-2',36.1022773,128.4223043,_binary '\0','2025-08-15 23:34:39',20,31,_binary '\0','2025-08-15 23:29:54','naver','hGfIDTD-w1iSsv-rsVl8RR21NqJnrvithGxRP2JX408'),(22,'juniprime@naver.com','USER','윤성준','구미5반 차은우','m',42,'1999-01-31 00:00:00','대한민국 경기도 화성시 동탄신리천로1길 66 (창의고등학교)',37.1801521,127.1209476,_binary '\0','2025-08-18 07:20:28',21,49,_binary '\0','2025-08-16 19:44:52','kakao','4362070807'),(23,'chohui5272@naver.com','USER','손초희','손초희네이버','f',NULL,'2002-04-15 00:00:00','대한민국 경상북도 구미시 인동가산로 22',36.1065851,128.4184092,_binary '\0','2025-08-17 19:33:32',20,65,_binary '\0','2025-08-17 19:29:28','naver','tjU7OV90fU8wKTUYJe4WwS9kg_BY0DJNYAXm8wm4NGg'),(24,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 19:54:33','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(25,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 19:59:08','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(26,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 20:55:08','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(27,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 21:21:10','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(28,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 21:53:49','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(29,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 21:55:52','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(30,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 21:58:40','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(31,'deleted_user@facemeet.deleted','USER',NULL,'(알 수 없음)','u',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '','2025-08-17 22:00:51','deleted_user@facemeet.deleted','deleted_user@facemeet.deleted'),(32,'rlaeodrkd@naver.com','USER','김소은','손초희바보똥개','f',17,'2001-06-25 00:00:00','대한민국 구미시 인동사거리',36.1068457,128.4189452,_binary '\0','2025-08-18 01:01:50',20,46,_binary '\0','2025-08-17 22:06:00','kakao','4360436801'),(34,'1__________999@naver.com','USER','윤성준','구미 에겐보이즈 대장','f',62,'1999-01-31 00:00:00','대한민국 경상북도 구미시 진평동 628-56',36.1012515,128.4195828,_binary '\0','2025-08-18 07:20:22',32,48,_binary '\0','2025-08-17 22:35:13','naver','TTKCgUqk3BgIRpM0X_3XdTmW8e-m6dCFUKL6kua-CMI'),(35,'schabc8436@daum.net','USER','손초희','구미3반 장원영','f',36,'2002-04-15 00:00:00','대한민국 경상북도 구미시 진평동 1026-3',36.1062398,128.4187608,_binary '\0','2025-08-17 22:53:38',20,65,_binary '\0','2025-08-17 22:35:56','kakao','4360547796');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
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

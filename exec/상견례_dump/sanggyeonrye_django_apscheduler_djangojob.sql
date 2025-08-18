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
-- Table structure for table `django_apscheduler_djangojob`
--

DROP TABLE IF EXISTS `django_apscheduler_djangojob`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_apscheduler_djangojob` (
  `id` varchar(255) NOT NULL,
  `next_run_time` datetime(6) DEFAULT NULL,
  `job_state` longblob NOT NULL,
  PRIMARY KEY (`id`),
  KEY `django_apscheduler_djangojob_next_run_time_2f022619` (`next_run_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_apscheduler_djangojob`
--

LOCK TABLES `django_apscheduler_djangojob` WRITE;
/*!40000 ALTER TABLE `django_apscheduler_djangojob` DISABLE KEYS */;
INSERT INTO `django_apscheduler_djangojob` VALUES ('append-dataset-midnight','2025-08-19 00:00:00.000000',_binary '€•+\0\0\0\0\0\0}”(Œversion”KŒid”Œappend-dataset-midnight”Œfunc”Œ$articles.jobs:run_append_dataset_job”Œtrigger”Œapscheduler.triggers.cron”ŒCronTrigger”“”)”}”(hKŒtimezone”Œbuiltins”Œgetattr”“”Œzoneinfo”ŒZoneInfo”“”Œ	_unpickle”†”R”Œ\nAsia/Seoul”K†”R”Œ\nstart_date”NŒend_date”NŒfields”]”(Œ apscheduler.triggers.cron.fields”Œ	BaseField”“”)”}”(Œname”Œyear”Œ\nis_default”ˆŒexpressions”]”Œ%apscheduler.triggers.cron.expressions”Œ\rAllExpression”“”)”}”Œstep”NsbaubhŒ\nMonthField”“”)”}”(h\"Œmonth”h$ˆh%]”h))”}”h,NsbaubhŒDayOfMonthField”“”)”}”(h\"Œday”h$ˆh%]”h))”}”h,NsbaubhŒ	WeekField”“”)”}”(h\"Œweek”h$ˆh%]”h))”}”h,NsbaubhŒDayOfWeekField”“”)”}”(h\"Œday_of_week”h$ˆh%]”h))”}”h,Nsbaubh)”}”(h\"Œhour”h$‰h%]”h\'ŒRangeExpression”“”)”}”(h,NŒfirst”K\0Œlast”K\0ubaubh)”}”(h\"Œminute”h$‰h%]”hR)”}”(h,NhUK\0hVK\0ubaubh)”}”(h\"Œsecond”h$‰h%]”hR)”}”(h,NhUK\0hVK\0ubaubeŒjitter”NubŒexecutor”Œdefault”Œargs”)Œkwargs”}”h\"Œrun_append_dataset_job”Œmisfire_grace_time”MXŒcoalesce”ˆŒ\rmax_instances”KŒ\rnext_run_time”Œdatetime”Œdatetime”“”C\n\é\0\0\0\0\0\0”h†”R”u.'),('sentiment-midnight','2025-08-19 00:00:00.000000',_binary '€•\0\0\0\0\0\0}”(Œversion”KŒid”Œsentiment-midnight”Œfunc”Œarticles.jobs:run_sentiment_job”Œtrigger”Œapscheduler.triggers.cron”ŒCronTrigger”“”)”}”(hKŒtimezone”Œbuiltins”Œgetattr”“”Œzoneinfo”ŒZoneInfo”“”Œ	_unpickle”†”R”Œ\nAsia/Seoul”K†”R”Œ\nstart_date”NŒend_date”NŒfields”]”(Œ apscheduler.triggers.cron.fields”Œ	BaseField”“”)”}”(Œname”Œyear”Œ\nis_default”ˆŒexpressions”]”Œ%apscheduler.triggers.cron.expressions”Œ\rAllExpression”“”)”}”Œstep”NsbaubhŒ\nMonthField”“”)”}”(h\"Œmonth”h$ˆh%]”h))”}”h,NsbaubhŒDayOfMonthField”“”)”}”(h\"Œday”h$ˆh%]”h))”}”h,NsbaubhŒ	WeekField”“”)”}”(h\"Œweek”h$ˆh%]”h))”}”h,NsbaubhŒDayOfWeekField”“”)”}”(h\"Œday_of_week”h$ˆh%]”h))”}”h,Nsbaubh)”}”(h\"Œhour”h$‰h%]”h\'ŒRangeExpression”“”)”}”(h,NŒfirst”K\0Œlast”K\0ubaubh)”}”(h\"Œminute”h$‰h%]”hR)”}”(h,NhUK\0hVK\0ubaubh)”}”(h\"Œsecond”h$‰h%]”hR)”}”(h,NhUK\0hVK\0ubaubeŒjitter”NubŒexecutor”Œdefault”Œargs”)Œkwargs”}”h\"Œrun_sentiment_job”Œmisfire_grace_time”MXŒcoalesce”ˆŒ\rmax_instances”KŒ\rnext_run_time”Œdatetime”Œdatetime”“”C\n\é\0\0\0\0\0\0”h†”R”u.'),('update-train-midnight','2025-08-19 00:00:00.000000',_binary '€•-\0\0\0\0\0\0}”(Œversion”KŒid”Œupdate-train-midnight”Œfunc”Œ&articles.jobs:run_update_and_train_job”Œtrigger”Œapscheduler.triggers.cron”ŒCronTrigger”“”)”}”(hKŒtimezone”Œbuiltins”Œgetattr”“”Œzoneinfo”ŒZoneInfo”“”Œ	_unpickle”†”R”Œ\nAsia/Seoul”K†”R”Œ\nstart_date”NŒend_date”NŒfields”]”(Œ apscheduler.triggers.cron.fields”Œ	BaseField”“”)”}”(Œname”Œyear”Œ\nis_default”ˆŒexpressions”]”Œ%apscheduler.triggers.cron.expressions”Œ\rAllExpression”“”)”}”Œstep”NsbaubhŒ\nMonthField”“”)”}”(h\"Œmonth”h$ˆh%]”h))”}”h,NsbaubhŒDayOfMonthField”“”)”}”(h\"Œday”h$ˆh%]”h))”}”h,NsbaubhŒ	WeekField”“”)”}”(h\"Œweek”h$ˆh%]”h))”}”h,NsbaubhŒDayOfWeekField”“”)”}”(h\"Œday_of_week”h$ˆh%]”h))”}”h,Nsbaubh)”}”(h\"Œhour”h$‰h%]”h\'ŒRangeExpression”“”)”}”(h,NŒfirst”K\0Œlast”K\0ubaubh)”}”(h\"Œminute”h$‰h%]”hR)”}”(h,NhUK\0hVK\0ubaubh)”}”(h\"Œsecond”h$‰h%]”hR)”}”(h,NhUK\0hVK\0ubaubeŒjitter”NubŒexecutor”Œdefault”Œargs”)Œkwargs”}”h\"Œrun_update_and_train_job”Œmisfire_grace_time”MŒcoalesce”ˆŒ\rmax_instances”KŒ\rnext_run_time”Œdatetime”Œdatetime”“”C\n\é\0\0\0\0\0\0”h†”R”u.');
/*!40000 ALTER TABLE `django_apscheduler_djangojob` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-18  8:30:48

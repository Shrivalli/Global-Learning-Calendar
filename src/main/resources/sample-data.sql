-- ============================================================================
-- Global Learning Calendar - Sample Data Insert Script
-- MySQL 9.5 Compatible
-- Generated: 2025-12-02
-- ============================================================================
-- This script contains sample data for all entities in the system.
-- Run these statements in order to maintain referential integrity.
-- ============================================================================

-- Optional: Uncomment to clear existing data before inserting
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE `bookings`;
-- TRUNCATE TABLE `learning_sessions`;
-- TRUNCATE TABLE `program_target_business_units`;
-- TRUNCATE TABLE `program_target_roles`;
-- TRUNCATE TABLE `program_skills`;
-- TRUNCATE TABLE `learning_programs`;
-- TRUNCATE TABLE `users`;
-- TRUNCATE TABLE `locations`;
-- TRUNCATE TABLE `business_units`;
-- TRUNCATE TABLE `skills`;
-- TRUNCATE TABLE `roles`;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 1. ROLES - Insert lookup roles
-- ============================================================================
INSERT INTO `roles` (`id`,`code`,`name`,`description`,`role_type`,`is_active`,`created_at`,`updated_at`) VALUES
(1,'ROLE_EMP','Employee','Regular employee role','EMPLOYEE',1,'2025-11-01 08:00:00','2025-11-01 08:00:00'),
(2,'ROLE_MGR','Manager','People manager role','MANAGER',1,'2025-11-01 08:00:00','2025-11-01 08:00:00'),
(3,'ROLE_BU_LEADER','BU Leader','Business unit leader','BU_LEADER',1,'2025-11-01 08:00:00','2025-11-01 08:00:00'),
(4,'ROLE_LD_LEADER','L&D Leader','Learning and development leader','LD_LEADER',1,'2025-11-01 08:00:00','2025-11-01 08:00:00'),
(5,'ROLE_LD_ADMIN','L&D Admin','Learning and development administrator','LD_ADMIN',1,'2025-11-01 08:00:00','2025-11-01 08:00:00'),
(6,'ROLE_SYS_ADMIN','System Admin','System administrator','SYSTEM_ADMIN',1,'2025-11-01 08:00:00','2025-11-01 08:00:00');

-- ============================================================================
-- 2. SKILLS - Insert skills catalog
-- ============================================================================
INSERT INTO `skills` (`id`,`code`,`name`,`description`,`skill_category`,`is_active`,`created_at`,`updated_at`) VALUES
(1,'SKL_JAVA','Java Programming','Core Java and advanced concepts','TECHNICAL',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(2,'SKL_PYTHON','Python Programming','Python development and scripting','TECHNICAL',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(3,'SKL_JS','JavaScript','Modern JavaScript and ES6+','TECHNICAL',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(4,'SKL_REACT','React Framework','React.js front-end development','TECHNICAL',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(5,'SKL_SPRING','Spring Boot','Spring Boot framework','TECHNICAL',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(6,'SKL_COMM','Communication','Effective communication skills','SOFT_SKILLS',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(7,'SKL_LEAD','Leadership','Leadership and team management','SOFT_SKILLS',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(8,'SKL_PM','Project Management','Project management fundamentals','MANAGEMENT',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(9,'SKL_AGILE','Agile Methodology','Scrum and Agile practices','MANAGEMENT',1,'2025-11-01 08:05:00','2025-11-01 08:05:00'),
(10,'SKL_SQL','SQL Database','SQL and database design','TECHNICAL',1,'2025-11-01 08:05:00','2025-11-01 08:05:00');

-- ============================================================================
-- 3. BUSINESS UNITS - Insert organizational structure
-- ============================================================================
INSERT INTO `business_units` (`id`,`code`,`name`,`description`,`parent_bu_id`,`is_active`,`created_at`,`updated_at`) VALUES
(1,'BU_CORP','Corporate','Corporate headquarters',NULL,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(2,'BU_TECH','Technology','Technology division',1,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(3,'BU_DEV','Development','Software development team',2,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(4,'BU_QA','Quality Assurance','QA and testing team',2,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(5,'BU_SALES','Sales','Sales division',1,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(6,'BU_MARKET','Marketing','Marketing division',1,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(7,'BU_HR','Human Resources','HR and people operations',1,1,'2025-11-01 08:10:00','2025-11-01 08:10:00'),
(8,'BU_LD','Learning & Development','L&D department',7,1,'2025-11-01 08:10:00','2025-11-01 08:10:00');

-- ============================================================================
-- 4. LOCATIONS - Insert office locations
-- ============================================================================
INSERT INTO `locations` (`id`,`name`,`city`,`country`,`region`,`timezone`,`address`,`capacity`,`is_active`,`created_at`,`updated_at`) VALUES
(1,'Headquarters','New York','USA','Americas','America/New_York','123 Main St, New York, NY 10001',200,1,'2025-11-01 08:15:00','2025-11-01 08:15:00'),
(2,'Tech Campus','San Francisco','USA','Americas','America/Los_Angeles','456 Tech Ave, San Francisco, CA 94102',150,1,'2025-11-01 08:15:00','2025-11-01 08:15:00'),
(3,'London Office','London','UK','EMEA','Europe/London','789 Oxford St, London W1D 2HH',100,1,'2025-11-01 08:15:00','2025-11-01 08:15:00'),
(4,'Singapore Hub','Singapore','Singapore','APAC','Asia/Singapore','321 Marina Bay, Singapore 018956',80,1,'2025-11-01 08:15:00','2025-11-01 08:15:00'),
(5,'Remote','Remote','Global',NULL,'UTC','Virtual',0,1,'2025-11-01 08:15:00','2025-11-01 08:15:00');

-- ============================================================================
-- 5. USERS - Insert users (managers first, then employees)
-- ============================================================================
-- System administrators and L&D staff
INSERT INTO `users` (`id`,`employee_id`,`email`,`first_name`,`last_name`,`role_id`,`business_unit_id`,`location_id`,`manager_id`,`job_title`,`is_active`,`created_at`,`updated_at`) VALUES
(1,'E1000','admin@globallearning.com','System','Administrator',6,1,1,NULL,'System Administrator',1,'2025-11-01 09:00:00','2025-11-01 09:00:00'),
(2,'E1001','ld.leader@globallearning.com','Sarah','Johnson',4,8,1,NULL,'L&D Director',1,'2025-11-01 09:00:00','2025-11-01 09:00:00'),
(3,'E1002','ld.admin@globallearning.com','Mike','Chen',5,8,1,2,'L&D Administrator',1,'2025-11-01 09:00:00','2025-11-01 09:00:00');

-- Business unit leaders
INSERT INTO `users` (`id`,`employee_id`,`email`,`first_name`,`last_name`,`role_id`,`business_unit_id`,`location_id`,`manager_id`,`job_title`,`is_active`,`created_at`,`updated_at`) VALUES
(4,'E2001','tech.lead@globallearning.com','David','Martinez',3,2,2,1,'VP of Technology',1,'2025-11-01 09:05:00','2025-11-01 09:05:00'),
(5,'E2002','sales.lead@globallearning.com','Jennifer','Williams',3,5,1,1,'VP of Sales',1,'2025-11-01 09:05:00','2025-11-01 09:05:00');

-- Managers
INSERT INTO `users` (`id`,`employee_id`,`email`,`first_name`,`last_name`,`role_id`,`business_unit_id`,`location_id`,`manager_id`,`job_title`,`is_active`,`created_at`,`updated_at`) VALUES
(6,'E3001','dev.manager@globallearning.com','Robert','Taylor',2,3,2,4,'Development Manager',1,'2025-11-01 09:10:00','2025-11-01 09:10:00'),
(7,'E3002','qa.manager@globallearning.com','Lisa','Anderson',2,4,2,4,'QA Manager',1,'2025-11-01 09:10:00','2025-11-01 09:10:00'),
(8,'E3003','sales.manager@globallearning.com','James','Brown',2,5,1,5,'Sales Manager',1,'2025-11-01 09:10:00','2025-11-01 09:10:00');

-- Regular employees
INSERT INTO `users` (`id`,`employee_id`,`email`,`first_name`,`last_name`,`role_id`,`business_unit_id`,`location_id`,`manager_id`,`job_title`,`is_active`,`created_at`,`updated_at`) VALUES
(9,'E4001','alice.dev@globallearning.com','Alice','Johnson',1,3,2,6,'Senior Software Engineer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(10,'E4002','bob.dev@globallearning.com','Bob','Smith',1,3,2,6,'Software Engineer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(11,'E4003','charlie.dev@globallearning.com','Charlie','Davis',1,3,2,6,'Junior Developer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(12,'E4004','diana.qa@globallearning.com','Diana','Wilson',1,4,2,7,'QA Engineer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(13,'E4005','emma.qa@globallearning.com','Emma','Moore',1,4,2,7,'Test Automation Engineer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(14,'E4006','frank.sales@globallearning.com','Frank','Taylor',1,5,1,8,'Sales Representative',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(15,'E4007','grace.sales@globallearning.com','Grace','White',1,5,1,8,'Account Executive',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(16,'E4008','henry.dev@globallearning.com','Henry','Lee',1,3,3,6,'Full Stack Developer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(17,'E4009','isabel.dev@globallearning.com','Isabel','Garcia',1,3,4,6,'Backend Developer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00'),
(18,'E4010','jack.sales@globallearning.com','Jack','Martinez',1,5,3,8,'Sales Engineer',1,'2025-11-01 09:15:00','2025-11-01 09:15:00');

-- ============================================================================
-- 6. LEARNING PROGRAMS - Insert training programs
-- ============================================================================
INSERT INTO `learning_programs` (`id`,`code`,`name`,`description`,`program_type`,`delivery_mode`,`duration_hours`,`created_by`,`is_mandatory`,`is_active`,`created_at`,`updated_at`) VALUES
(1,'LP-JAVA-101','Java Fundamentals','Introduction to Java programming covering core concepts, OOP, and basic frameworks','TECHNICAL','IN_PERSON',16,2,0,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(2,'LP-JAVA-201','Advanced Java','Advanced Java topics including concurrency, JVM internals, and performance tuning','TECHNICAL','HYBRID',24,2,0,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(3,'LP-SPRING-101','Spring Boot Basics','Getting started with Spring Boot framework for microservices development','TECHNICAL','VIRTUAL',12,2,0,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(4,'LP-REACT-101','React Fundamentals','Learn React.js for building modern web applications','TECHNICAL','IN_PERSON',16,2,0,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(5,'LP-COMM-SKILLS','Communication Excellence','Effective communication skills for technical professionals','SOFT_SKILLS','VIRTUAL',8,2,1,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(6,'LP-LEADERSHIP','Leadership Essentials','Core leadership skills for new managers','LEADERSHIP','HYBRID',16,2,0,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(7,'LP-AGILE-SCRUM','Agile & Scrum','Agile methodology and Scrum framework for project management','WORKSHOP','IN_PERSON',8,2,1,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(8,'LP-SQL-DB','Database Design','SQL and database design fundamentals','TECHNICAL','VIRTUAL',12,2,0,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(9,'LP-ONBOARDING','New Hire Onboarding','Comprehensive onboarding program for new employees','ONBOARDING','HYBRID',4,2,1,1,'2025-11-05 10:00:00','2025-11-05 10:00:00'),
(10,'LP-COMPLIANCE','Compliance Training','Annual compliance and ethics training','COMPLIANCE','SELF_PACED',2,2,1,1,'2025-11-05 10:00:00','2025-11-05 10:00:00');

-- ============================================================================
-- 7. PROGRAM_SKILLS - Map programs to skills (many-to-many)
-- ============================================================================
INSERT INTO `program_skills` (`program_id`,`skill_id`) VALUES
-- Java Fundamentals
(1,1),(1,10),
-- Advanced Java
(2,1),(2,5),
-- Spring Boot Basics
(3,1),(3,5),(3,10),
-- React Fundamentals
(4,3),(4,4),
-- Communication Excellence
(5,6),
-- Leadership Essentials
(6,7),(6,6),
-- Agile & Scrum
(7,8),(7,9),
-- Database Design
(8,10),
-- New Hire Onboarding (multiple skills)
(9,6),(9,8),
-- Compliance Training
(10,6);

-- ============================================================================
-- 8. PROGRAM_TARGET_ROLES - Map programs to target roles
-- ============================================================================
INSERT INTO `program_target_roles` (`program_id`,`role_id`) VALUES
-- Technical programs target employees and managers
(1,1),(1,2),
(2,1),(2,2),
(3,1),(3,2),
(4,1),(4,2),
(8,1),(8,2),
-- Communication for all roles
(5,1),(5,2),(5,3),(5,4),(5,5),(5,6),
-- Leadership for managers and above
(6,2),(6,3),(6,4),
-- Agile for employees and managers
(7,1),(7,2),(7,3),
-- Onboarding for all new hires
(9,1),(9,2),(9,3),(9,4),(9,5),
-- Compliance for everyone
(10,1),(10,2),(10,3),(10,4),(10,5),(10,6);

-- ============================================================================
-- 9. PROGRAM_TARGET_BUSINESS_UNITS - Map programs to target BUs
-- ============================================================================
INSERT INTO `program_target_business_units` (`program_id`,`business_unit_id`) VALUES
-- Technical programs for Technology division
(1,2),(1,3),(1,4),
(2,2),(2,3),
(3,2),(3,3),
(4,2),(4,3),
(8,2),(8,3),(8,4),
-- Communication for all BUs
(5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,7),(5,8),
-- Leadership for managers across BUs
(6,1),(6,2),(6,5),(6,6),(6,7),
-- Agile for tech and project teams
(7,2),(7,3),(7,4),(7,5),
-- Onboarding for all BUs
(9,1),(9,2),(9,3),(9,4),(9,5),(9,6),(9,7),(9,8),
-- Compliance for all BUs
(10,1),(10,2),(10,3),(10,4),(10,5),(10,6),(10,7),(10,8);

-- ============================================================================
-- 10. LEARNING SESSIONS - Insert scheduled sessions
-- ============================================================================
INSERT INTO `learning_sessions` (`id`,`session_code`,`program_id`,`location_id`,`start_date_time`,`end_date_time`,`total_seats`,`available_seats`,`waitlist_capacity`,`status`,`instructor_name`,`instructor_email`,`virtual_meeting_link`,`room_number`,`notes`,`created_by`,`is_active`,`created_at`,`updated_at`) VALUES
-- December 2025 sessions
(1,'SESS-2025-12-001',1,2,'2025-12-10 09:00:00','2025-12-12 17:00:00',20,15,5,'SCHEDULED','Dr. James Wilson','j.wilson@training.com',NULL,'TR-201','Bring laptop with JDK 17 installed',2,1,'2025-11-10 10:00:00','2025-11-10 10:00:00'),
(2,'SESS-2025-12-002',4,1,'2025-12-15 09:00:00','2025-12-17 17:00:00',15,8,3,'SCHEDULED','Sarah Kim','s.kim@training.com',NULL,'TR-105','Node.js and VS Code required',2,1,'2025-11-10 10:00:00','2025-11-10 10:00:00'),
(3,'SESS-2025-12-003',5,5,'2025-12-05 14:00:00','2025-12-05 18:00:00',50,25,10,'SCHEDULED','Prof. Linda Martinez','l.martinez@training.com','https://meet.globallearning.com/comm-skills-001',NULL,'Virtual session - camera required',2,1,'2025-11-10 10:00:00','2025-11-10 10:00:00'),
(4,'SESS-2025-12-004',7,1,'2025-12-08 09:00:00','2025-12-08 17:00:00',30,10,5,'SCHEDULED','Mark Thompson','m.thompson@training.com',NULL,'TR-301','Workshop format - active participation',2,1,'2025-11-10 10:00:00','2025-11-10 10:00:00'),

-- January 2026 sessions
(5,'SESS-2026-01-001',3,5,'2026-01-08 10:00:00','2026-01-10 16:00:00',25,20,5,'SCHEDULED','Alex Rodriguez','a.rodriguez@training.com','https://meet.globallearning.com/spring-boot-101',NULL,'Hands-on coding sessions',2,1,'2025-11-15 10:00:00','2025-11-15 10:00:00'),
(6,'SESS-2026-01-002',2,2,'2026-01-20 09:00:00','2026-01-23 17:00:00',15,15,3,'SCHEDULED','Dr. James Wilson','j.wilson@training.com',NULL,'TR-202','Prerequisites: Java Fundamentals',2,1,'2025-11-15 10:00:00','2025-11-15 10:00:00'),
(7,'SESS-2026-01-003',6,3,'2026-01-15 09:00:00','2026-01-17 17:00:00',20,12,5,'SCHEDULED','Rebecca Foster','r.foster@training.com','https://meet.globallearning.com/leadership-jan',NULL,'Hybrid: Day 1 virtual, Days 2-3 in-person',2,1,'2025-11-15 10:00:00','2025-11-15 10:00:00'),
(8,'SESS-2026-01-004',8,5,'2026-01-22 13:00:00','2026-01-24 17:00:00',30,25,5,'SCHEDULED','David Chen','d.chen@training.com','https://meet.globallearning.com/sql-db-101',NULL,'SQL Server and MySQL examples',2,1,'2025-11-15 10:00:00','2025-11-15 10:00:00'),

-- Completed session (November 2025)
(9,'SESS-2025-11-001',9,1,'2025-11-20 09:00:00','2025-11-20 13:00:00',30,0,0,'COMPLETED','Sarah Johnson','sarah.johnson@globallearning.com',NULL,'CONF-A','New hire orientation',2,1,'2025-11-01 10:00:00','2025-11-20 14:00:00'),

-- Cancelled session
(10,'SESS-2025-12-099',10,5,'2025-12-25 10:00:00','2025-12-25 12:00:00',100,100,0,'CANCELLED','Automated','noreply@globallearning.com','https://meet.globallearning.com/compliance-dec',NULL,'Cancelled due to holiday',2,0,'2025-11-10 10:00:00','2025-12-01 10:00:00');

-- ============================================================================
-- 11. BOOKINGS - Insert user bookings for sessions
-- ============================================================================
INSERT INTO `bookings` (`id`,`booking_reference`,`user_id`,`session_id`,`status`,`seat_number`,`waitlist_position`,`booking_date`,`confirmation_date`,`cancellation_date`,`cancellation_reason`,`attendance_status`,`attendance_marked_at`,`completion_status`,`completion_date`,`feedback_rating`,`feedback_comments`,`approved_by`,`approval_date`,`notes`,`created_at`,`updated_at`) VALUES
-- Session 1: Java Fundamentals (5 confirmed bookings out of 20 seats, 15 available)
(1,'BK-2025-11-0001',9,1,'CONFIRMED',1,NULL,'2025-11-12 09:30:00','2025-11-12 09:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-12 10:00:00','Approved by L&D Admin','2025-11-12 09:30:00','2025-11-12 09:30:00'),
(2,'BK-2025-11-0002',10,1,'CONFIRMED',2,NULL,'2025-11-12 10:00:00','2025-11-12 10:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-12 10:10:00','Approved by L&D Admin','2025-11-12 10:00:00','2025-11-12 10:00:00'),
(3,'BK-2025-11-0003',11,1,'CONFIRMED',3,NULL,'2025-11-12 11:00:00','2025-11-12 11:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-12 11:10:00','Approved by L&D Admin','2025-11-12 11:00:00','2025-11-12 11:00:00'),
(4,'BK-2025-11-0004',16,1,'CONFIRMED',4,NULL,'2025-11-13 08:30:00','2025-11-13 08:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-13 09:00:00','Approved by L&D Admin','2025-11-13 08:30:00','2025-11-13 08:30:00'),
(5,'BK-2025-11-0005',17,1,'CONFIRMED',5,NULL,'2025-11-13 09:30:00','2025-11-13 09:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-13 10:00:00','Approved by L&D Admin','2025-11-13 09:30:00','2025-11-13 09:30:00'),

-- Session 2: React Fundamentals (7 confirmed bookings out of 15 seats, 8 available)
(6,'BK-2025-11-0006',9,2,'CONFIRMED',1,NULL,'2025-11-14 10:00:00','2025-11-14 10:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-14 10:10:00','Frontend specialization','2025-11-14 10:00:00','2025-11-14 10:00:00'),
(7,'BK-2025-11-0007',10,2,'CONFIRMED',2,NULL,'2025-11-14 11:00:00','2025-11-14 11:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-14 11:10:00',NULL,'2025-11-14 11:00:00','2025-11-14 11:00:00'),
(8,'BK-2025-11-0008',11,2,'CONFIRMED',3,NULL,'2025-11-14 14:00:00','2025-11-14 14:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-14 14:10:00',NULL,'2025-11-14 14:00:00','2025-11-14 14:00:00'),
(9,'BK-2025-11-0009',16,2,'CONFIRMED',4,NULL,'2025-11-15 09:00:00','2025-11-15 09:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-15 09:10:00','Full stack path','2025-11-15 09:00:00','2025-11-15 09:00:00'),
(10,'BK-2025-11-0010',12,2,'CONFIRMED',5,NULL,'2025-11-15 10:30:00','2025-11-15 10:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-15 10:40:00','QA automation interest','2025-11-15 10:30:00','2025-11-15 10:30:00'),
(11,'BK-2025-11-0011',13,2,'CONFIRMED',6,NULL,'2025-11-15 11:00:00','2025-11-15 11:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-15 11:10:00','Test automation','2025-11-15 11:00:00','2025-11-15 11:00:00'),
(12,'BK-2025-11-0012',14,2,'CONFIRMED',7,NULL,'2025-11-15 14:00:00','2025-11-15 14:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-15 14:10:00','Cross-functional training','2025-11-15 14:00:00','2025-11-15 14:00:00'),

-- Session 3: Communication Excellence (25 confirmed bookings out of 50 seats)
(13,'BK-2025-11-0013',10,3,'CONFIRMED',NULL,NULL,'2025-11-16 09:00:00','2025-11-16 09:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 09:10:00','Mandatory training','2025-11-16 09:00:00','2025-11-16 09:00:00'),
(14,'BK-2025-11-0014',11,3,'CONFIRMED',NULL,NULL,'2025-11-16 09:30:00','2025-11-16 09:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 09:40:00','Mandatory training','2025-11-16 09:30:00','2025-11-16 09:30:00'),
(15,'BK-2025-11-0015',12,3,'CONFIRMED',NULL,NULL,'2025-11-16 10:00:00','2025-11-16 10:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 10:10:00','Mandatory training','2025-11-16 10:00:00','2025-11-16 10:00:00'),
(16,'BK-2025-11-0016',13,3,'CONFIRMED',NULL,NULL,'2025-11-16 10:30:00','2025-11-16 10:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 10:40:00','Mandatory training','2025-11-16 10:30:00','2025-11-16 10:30:00'),
(17,'BK-2025-11-0017',14,3,'CONFIRMED',NULL,NULL,'2025-11-16 11:00:00','2025-11-16 11:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 11:10:00','Mandatory training','2025-11-16 11:00:00','2025-11-16 11:00:00'),
(18,'BK-2025-11-0018',15,3,'CONFIRMED',NULL,NULL,'2025-11-16 11:30:00','2025-11-16 11:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 11:40:00','Mandatory training','2025-11-16 11:30:00','2025-11-16 11:30:00'),
(19,'BK-2025-11-0019',16,3,'CONFIRMED',NULL,NULL,'2025-11-16 13:00:00','2025-11-16 13:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 13:10:00','Mandatory training','2025-11-16 13:00:00','2025-11-16 13:00:00'),
(20,'BK-2025-11-0020',17,3,'CONFIRMED',NULL,NULL,'2025-11-16 13:30:00','2025-11-16 13:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 13:40:00','Mandatory training','2025-11-16 13:30:00','2025-11-16 13:30:00'),
(21,'BK-2025-11-0021',18,3,'CONFIRMED',NULL,NULL,'2025-11-16 14:00:00','2025-11-16 14:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-16 14:10:00','Mandatory training','2025-11-16 14:00:00','2025-11-16 14:00:00'),
-- Additional 16 bookings for session 3 (simplified)
(22,'BK-2025-11-0022',9,3,'CONFIRMED',NULL,NULL,'2025-11-17 09:00:00','2025-11-17 09:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-17 09:10:00',NULL,'2025-11-17 09:00:00','2025-11-17 09:00:00'),
(23,'BK-2025-11-0023',6,3,'CONFIRMED',NULL,NULL,'2025-11-17 09:30:00','2025-11-17 09:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-17 09:40:00',NULL,'2025-11-17 09:30:00','2025-11-17 09:30:00'),
(24,'BK-2025-11-0024',7,3,'CONFIRMED',NULL,NULL,'2025-11-17 10:00:00','2025-11-17 10:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-17 10:10:00',NULL,'2025-11-17 10:00:00','2025-11-17 10:00:00'),
(25,'BK-2025-11-0025',8,3,'CONFIRMED',NULL,NULL,'2025-11-17 10:30:00','2025-11-17 10:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-17 10:40:00',NULL,'2025-11-17 10:30:00','2025-11-17 10:30:00'),
(26,'BK-2025-11-0026',4,3,'CONFIRMED',NULL,NULL,'2025-11-17 11:00:00','2025-11-17 11:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-17 11:10:00',NULL,'2025-11-17 11:00:00','2025-11-17 11:00:00'),
(27,'BK-2025-11-0027',5,3,'CONFIRMED',NULL,NULL,'2025-11-17 11:30:00','2025-11-17 11:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-17 11:40:00',NULL,'2025-11-17 11:30:00','2025-11-17 11:30:00'),

-- Session 4: Agile & Scrum (20 confirmed out of 30 seats, 10 available)
(28,'BK-2025-11-0028',9,4,'CONFIRMED',1,NULL,'2025-11-18 09:00:00','2025-11-18 09:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 09:10:00','Team lead recommendation','2025-11-18 09:00:00','2025-11-18 09:00:00'),
(29,'BK-2025-11-0029',10,4,'CONFIRMED',2,NULL,'2025-11-18 09:30:00','2025-11-18 09:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 09:40:00',NULL,'2025-11-18 09:30:00','2025-11-18 09:30:00'),
(30,'BK-2025-11-0030',11,4,'CONFIRMED',3,NULL,'2025-11-18 10:00:00','2025-11-18 10:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 10:10:00',NULL,'2025-11-18 10:00:00','2025-11-18 10:00:00'),
(31,'BK-2025-11-0031',12,4,'CONFIRMED',4,NULL,'2025-11-18 10:30:00','2025-11-18 10:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 10:40:00',NULL,'2025-11-18 10:30:00','2025-11-18 10:30:00'),
(32,'BK-2025-11-0032',13,4,'CONFIRMED',5,NULL,'2025-11-18 11:00:00','2025-11-18 11:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 11:10:00',NULL,'2025-11-18 11:00:00','2025-11-18 11:00:00'),
(33,'BK-2025-11-0033',14,4,'CONFIRMED',6,NULL,'2025-11-18 11:30:00','2025-11-18 11:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 11:40:00',NULL,'2025-11-18 11:30:00','2025-11-18 11:30:00'),
(34,'BK-2025-11-0034',15,4,'CONFIRMED',7,NULL,'2025-11-18 13:00:00','2025-11-18 13:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 13:10:00',NULL,'2025-11-18 13:00:00','2025-11-18 13:00:00'),
(35,'BK-2025-11-0035',16,4,'CONFIRMED',8,NULL,'2025-11-18 13:30:00','2025-11-18 13:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 13:40:00',NULL,'2025-11-18 13:30:00','2025-11-18 13:30:00'),
(36,'BK-2025-11-0036',17,4,'CONFIRMED',9,NULL,'2025-11-18 14:00:00','2025-11-18 14:05:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 14:10:00',NULL,'2025-11-18 14:00:00','2025-11-18 14:00:00'),
(37,'BK-2025-11-0037',18,4,'CONFIRMED',10,NULL,'2025-11-18 14:30:00','2025-11-18 14:35:00',NULL,NULL,'NOT_MARKED',NULL,'NOT_STARTED',NULL,NULL,NULL,2,'2025-11-18 14:40:00',NULL,'2025-11-18 14:30:00','2025-11-18 14:30:00'),

-- Session 9: New Hire Onboarding (Completed session with attendance)
(38,'BK-2025-11-0100',11,9,'COMPLETED',NULL,NULL,'2025-11-18 15:00:00','2025-11-18 15:05:00',NULL,NULL,'PRESENT','2025-11-20 09:15:00','COMPLETED','2025-11-20 13:30:00',5,'Great introduction to the company','2025-11-18 15:10:00','2025-11-18 15:10:00','New hire - first day training','2025-11-18 15:00:00','2025-11-20 14:00:00'),
(39,'BK-2025-11-0101',17,9,'COMPLETED',NULL,NULL,'2025-11-19 09:00:00','2025-11-19 09:05:00',NULL,NULL,'PRESENT','2025-11-20 09:15:00','COMPLETED','2025-11-20 13:30:00',5,'Very helpful orientation','2025-11-19 09:10:00','2025-11-19 09:10:00','New hire onboarding','2025-11-19 09:00:00','2025-11-20 14:00:00'),
(40,'BK-2025-11-0102',18,9,'COMPLETED',NULL,NULL,'2025-11-19 10:00:00','2025-11-19 10:05:00',NULL,NULL,'PRESENT','2025-11-20 09:15:00','COMPLETED','2025-11-20 13:30:00',4,'Good overview of systems','2025-11-19 10:10:00','2025-11-19 10:10:00','New hire','2025-11-19 10:00:00','2025-11-20 14:00:00'),

-- Cancelled booking example
(41,'BK-2025-11-0200',14,1,'CANCELLED',NULL,NULL,'2025-11-20 10:00:00',NULL,'2025-11-25 09:00:00','Conflicting project deadline',NULL,NULL,NULL,NULL,NULL,NULL,2,'2025-11-20 10:05:00','Will reschedule for next session','2025-11-20 10:00:00','2025-11-25 09:00:00'),

-- Waitlisted booking example
(42,'BK-2025-11-0300',15,2,'WAITLISTED',NULL,1,'2025-11-28 14:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,'2025-11-28 14:05:00','On waitlist - high priority','2025-11-28 14:00:00','2025-11-28 14:00:00');

-- ============================================================================
-- END OF SAMPLE DATA INSERT SCRIPT
-- ============================================================================
-- Summary:
-- - 6 roles inserted
-- - 10 skills inserted
-- - 8 business units inserted (with hierarchy)
-- - 5 locations inserted
-- - 18 users inserted (with management hierarchy)
-- - 10 learning programs inserted
-- - Program-to-skill mappings created
-- - Program-to-role mappings created
-- - Program-to-business-unit mappings created
-- - 10 learning sessions inserted (upcoming and completed)
-- - 42 bookings inserted (various statuses)
-- ============================================================================


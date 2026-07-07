-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 07, 2026 at 09:27 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `gram`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin_info`
--

CREATE TABLE `admin_info` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin_info`
--

INSERT INTO `admin_info` (`id`, `username`, `password`, `full_name`, `created_at`) VALUES
(1, 'admin', '0192023a7bbd73250516f069df18b500', 'System Administrator', '2026-03-31 12:32:49');

-- --------------------------------------------------------

--
-- Table structure for table `birth_requests`
--

CREATE TABLE `birth_requests` (
  `id` int(11) NOT NULL,
  `request_id` varchar(20) NOT NULL,
  `household_id` varchar(20) NOT NULL,
  `child_name` varchar(100) NOT NULL,
  `gender` enum('Male','Female') NOT NULL,
  `date_of_birth` date NOT NULL,
  `place_of_birth` varchar(100) DEFAULT NULL,
  `relationship` varchar(20) DEFAULT NULL,
  `aadhaar_number` varchar(12) DEFAULT NULL,
  `father_name` varchar(100) DEFAULT NULL,
  `mother_name` varchar(100) DEFAULT NULL,
  `birth_certificate_number` varchar(50) DEFAULT NULL,
  `certificate_issue_date` date DEFAULT NULL,
  `status` enum('pending','approved','rejected') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `birth_requests`
--

INSERT INTO `birth_requests` (`id`, `request_id`, `household_id`, `child_name`, `gender`, `date_of_birth`, `place_of_birth`, `relationship`, `aadhaar_number`, `father_name`, `mother_name`, `birth_certificate_number`, `certificate_issue_date`, `status`, `created_at`) VALUES
(1, 'BR20260405170111', 'HH000007', 'Aravi Sawant', 'Female', '2024-04-05', 'kolhapur', 'Daughter', '955856555285', 'Sushant Sawant', 'Trupti Sawant', '7666556667', '2026-04-05', 'pending', '2026-04-05 11:31:51');

-- --------------------------------------------------------

--
-- Table structure for table `correction_requests`
--

CREATE TABLE `correction_requests` (
  `id` int(11) NOT NULL,
  `request_id` varchar(20) NOT NULL,
  `household_id` varchar(20) NOT NULL,
  `member_id` varchar(20) DEFAULT NULL,
  `field_name` varchar(50) NOT NULL,
  `old_value` text DEFAULT NULL,
  `new_value` text NOT NULL,
  `reason` text DEFAULT NULL,
  `document_type` varchar(50) DEFAULT NULL,
  `document_number` varchar(100) DEFAULT NULL,
  `document_path` varchar(255) DEFAULT NULL,
  `status` enum('pending','approved','rejected') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `death_requests`
--

CREATE TABLE `death_requests` (
  `id` int(11) NOT NULL,
  `request_id` varchar(20) NOT NULL,
  `household_id` varchar(20) NOT NULL,
  `member_id` varchar(20) DEFAULT NULL,
  `member_name` varchar(100) NOT NULL,
  `date_of_death` date NOT NULL,
  `place_of_death` varchar(100) DEFAULT NULL,
  `death_certificate_number` varchar(50) DEFAULT NULL,
  `death_reason` enum('Natural','Accident','Other') DEFAULT 'Natural',
  `other_reason` text DEFAULT NULL,
  `status` enum('pending','approved','rejected') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `family_members`
--

CREATE TABLE `family_members` (
  `id` int(11) NOT NULL,
  `member_id` varchar(20) NOT NULL,
  `household_id` varchar(20) NOT NULL,
  `member_name` varchar(100) NOT NULL,
  `member_aadhaar` varchar(12) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `gender` enum('Male','Female','Other') NOT NULL,
  `relationship` varchar(20) NOT NULL,
  `education` varchar(50) DEFAULT NULL,
  `occupation` varchar(50) DEFAULT NULL,
  `marital_status` enum('Married','Unmarried','Widowed','Divorced') DEFAULT NULL,
  `is_deceased` tinyint(1) DEFAULT 0,
  `death_date` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `profile_image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `family_members`
--

INSERT INTO `family_members` (`id`, `member_id`, `household_id`, `member_name`, `member_aadhaar`, `date_of_birth`, `age`, `gender`, `relationship`, `education`, `occupation`, `marital_status`, `is_deceased`, `death_date`, `created_at`, `profile_image`) VALUES
(1, 'MEM000001', 'HH000002', 'veera', NULL, NULL, 25, 'Female', 'Sister', 'bcom', '', NULL, 0, NULL, '2026-04-03 06:46:23', NULL),
(2, 'MEM000002', 'HH000003', 'ArchitaThakur', '650615595555', '0000-00-00', 25, 'Male', 'Daughter', '12th', 'no', 'Unmarried', 0, NULL, '2026-04-03 06:54:48', NULL),
(3, 'MEM000003', 'HH000006', 'Shivendra Kognule', '86855358985', '2019-04-05', 5, 'Male', 'Son', 'NA', 'NA', 'Married', 0, NULL, '2026-04-05 10:20:02', 'MEM_1775384402_5605.jpg'),
(4, 'MEM000004', 'HH000007', 'Ankur Sawant', '864885358', '2010-05-15', 16, 'Male', 'Son', '10th Pass', 'Student', 'Unmarried', 0, NULL, '2026-04-05 10:25:14', 'MEM_1775384714_4230.jpg'),
(5, 'MEM000005', 'HH000007', 'Savitri Sawant', '856369552566', NULL, 35, 'Female', 'Spouse', '10th Pass', 'Housewife', 'Married', 0, NULL, '2026-04-16 18:19:22', NULL),
(6, 'MEM000006', 'HH000007', 'Kavita Sawant', '856369552567', NULL, 12, 'Female', 'Daughter', '7th Standard', 'Student', 'Unmarried', 0, NULL, '2026-04-16 18:19:22', NULL);

--
-- Triggers `family_members`
--
DELIMITER $$
CREATE TRIGGER `before_insert_family_members` BEFORE INSERT ON `family_members` FOR EACH ROW BEGIN
    DECLARE next_id INT;
    DECLARE new_member_id VARCHAR(20);
    
    SELECT IFNULL(MAX(CAST(SUBSTRING(member_id, 4) AS UNSIGNED)), 0) + 1 INTO next_id 
    FROM family_members;
    
    SET new_member_id = CONCAT('MEM', LPAD(next_id, 6, '0'));
    SET NEW.member_id = new_member_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `household_info`
--

CREATE TABLE `household_info` (
  `id` int(11) NOT NULL,
  `household_id` varchar(20) NOT NULL,
  `head_name` varchar(100) NOT NULL,
  `head_aadhaar` varchar(12) NOT NULL,
  `head_mobile` varchar(10) NOT NULL,
  `address` text DEFAULT NULL,
  `village` varchar(50) DEFAULT NULL,
  `taluka` varchar(50) DEFAULT NULL,
  `district` varchar(50) DEFAULT NULL,
  `pincode` varchar(6) DEFAULT NULL,
  `annual_income` decimal(12,2) DEFAULT NULL,
  `caste` enum('General','OBC','SC','ST','Others') NOT NULL,
  `house_type` enum('Kutcha','Semi-Pucca','Pucca','Apartment') NOT NULL,
  `bpl_card_number` varchar(20) DEFAULT NULL,
  `electricity` tinyint(1) DEFAULT 0,
  `water_connection` tinyint(1) DEFAULT 0,
  `gas_connection` tinyint(1) DEFAULT 0,
  `registered_by` varchar(20) DEFAULT NULL,
  `registration_date` datetime DEFAULT current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `occupation` varchar(50) DEFAULT 'Not Specified',
  `bank_name` varchar(100) DEFAULT 'Not Available',
  `account_number` varchar(30) DEFAULT 'Not Available',
  `ifsc_code` varchar(20) DEFAULT 'Not Available',
  `alt_mobile` varchar(10) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `state` varchar(50) DEFAULT 'Maharashtra',
  `profile_image` varchar(255) DEFAULT NULL,
  `head_gender` enum('Male','Female','Other') DEFAULT 'Male'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `household_info`
--

INSERT INTO `household_info` (`id`, `household_id`, `head_name`, `head_aadhaar`, `head_mobile`, `address`, `village`, `taluka`, `district`, `pincode`, `annual_income`, `caste`, `house_type`, `bpl_card_number`, `electricity`, `water_connection`, `gas_connection`, `registered_by`, `registration_date`, `is_active`, `created_at`, `updated_at`, `occupation`, `bank_name`, `account_number`, `ifsc_code`, `alt_mobile`, `email`, `state`, `profile_image`, `head_gender`) VALUES
(1, 'HH000001', 'Thakur 63', '515545535825', '8788171006', 'kolhapur', '', '', '', '', 800000.00, 'General', 'Pucca', NULL, 1, 1, 1, 'STAFF001', '2026-04-03 11:53:19', 1, '2026-04-03 06:23:19', '2026-04-03 06:23:19', 'Not Specified', 'Not Available', 'Not Available', 'Not Available', NULL, NULL, 'Maharashtra', NULL, 'Male'),
(2, 'HH000002', 'Saish Thakur', '650622168121', '7385606361', 'Pune', '', '', '', '', 750000.00, 'OBC', 'Pucca', NULL, 1, 1, 1, 'STAFF001', '2026-04-03 12:16:23', 1, '2026-04-03 06:46:23', '2026-04-16 18:07:44', 'Not Specified', 'Not Available', 'Not Available', 'Not Available', NULL, NULL, 'Maharashtra', NULL, 'Female'),
(3, 'HH000003', 'Priya Thakur', '559965858588', '9558558849', 'Mumbai', '', '', '', '', 84558899.00, 'General', 'Pucca', NULL, 1, 1, 1, 'STAFF001', '2026-04-03 12:24:48', 1, '2026-04-03 06:54:48', '2026-04-03 06:54:48', 'Not Specified', 'Not Available', 'Not Available', 'Not Available', NULL, NULL, 'Maharashtra', NULL, 'Male'),
(4, 'HH000004', 'Arya Thakur', '724871578556', '8788171006', 'kolhapur', '', '', '', '', 75000.00, 'General', 'Pucca', NULL, 1, 1, 1, 'STAFF001', '2026-04-05 15:18:22', 1, '2026-04-05 09:48:22', '2026-04-05 09:48:22', 'engineer', 'STate bank of india', '', '567577', '', '', 'Maharashtra', 'HH_1775382502_4856.jpg', 'Male'),
(5, 'HH000005', 'Kishor Thakur', '835598568858', '7385606361', 'Nashik', '', '', '', '', 80688.00, 'General', 'Pucca', NULL, 1, 1, 1, 'STAFF001', '2026-04-05 15:36:00', 1, '2026-04-05 10:06:00', '2026-04-05 10:06:00', 'electric', 'Maharashtra Bank', '', '35445566556', '', '', 'Maharashtra', 'HH_1775383560_3412.jpg', 'Male'),
(6, 'HH000006', 'Omkar Kognul6', '865293514569', '8788171006', 'Mumbai', '', '', '', '', 900000.00, 'OBC', 'Pucca', NULL, 1, 1, 1, 'STAFF001', '2026-04-05 15:50:02', 1, '2026-04-05 10:20:02', '2026-04-05 10:20:02', 'Hotel Manager', 'Maharashtra Bank', '', '5456556556', '', '', 'Maharashtra', 'HH_1775384401_2407.jpg', 'Male'),
(7, 'HH000007', 'Sushant Sawant', '856369552565', '9325013900', 'Kolhapur', '', '', '', '', 25000.00, 'SC', 'Pucca', 'BPL2026001', 1, 1, 0, 'STAFF001', '2026-04-05 15:55:14', 1, '2026-04-05 10:25:14', '2026-04-16 18:18:58', 'Farmer', 'State Bank of India', 'SBIN123456789', 'SBIN0001234', '', '', 'Maharashtra', 'HH_1775384714_8163.jpg', 'Male');

--
-- Triggers `household_info`
--
DELIMITER $$
CREATE TRIGGER `before_insert_household_info` BEFORE INSERT ON `household_info` FOR EACH ROW BEGIN
    DECLARE next_id INT;
    DECLARE new_household_id VARCHAR(20);
    
    SELECT IFNULL(MAX(CAST(SUBSTRING(household_id, 3) AS UNSIGNED)), 0) + 1 INTO next_id 
    FROM household_info;
    
    SET new_household_id = CONCAT('HH', LPAD(next_id, 6, '0'));
    SET NEW.household_id = new_household_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `requests`
--

CREATE TABLE `requests` (
  `id` int(11) NOT NULL,
  `request_id` varchar(20) NOT NULL,
  `household_id` varchar(20) NOT NULL,
  `request_type` enum('birth','death','correction') NOT NULL,
  `request_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`request_data`)),
  `request_status` enum('pending','approved','rejected') DEFAULT 'pending',
  `requested_by` varchar(20) DEFAULT NULL,
  `request_date` datetime DEFAULT current_timestamp(),
  `reviewed_by` varchar(20) DEFAULT NULL,
  `review_date` datetime DEFAULT NULL,
  `admin_remarks` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `schemes`
--

CREATE TABLE `schemes` (
  `id` int(11) NOT NULL,
  `scheme_id` varchar(20) NOT NULL,
  `scheme_name` varchar(200) NOT NULL,
  `scheme_category` enum('Household-Based','Individual-Based') NOT NULL,
  `department_name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `benefit_type` enum('Financial','Subsidy','Pension','Scholarship') NOT NULL,
  `benefit_amount` decimal(12,2) DEFAULT NULL,
  `application_start_date` date DEFAULT NULL,
  `application_end_date` date DEFAULT NULL,
  `required_documents` text DEFAULT NULL,
  `min_income` decimal(12,2) DEFAULT NULL,
  `max_income` decimal(12,2) DEFAULT NULL,
  `min_age` int(11) DEFAULT NULL,
  `max_age` int(11) DEFAULT NULL,
  `gender_restriction` enum('Male','Female','Any') DEFAULT 'Any',
  `caste_restriction` varchar(50) DEFAULT 'All',
  `scheme_status` enum('Active','Inactive') DEFAULT 'Active',
  `created_by` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `portal_url` varchar(255) DEFAULT NULL,
  `application_process` text DEFAULT NULL,
  `benefits` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `schemes`
--

INSERT INTO `schemes` (`id`, `scheme_id`, `scheme_name`, `scheme_category`, `department_name`, `description`, `benefit_type`, `benefit_amount`, `application_start_date`, `application_end_date`, `required_documents`, `min_income`, `max_income`, `min_age`, `max_age`, `gender_restriction`, `caste_restriction`, `scheme_status`, `created_by`, `created_at`, `updated_at`, `portal_url`, `application_process`, `benefits`) VALUES
(1, 'SCH000001', 'PM Awas Yojana', 'Household-Based', 'Housing Department', 'Financial assistance for house construction to eligible families', 'Financial', 120000.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Income Certificate, Residence Proof', 0.00, 300000.00, NULL, NULL, 'Any', 'All', 'Active', NULL, '2026-04-16 16:44:39', '2026-04-16 16:44:39', NULL, NULL, NULL),
(2, 'SCH000002', 'SC Student Scholarship', 'Individual-Based', 'Education Department', 'Scholarship for SC category students pursuing higher education', 'Scholarship', 5000.00, '2024-06-01', '2024-08-31', 'Aadhaar Card, Caste Certificate, Income Certificate, Previous Year Marksheet', 0.00, 250000.00, 6, 25, 'Any', 'SC', 'Active', NULL, '2026-04-16 16:44:39', '2026-04-16 16:44:39', NULL, NULL, NULL),
(3, 'SCH000003', 'Old Age Pension', 'Individual-Based', 'Social Welfare Department', 'Monthly pension for senior citizens above 60 years', 'Pension', 1500.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Age Proof, Bank Passbook', 0.00, 50000.00, 60, NULL, 'Any', 'All', 'Active', NULL, '2026-04-16 16:44:39', '2026-04-16 16:44:39', NULL, NULL, NULL),
(4, 'SCH000004', 'Ration Card Scheme', 'Household-Based', 'Food Department', 'Subsidized food grains for BPL families', 'Subsidy', NULL, '2024-01-01', '2024-12-31', 'Aadhaar Card, BPL Certificate, Residence Proof', 0.00, 100000.00, NULL, NULL, 'Any', 'All', 'Active', NULL, '2026-04-16 16:44:39', '2026-04-16 16:44:39', NULL, NULL, NULL),
(5, 'SCH000005', 'Ladki Bahin Yojana', 'Individual-Based', 'Women and Child Development', 'Financial assistance for women', 'Financial', 15000.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Age Proof, Bank Passbook', 0.00, 250000.00, 21, 60, 'Female', 'All', 'Active', NULL, '2026-04-16 16:44:39', '2026-04-16 16:44:39', NULL, NULL, NULL),
(6, 'SCH000006', 'PM Awas Yojana', 'Household-Based', 'Ministry of Housing and Urban Affairs', 'Financial assistance for construction of pucca house for eligible families', 'Financial', 120000.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Income Certificate, Land Ownership Document, Bank Passbook', 0.00, 300000.00, NULL, NULL, 'Any', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:08', '2026-04-16 18:34:08', 'https://pmaymis.gov.in', '1. Visit PMAY portal\n2. Register with mobile number\n3. Fill application form\n4. Upload required documents\n5. Submit application\n6. Track application status online', '✅ Financial assistance up to ₹1,20,000\n✅ House construction support\n✅ Interest subsidy on loan'),
(7, 'SCH000007', 'Post Matric Scholarship for SC Students', 'Individual-Based', 'Social Justice Department', 'Scholarship for SC category students pursuing higher education', 'Scholarship', 5000.00, '2024-06-01', '2024-08-31', 'Aadhaar Card, Caste Certificate, Income Certificate, Previous Year Marksheet, Bank Passbook', 0.00, 250000.00, 16, 25, 'Any', 'SC', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://scholarships.gov.in', '1. Visit National Scholarship Portal\n2. Register as new user\n3. Fill application form\n4. Upload required documents\n5. Submit application\n6. Track status online', '✅ Monthly scholarship up to ₹5,000\n✅ Book allowance\n✅ Exam fee reimbursement\n✅ Maintenance allowance'),
(8, 'SCH000008', 'Indira Gandhi National Old Age Pension Scheme', 'Individual-Based', 'Social Welfare Department', 'Monthly pension for senior citizens above 60 years', 'Pension', 1500.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Age Proof, Bank Passbook, Income Certificate', 0.00, 50000.00, 60, NULL, 'Any', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://nsap.nic.in', '1. Visit nearest Common Service Centre\n2. Fill application form\n3. Submit required documents\n4. Verification by Gram Panchayat\n5. Approval by Social Welfare Department\n6. Pension credited to bank account monthly', '✅ Monthly pension of ₹1,500\n✅ Direct bank transfer\n✅ Life certificate submission annually'),
(9, 'SCH000009', 'National Food Security Scheme (Ration Card)', 'Household-Based', 'Food and Civil Supplies Department', 'Subsidized food grains for eligible families', 'Subsidy', NULL, '2024-01-01', '2024-12-31', 'Aadhaar Card, Residence Proof, Income Certificate, Family Member Details', 0.00, 100000.00, NULL, NULL, 'Any', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://nfsa.gov.in', '1. Visit local Tehsil office\n2. Submit application form\n3. Provide family member details\n4. Verification by Food Inspector\n5. Ration card issued\n6. Collect from nearest ration shop', '✅ Subsidized food grains (Rice, Wheat, Sugar)\n✅ 5kg per person per month\n✅ Special benefits for BPL families'),
(10, 'SCH000010', 'Ladki Bahin Yojana', 'Individual-Based', 'Women and Child Development Department', 'Financial assistance for women from economically weaker sections', 'Financial', 15000.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Age Proof, Income Certificate, Bank Passbook, Marriage Certificate (if applicable)', 0.00, 250000.00, 21, 60, 'Female', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://wcd.nic.in', '1. Visit Anganwadi center\n2. Fill application form\n3. Submit required documents\n4. Verification by Women Welfare Officer\n5. Approval by District Collector\n6. Amount credited to bank account', '✅ Financial assistance of ₹15,000 per year\n✅ Skill development training\n✅ Health insurance coverage'),
(11, 'SCH000011', 'PM Kisan Samman Nidhi', 'Household-Based', 'Agriculture Department', 'Income support for small and marginal farmers', 'Financial', 6000.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Land Records, Bank Passbook', 0.00, 200000.00, NULL, NULL, 'Any', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://pmkisan.gov.in', '1. Visit nearest CSC center\n2. Register with Aadhaar\n3. Provide land record details\n4. Verification by Agriculture Officer\n5. Approval by State Government\n6. Amount credited in 3 installments', '✅ ₹6,000 per year in 3 installments\n✅ Direct bank transfer\n✅ Farmer welfare support'),
(12, 'SCH000012', 'Ayushman Bharat - PMJAY', 'Household-Based', 'Health and Family Welfare Department', 'Health insurance coverage for vulnerable families', 'Subsidy', 500000.00, '2024-01-01', '2024-12-31', 'Aadhaar Card, Ration Card, Income Certificate', 0.00, 200000.00, NULL, NULL, 'Any', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://pmjay.gov.in', '1. Check eligibility on portal\n2. Visit empaneled hospital\n3. Provide Aadhaar and ration card\n4. Get e-card generated\n5. Avail cashless treatment', '✅ Health cover up to ₹5 lakh per family\n✅ Cashless treatment\n✅ 1500+ medical packages\n✅ No age limit'),
(13, 'SCH000013', 'Beti Bachao Beti Padhao Scheme', 'Individual-Based', 'Education Department', 'Financial support for girl child education', 'Scholarship', 10000.00, '2024-04-01', '2024-07-31', 'Aadhaar Card, Birth Certificate, School Admission Proof, Bank Passbook', 0.00, 300000.00, 5, 18, 'Female', 'All', 'Active', 'ADMIN001', '2026-04-16 18:34:09', '2026-04-16 18:34:09', 'https://wcd.nic.in/bbbp', '1. Apply through school\n2. Submit birth certificate\n3. Provide income certificate\n4. School verification\n5. Scholarship disbursed to bank account', '✅ Annual scholarship of ₹10,000\n✅ Free education materials\n✅ Career counseling\n✅ Skill development');

--
-- Triggers `schemes`
--
DELIMITER $$
CREATE TRIGGER `before_insert_schemes` BEFORE INSERT ON `schemes` FOR EACH ROW BEGIN
    DECLARE next_id INT;
    DECLARE new_scheme_id VARCHAR(20);
    
    SELECT IFNULL(MAX(CAST(SUBSTRING(scheme_id, 4) AS UNSIGNED)), 0) + 1 INTO next_id 
    FROM schemes;
    
    SET new_scheme_id = CONCAT('SCH', LPAD(next_id, 6, '0'));
    SET NEW.scheme_id = new_scheme_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `user_id` varchar(20) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `full_name` varchar(100) NOT NULL,
  `mobile` varchar(10) NOT NULL,
  `household_id` varchar(20) DEFAULT NULL,
  `account_status` enum('pending','active','blocked') DEFAULT 'pending',
  `otp` varchar(10) DEFAULT NULL,
  `otp_expiry` datetime DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `otp_verified` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `user_id`, `username`, `password`, `full_name`, `mobile`, `household_id`, `account_status`, `otp`, `otp_expiry`, `last_login`, `created_at`, `updated_at`, `otp_verified`) VALUES
(1, 'USR000001', '8788171006', '29510a0a315862ff4ff92f99a2f27255', 'Thakur 63', '8788171006', 'HH000001', 'active', NULL, NULL, '2026-04-22 16:16:26', '2026-04-03 06:23:19', '2026-04-22 10:46:26', 1),
(2, 'USR000002', '7385606361', NULL, 'Saish Thakur', '7385606361', 'HH000002', 'pending', NULL, NULL, '2026-04-04 11:19:27', '2026-04-03 06:46:23', '2026-04-22 09:49:17', 0),
(3, 'USR000003', '9558558849', NULL, 'Priya Thakur', '9558558849', 'HH000003', 'active', NULL, NULL, NULL, '2026-04-03 06:54:48', '2026-04-18 18:59:28', 0),
(4, 'USR000004', '9325013900', 'b683e3da60859e9a291409fc3980f395', 'Sushant Sawant', '9325013900', 'HH000007', 'active', NULL, NULL, '2026-04-22 15:33:39', '2026-04-05 10:25:14', '2026-04-22 10:03:39', 1);

--
-- Triggers `users`
--
DELIMITER $$
CREATE TRIGGER `before_insert_users` BEFORE INSERT ON `users` FOR EACH ROW BEGIN
    DECLARE next_id INT;
    DECLARE new_user_id VARCHAR(20);
    
    SELECT IFNULL(MAX(CAST(SUBSTRING(user_id, 4) AS UNSIGNED)), 0) + 1 INTO next_id 
    FROM users;
    
    SET new_user_id = CONCAT('USR', LPAD(next_id, 6, '0'));
    SET NEW.user_id = new_user_id;
END
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin_info`
--
ALTER TABLE `admin_info`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `birth_requests`
--
ALTER TABLE `birth_requests`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `request_id` (`request_id`),
  ADD KEY `household_id` (`household_id`);

--
-- Indexes for table `correction_requests`
--
ALTER TABLE `correction_requests`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `request_id` (`request_id`),
  ADD KEY `household_id` (`household_id`),
  ADD KEY `member_id` (`member_id`);

--
-- Indexes for table `death_requests`
--
ALTER TABLE `death_requests`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `request_id` (`request_id`),
  ADD KEY `household_id` (`household_id`),
  ADD KEY `member_id` (`member_id`);

--
-- Indexes for table `family_members`
--
ALTER TABLE `family_members`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `member_id` (`member_id`),
  ADD UNIQUE KEY `member_aadhaar` (`member_aadhaar`),
  ADD KEY `idx_household` (`household_id`);

--
-- Indexes for table `household_info`
--
ALTER TABLE `household_info`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `household_id` (`household_id`),
  ADD UNIQUE KEY `head_aadhaar` (`head_aadhaar`),
  ADD KEY `idx_mobile` (`head_mobile`),
  ADD KEY `idx_aadhaar` (`head_aadhaar`);

--
-- Indexes for table `requests`
--
ALTER TABLE `requests`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `request_id` (`request_id`),
  ADD KEY `idx_household` (`household_id`),
  ADD KEY `idx_status` (`request_status`);

--
-- Indexes for table `schemes`
--
ALTER TABLE `schemes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `scheme_id` (`scheme_id`),
  ADD KEY `idx_category` (`scheme_category`),
  ADD KEY `idx_status` (`scheme_status`),
  ADD KEY `idx_benefit_type` (`benefit_type`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `mobile` (`mobile`),
  ADD KEY `household_id` (`household_id`),
  ADD KEY `idx_mobile` (`mobile`),
  ADD KEY `idx_status` (`account_status`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin_info`
--
ALTER TABLE `admin_info`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `birth_requests`
--
ALTER TABLE `birth_requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `correction_requests`
--
ALTER TABLE `correction_requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `death_requests`
--
ALTER TABLE `death_requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `family_members`
--
ALTER TABLE `family_members`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `household_info`
--
ALTER TABLE `household_info`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `requests`
--
ALTER TABLE `requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `schemes`
--
ALTER TABLE `schemes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `birth_requests`
--
ALTER TABLE `birth_requests`
  ADD CONSTRAINT `birth_requests_ibfk_1` FOREIGN KEY (`household_id`) REFERENCES `household_info` (`household_id`);

--
-- Constraints for table `correction_requests`
--
ALTER TABLE `correction_requests`
  ADD CONSTRAINT `correction_requests_ibfk_1` FOREIGN KEY (`household_id`) REFERENCES `household_info` (`household_id`),
  ADD CONSTRAINT `correction_requests_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `family_members` (`member_id`);

--
-- Constraints for table `death_requests`
--
ALTER TABLE `death_requests`
  ADD CONSTRAINT `death_requests_ibfk_1` FOREIGN KEY (`household_id`) REFERENCES `household_info` (`household_id`),
  ADD CONSTRAINT `death_requests_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `family_members` (`member_id`);

--
-- Constraints for table `family_members`
--
ALTER TABLE `family_members`
  ADD CONSTRAINT `family_members_ibfk_1` FOREIGN KEY (`household_id`) REFERENCES `household_info` (`household_id`) ON DELETE CASCADE;

--
-- Constraints for table `requests`
--
ALTER TABLE `requests`
  ADD CONSTRAINT `requests_ibfk_1` FOREIGN KEY (`household_id`) REFERENCES `household_info` (`household_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`household_id`) REFERENCES `household_info` (`household_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

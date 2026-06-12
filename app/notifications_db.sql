-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 28, 2026 at 06:16 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `notifications_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `promotions`
--

CREATE TABLE `promotions` (
  `id` int(11) NOT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `discount` varchar(50) DEFAULT NULL,
  `valid_until` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotions`
--

INSERT INTO `promotions` (`id`, `shop_id`, `title`, `description`, `discount`, `valid_until`) VALUES
(1, 1, '20% Off', 'Valid weekdays', '20%', '2026-12-31'),
(2, 2, 'Buy 1 Free 1', 'Every Friday', 'BOGO', '2026-12-31');

-- --------------------------------------------------------

--
-- Table structure for table `shops`
--

CREATE TABLE `shops` (
  `id` int(11) NOT NULL,
  `name` varchar(150) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shops`
--

INSERT INTO `shops` (`id`, `name`, `address`, `latitude`, `longitude`) VALUES
(1, 'Pizza Palace', '123 Main St', 4.0311, 101.0511),
(2, 'Burger Barn', 'Jitra', 4.035, 101.048);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `fcm_token` varchar(255) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `fcm_token`, `location`, `created_at`) VALUES
(1, 'Alice', 'alice@example.com', 'abc123token', 'cafe', '2026-05-25 02:08:51'),
(2, 'Bob', 'bob@example.com', 'xyz456token', 'park', '2026-05-25 02:08:51'),
(3, 'Charlie', 'charlie@example.com', 'def789token', 'mall', '2026-05-25 02:08:51'),
(5, 'test', 'test@gmail.com', '123', 'cafe', '2026-05-26 09:26:13'),
(6, 'abu', 'abu@gmail.com', '', NULL, '2026-05-26 09:26:55'),
(7, 'ali', 'ali@gmail.com', '', NULL, '2026-05-27 02:49:06'),
(8, 'sara', 'sara@gmail.com', '', NULL, '2026-05-27 03:01:13');

-- --------------------------------------------------------

--
-- Table structure for table `user_locations`
--

CREATE TABLE `user_locations` (
  `id` int(11) NOT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `recorded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_locations`
--

INSERT INTO `user_locations` (`id`, `user_id`, `latitude`, `longitude`, `recorded_at`) VALUES
(1, 'ali', 4.6606661, 101.1476923, '2026-05-27 02:57:31'),
(2, 'ali', 4.6606649, 101.1476916, '2026-05-27 02:57:39'),
(3, 'ali', 4.6605632, 101.1477759, '2026-05-27 02:57:48'),
(4, 'ali', 4.6605651, 101.1477405, '2026-05-27 02:57:54'),
(5, 'ali', 4.6605882, 101.1477206, '2026-05-27 02:58:03'),
(6, 'ali', 4.6605928, 101.1477179, '2026-05-27 02:58:11'),
(7, 'ali', 4.6605851, 101.1477223, '2026-05-27 02:58:18'),
(8, 'ali', 4.6605774, 101.1477203, '2026-05-27 02:58:27'),
(9, 'sara', 4.032, 101.052, '2026-05-27 03:01:38');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `shops`
--
ALTER TABLE `shops`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `user_locations`
--
ALTER TABLE `user_locations`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `promotions`
--
ALTER TABLE `promotions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `shops`
--
ALTER TABLE `shops`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `user_locations`
--
ALTER TABLE `user_locations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

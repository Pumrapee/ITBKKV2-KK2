CREATE DATABASE IF NOT EXISTS kanbandb;
USE kanbandb;

CREATE TABLE `boards` (
  `boardId` varchar(10) NOT NULL,
  `boardName` varchar(120) NOT NULL,
  `owneroid` varchar(36) NOT NULL,
  `visibility` ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE',
  `taskLimitEnabled` boolean NOT NULL,
  `maxTasksPerStatus` int NOT NULL,
  PRIMARY KEY (`boardId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `statuses` (
  `statusId` int NOT NULL AUTO_INCREMENT,
  `boardId` varchar(10) NOT NULL,
  `statusName` varchar(50) NOT NULL,
  `statusDescription` varchar(200) DEFAULT NULL,
  `statusColor` varchar(7) NOT NULL,
  PRIMARY KEY (`statusId`),
  UNIQUE KEY `status_board_unique` (`statusName`, `boardId`),
  KEY `boardId` (`boardId`),
  CONSTRAINT `statuses_ibfk_1` FOREIGN KEY (`boardId`) REFERENCES `boards` (`boardId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tasks` (
  `taskId` int NOT NULL AUTO_INCREMENT,
  `boardId` varchar(10) NOT NULL,
  `taskTitle` varchar(100) NOT NULL,
  `taskDescription` varchar(500) DEFAULT NULL,
  `taskAssignees` varchar(30) DEFAULT NULL,
  `taskStatus` int NOT NULL,
  `createdOn` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`taskId`),
  KEY `taskStatus` (`taskStatus`),
  KEY `boardId` (`boardId`),
  CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`taskStatus`) REFERENCES `statuses` (`statusId`),
  CONSTRAINT `tasks_ibfk_2` FOREIGN KEY (`boardId`) REFERENCES `boards` (`boardId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

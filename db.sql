
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `firstName` varchar(255) NOT NULL,
  `lastName` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL UNIQUE,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `admin` (
  `idA` int NOT NULL,
  PRIMARY KEY (`idA`),
  CONSTRAINT `fk_admin_user` FOREIGN KEY (`idA`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `teacher` (
  `idT` int NOT NULL,
  `domain` varchar(45) NOT NULL,
  `grade` varchar(45) NOT NULL,
  PRIMARY KEY (`idT`),
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`idT`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `student` (
  `studentCardNumber` int NOT NULL,
  `academicYear` varchar(45) NOT NULL,
  `idS` int NOT NULL,
  PRIMARY KEY (`idS`),
  CONSTRAINT `fk_student_user` FOREIGN KEY (`idS`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `course` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(25) NOT NULL,
  `targetAudience` varchar(45) NOT NULL,
  `enrolmentKey` varchar(45) NOT NULL,
  `description` TEXT NOT NULL,
  `teacher_id` int NOT NULL, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`idT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `module` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(45) NOT NULL,
  `description` TEXT NOT NULL,
  `course_id` int NOT NULL,  -- Relation: course to module (many)
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_module_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chapter` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(45) NOT NULL,
  `content` TEXT NOT NULL,
  `module_id` int NOT NULL, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_chapter_module` FOREIGN KEY (`module_id`) REFERENCES `module` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `forum` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(45) NOT NULL,
  `createdAt` datetime NOT NULL,
  `chapter_id` int NOT NULL UNIQUE, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_forum_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` TEXT NOT NULL,
  `createdAt` datetime NOT NULL,
  `likes` int DEFAULT NULL,
  `dislikes` int DEFAULT NULL,
  `isModified` boolean DEFAULT NULL,
  `forum_id` int NOT NULL, 
  `user_id` int NOT NULL,   
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_comment_forum` FOREIGN KEY (`forum_id`) REFERENCES `forum` (`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `material` (
  `id` int NOT NULL AUTO_INCREMENT,
  `path` varchar(255) NOT NULL,
  `type` varchar(45) NOT NULL,
  `chapter_id` int NOT NULL, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_material_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pdf` (
  `idP` int NOT NULL,
  `nbrOfPage` int NOT NULL,
  KEY `idP_idx` (`idP`),
  CONSTRAINT `fk_pdf_material` FOREIGN KEY (`idP`) REFERENCES `material` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `video` (
  `idV` int NOT NULL,
  `duration` int NOT NULL,
  KEY `idV_idx` (`idV`),
  CONSTRAINT `fk_video_material` FOREIGN KEY (`idV`) REFERENCES `material` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `quiz` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(45) NOT NULL,
  `availableFrom` datetime NOT NULL,
  `availableTo` datetime NOT NULL,
  `chapter_id` int NOT NULL, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_quiz_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `text` TEXT NOT NULL,
  `score` int DEFAULT NULL,
  `quiz_id` int NOT NULL, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_question_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `answer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `text` TEXT NOT NULL,
  `isCorrect` boolean DEFAULT NULL,
  `question_id` int NOT NULL, 
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
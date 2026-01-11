CREATE TABLE `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    profile_picture_path VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE
);

CREATE TABLE `admin`(
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE `teacher` (
    id INT PRIMARY KEY,
    domain VARCHAR(255) NOT NULL,
    grade VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE `student` (
    id INT PRIMARY KEY,
    student_card_number VARCHAR(255) NOT NULL,
    academic_year VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE `course` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    target_audience VARCHAR(255) NOT NULL,
    enrollment_key VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    teacher_id INT NOT NULL,
    thumbnail_path VARCHAR(255) NOT NULL,

    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE
);

CREATE TABLE `module` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    course_id INT NOT NULL,
    order_index INT DEFAULT 0,
    thumbnail_path VARCHAR(255),
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE `chapter` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    module_id INT NOT NULL,
    order_index INT DEFAULT 0,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE
);

CREATE TABLE `forum` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    chapter_id INT NOT NULL UNIQUE,

    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
);

CREATE TABLE `comment` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    forum_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    dislikes INT DEFAULT 0,
    is_modified BOOLEAN DEFAULT FALSE,

    is_reply BOOLEAN DEFAULT FALSE,
    parent_comment_id INT,

    FOREIGN KEY (parent_comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    FOREIGN KEY (forum_id) REFERENCES forum(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE `material` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    chapter_id INT NOT NULL,

    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
);

-- CREATE TABLE `pdf` (
--     id INT PRIMARY KEY,
--     number_of_pages INT NOT NULL,

--     FOREIGN KEY (id) REFERENCES material(id) ON DELETE CASCADE
-- );

-- CREATE TABLE `video` (
--     id INT PRIMARY KEY,
--     duration INT NOT NULL,

--     FOREIGN KEY (id) REFERENCES material(id) ON DELETE CASCADE
-- );

CREATE TABLE `quiz` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chapter_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    file_path VARCHAR(255),
    max_grade INT NOT NULL,
    available_from DATETIME NOT NULL,
    available_to DATETIME NOT NULL,
    CONSTRAINT chk_available_dates CHECK (available_to > available_from),
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
);

CREATE TABLE `question` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    text TEXT NOT NULL,
    material VARCHAR(255),
    score INT DEFAULT 0,
    quiz_id INT NOT NULL,

    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

CREATE TABLE `answer` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    question_id INT NOT NULL,

    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

CREATE TABLE `enrollment` (
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE `submission`(
    student_id INT NOT NULL,
    quiz_id INT NOT NULL,
    submission_path VARCHAR(255),
    submission_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    grade DECIMAL(5,2),
    feedback TEXT,
    PRIMARY KEY (student_id, quiz_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

-- Not related to conception
CREATE TABLE `verification_code` (
    email VARCHAR(255) PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL
);
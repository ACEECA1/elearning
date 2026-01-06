--@block
SELECT 
    c.title AS course_title,
    m.title AS module_title,=
    m.order_index AS module_order,
    ch.title AS chapter_title,
    ch.order_index AS chapter_order,
    ch.content
FROM course c
JOIN module m ON c.id = m.course_id
JOIN chapter ch ON m.id = ch.module_id
WHERE c.id = 2  -- Replace 50 with your actual Course ID
ORDER BY m.order_index ASC, ch.order_index ASC;

--@block 
SELECT * FROM material;


--@block
SELECT * FROM material;

--@block
SELECT * FROM chapter;

--@block
SELECT * FROM forum;

--@block
SELECT * FROM comment;

--@block 
SELECT * FROM quiz;

--@block
SELECT * FROM question;

--@block
SELECT * FROM answer;

--@block
SELECT * FROM student;

--@block
SELECT * FROM course;


--@block
ALTER TABLE `enrollment`
DROP COLUMN `enrollment_date`;
ALTER TABLE `enrollment`
ADD COLUMN `enrollment_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

--@block
SELECT * FROM enrollment;
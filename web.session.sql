--@block Drop all tables
-- DROP TABLE IF EXISTS answer;
-- DROP TABLE IF EXISTS question;
-- DROP TABLE IF EXISTS quiz;
-- DROP TABLE IF EXISTS video;
-- DROP TABLE IF EXISTS pdf;
-- DROP TABLE IF EXISTS material;
-- DROP TABLE IF EXISTS comment;
-- DROP TABLE IF EXISTS forum;
-- DROP TABLE IF EXISTS chapter;
-- DROP TABLE IF EXISTS module;
-- DROP TABLE IF EXISTS enrollment;
-- DROP TABLE IF EXISTS course;
-- DROP TABLE IF EXISTS admin;
-- DROP TABLE IF EXISTS teacher;
-- DROP TABLE IF EXISTS student;
-- DROP TABLE IF EXISTS user;
--@block
SELECT * FROM user;

--@block
DELETE FROM user WHERE username = "walid123";



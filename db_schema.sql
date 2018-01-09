BEGIN TRANSACTION;
CREATE TABLE "users" (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	INTEGER UNIQUE
);
CREATE TABLE `tags` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	TEXT UNIQUE
);
CREATE TABLE `comments` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `article_id` INTEGER NOT NULL,
  `author` TEXT,
  `comment` TEXT,
  `date` INTEGER,
  `client_ip` TEXT
);
CREATE TABLE "articles" (
	`id` INTEGER PRIMARY KEY AUTOINCREMENT, 
	`title` TEXT, `article_url` NUMERIC UNIQUE, 
	`thumb_image` TEXT, 
	`date` INTEGER, 
	`user_id` INTEGER, 
	`summary` TEXT, 
	`content` TEXT, 
	`published` INTEGER NOT NULL DEFAULT 1,
	`short` INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE `article_tags` (
	`article_id`	INTEGER,
	`tag_id`	INTEGER,
	PRIMARY KEY(article_id,tag_id)
);
COMMIT;

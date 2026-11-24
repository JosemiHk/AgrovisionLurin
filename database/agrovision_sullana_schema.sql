-- AgroVision Sullana SQLite schema generated from compiled Room version 2.

-- Reference for academic review. This is not the migration for an existing phone.

-- Users must be provisioned in the application; no default credentials are seeded.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `username` TEXT NOT NULL, `displayName` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `passwordSalt` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`));

CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`);

CREATE TABLE IF NOT EXISTS `plots` (`id` TEXT NOT NULL, `code` TEXT NOT NULL, `name` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`));

CREATE UNIQUE INDEX IF NOT EXISTS `index_plots_code` ON `plots` (`code`);

CREATE TABLE IF NOT EXISTS `inspections` (`id` TEXT NOT NULL, `plotId` TEXT NOT NULL, `subPlot` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `diagnosisClass` INTEGER NOT NULL, `confidence` REAL NOT NULL, `latencyMs` INTEGER NOT NULL, `severityLevel` TEXT NOT NULL, `recommendationsJson` TEXT NOT NULL, `syncStatus` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `userId` TEXT NOT NULL, `state` TEXT NOT NULL, `classCode` TEXT NOT NULL, `classLabel` TEXT NOT NULL, `modelVersion` TEXT NOT NULL, `errorMessage` TEXT NOT NULL, `plotCodeSnapshot` TEXT NOT NULL, `userNameSnapshot` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`plotId`) REFERENCES `plots`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT );

CREATE INDEX IF NOT EXISTS `index_inspections_syncStatus` ON `inspections` (`syncStatus`);

CREATE INDEX IF NOT EXISTS `index_inspections_createdAt` ON `inspections` (`createdAt`);

CREATE INDEX IF NOT EXISTS `index_inspections_userId` ON `inspections` (`userId`);

CREATE INDEX IF NOT EXISTS `index_inspections_plotId` ON `inspections` (`plotId`);

COMMIT;

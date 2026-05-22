DROP DATABASE IF EXISTS MapDB;
CREATE DATABASE IF NOT EXISTS MapDB;

USE MapDB;

DROP USER IF EXISTS 'MapUser'@'localhost';
CREATE USER 'MapUser'@'localhost' IDENTIFIED BY 'map';
GRANT SELECT, INSERT ON MapDB.* TO 'MapUser'@'localhost';
CREATE TABLE MapDB.provaC(
    X varchar(10),
    Y float(5,2),
    C float(5,2)
);
INSERT INTO MapDB.provaC VALUES ('A', 2, 1);
insert into MapDB.provaC values('A',2,1); 
insert into MapDB.provaC values('A',1,1); 
insert into MapDB.provaC values('A',2,1); 
insert into MapDB.provaC values('A',5,1.5); 
insert into MapDB.provaC values('A',5,1.5); 
insert into MapDB.provaC values('A',6,1.5);
insert into MapDB.provaC values('B',6,10); 
insert into MapDB.provaC values('A',6,1.5); 
insert into MapDB.provaC values('A',6,1.5); 
insert into MapDB.provaC values('B',10,10); 
insert into MapDB.provaC values('B',5,10); 
insert into MapDB.provaC values('B',12,10); 
insert into MapDB.provaC values('B',14,10); 
insert into MapDB.provaC values('A',1,1);


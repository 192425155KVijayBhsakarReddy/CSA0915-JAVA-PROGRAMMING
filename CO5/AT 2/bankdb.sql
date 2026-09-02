CREATE DATABASE bankdb;

USE bankdb;

CREATE TABLE accounts (
    account_no INT PRIMARY KEY,
    account_name VARCHAR(100),
    balance DOUBLE
);

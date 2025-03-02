--liquibase formatted sql

--changeset user:description
    create table test1 (
    idd int primary key,
    name varchar(255)
);
--rollback drop table test1;
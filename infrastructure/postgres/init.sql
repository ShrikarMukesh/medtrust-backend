-- Single medtrust database with schema-per-service isolation
\c medtrust

CREATE SCHEMA IF NOT EXISTS auth         AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS appointment  AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS clinical     AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS patient      AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS consent      AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS audit        AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS notification AUTHORIZATION medtrust;
CREATE SCHEMA IF NOT EXISTS integration  AUTHORIZATION medtrust;

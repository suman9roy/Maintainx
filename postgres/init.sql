-- ============================
-- MaintainX Databases
-- ============================

CREATE DATABASE maintainx_auth;
CREATE DATABASE maintainx_resident;
CREATE DATABASE maintainx_payment;
CREATE DATABASE maintainx_complaint;
CREATE DATABASE maintainx_notice;
CREATE DATABASE maintainx_notification;
CREATE DATABASE maintainx_maintenance;
CREATE DATABASE maintainx_expense;
CREATE DATABASE maintainx_ai;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Optional extensions
\connect maintainx_auth;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect maintainx_resident;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect maintainx_payment;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect maintainx_complaint;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect maintainx_notice;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect maintainx_notification;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\connect maintainx_maintenance;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect maintainx_expense;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect maintainx_expense;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect maintainx_ai;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
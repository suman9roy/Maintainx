#!/bin/bash
set -e

# Create role if missing (ignore error if exists)
psql -U postgres -c "CREATE ROLE maintainx LOGIN PASSWORD 'maintainx'" || true
# Grant privileges on database
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE maintainx_ai TO maintainx" || true
# Run the migration SQL copied earlier
psql -U postgres -d maintainx_ai -f /tmp/V3__create_ai_document_chunks.sql

-- single-tenant database architecture into a multi-tenant architecture
-- using a shared database, shared schema approach
ALTER TABLE users ADD COLUMN tenant_id VARCHAR(36) NOT NULL DEFAULT 'default-tenant';
ALTER TABLE users ADD CONSTRAINT uk_users_email_tenant UNIQUE (email,tenant_id);
ALTER TABLE task ADD COLUMN tenant_id VARCHAR(36) NOT NULL DEFAULT 'default-tenant';
CREATE INDEX idx_task_tenant_id ON task(tenant_id);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
ALTER TABLE users DROP CONSTRAINT users_email_key;

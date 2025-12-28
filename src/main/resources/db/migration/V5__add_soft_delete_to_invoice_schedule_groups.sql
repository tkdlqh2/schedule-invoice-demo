-- Add soft delete support to invoice_schedule_groups table
ALTER TABLE invoice_schedule_groups
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Add index on deleted_at for better query performance
CREATE INDEX idx_invoice_schedule_groups_deleted_at ON invoice_schedule_groups(deleted_at);
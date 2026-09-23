-- Existing local demo volumes only: run before starting the updated publisher/consumer.
ALTER TABLE cloud_house_view_history ADD COLUMN event_id VARCHAR(36) NULL;
UPDATE cloud_house_view_history SET event_id = UUID() WHERE event_id IS NULL;
ALTER TABLE cloud_house_view_history MODIFY event_id VARCHAR(36) NOT NULL;
CREATE UNIQUE INDEX ux_history_event ON cloud_house_view_history(event_id);

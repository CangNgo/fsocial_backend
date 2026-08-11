-- media_type trùng thông tin với resource_type (Cloudinary trả sẵn "image"/"video"); bỏ cột thừa.
UPDATE attachments SET resource_type = lower(media_type) WHERE resource_type IS NULL AND media_type IS NOT NULL;

ALTER TABLE attachments DROP COLUMN media_type;

-- One-time MySQL 8 migration for review scent uniqueness.
-- Run the duplicate check first. If it returns rows, decide which duplicates to keep
-- before creating the unique index.

SELECT review_id, scent_name, COUNT(*) AS duplicate_count
FROM review_scents
GROUP BY review_id, scent_name
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX uk_review_scents_review_scent
    ON review_scents (review_id, scent_name);

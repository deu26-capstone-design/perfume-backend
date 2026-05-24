-- One-time MySQL 8 migration for mypage pagination stability and performance.
-- Run before deploying the mypage wishlist/review pagination API changes to an existing database.

-- Wishlist ordering now uses created_at DESC, perfume_id DESC.
-- Existing rows do not have historical insert timestamps, so backfill them to one stable value and
-- rely on perfume_id as the deterministic tie-breaker for legacy rows.
ALTER TABLE wishlist
    ADD COLUMN created_at DATETIME(6) NULL;

UPDATE wishlist
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP(6));

ALTER TABLE wishlist
    MODIFY created_at DATETIME(6) NOT NULL;

CREATE INDEX idx_wishlist_user_created_perfume
    ON wishlist (user_id, created_at DESC, perfume_id DESC);

-- Review pagination reads newest reviews by current user and by perfume detail page.
CREATE INDEX idx_reviews_user_created_id
    ON reviews (user_id, created_at DESC, id DESC);

CREATE INDEX idx_reviews_perfume_created_id
    ON reviews (perfume_id, created_at DESC, id DESC);

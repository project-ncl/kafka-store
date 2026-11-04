--
-- SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCL-6817] - Create REST APIs in Kafka-store for pnc-build-insights service
BEGIN transaction;
    ALTER TABLE buildstagerecord ADD COLUMN lastupdatetime timestamptz;
    UPDATE buildstagerecord set lastupdatetime = timestamp;
    CREATE INDEX idx_lastupdate_time ON buildstagerecord (lastupdatetime);
COMMIT;

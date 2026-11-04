--
-- SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCL-6870] - Prevent future duplicates in Kafka-store

BEGIN TRANSACTION;
    ALTER TABLE buildstagerecord ADD CONSTRAINT uq_buildId_stage_dur_tstamp UNIQUE (buildid, buildstage, duration, timestamp);
COMMIT;

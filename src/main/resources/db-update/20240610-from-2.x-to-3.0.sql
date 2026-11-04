--
-- SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN transaction;
    create sequence buildstagerecord_seq;
    select id from buildstagerecord order by id desc limit 1;
    -- somehow i can't run a subquery in the command below. so the value has to be substituted manually
    alter sequence buildstagerecord_seq restart with ${value from previous query};
COMMIT;

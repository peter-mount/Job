-- ============================================================
-- A custom type to hold a date range.
--
-- This type consists of two TIME's, a start and end.
-- In normal use it's used to restrict entries so they are
-- included only if the current time is between those two times.
--
-- Special case: start is after the end time. In this instance
-- The time range crosses midnight.
--
-- ============================================================

DROP TYPE TIMERANGE;

CREATE TYPE TIMERANGE AS (s TIME, e TIME );

CREATE OR REPLACE FUNCTION timerange_contains(TIME, TIMERANGE)
RETURNS boolean AS
$$
    SELECT CASE WHEN ($2) IS NULL THEN true
                WHEN ($2).s <= ($2).e THEN ($1) BETWEEN ($2).s AND ($2).e
                ELSE ( ($1) BETWEEN ($2).s AND '23:59:59'::TIME OR ($1) BETWEEN '00:00'::TIME AND ($2).e)
                END;
$$
LANGUAGE 'sql' IMMUTABLE;

-- time === timerange will return true if time is within the specified time range

CREATE OPERATOR === ( LEFTARG = TIME WITHOUT TIME ZONE, RIGHTARG = TIMERANGE, PROCEDURE = timerange_contains );

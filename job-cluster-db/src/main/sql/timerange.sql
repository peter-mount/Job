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

DROP FUNCTION timerange(TIME,TIME);
DROP OPERATOR === (TIME WITHOUT TIME ZONE,TIMERANGE);
DROP FUNCTION timerange_contains(TIME,TIMERANGE);
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

-- Will return an appropriate timerange for two times.
-- If both are null then returns null.
-- If first is null then 00:00 is used. If second null then 23:59:59 is used.
CREATE OR REPLACE FUNCTION timerange(TIME,TIME)
RETURNS TIMERANGE AS $$
    SELECT CASE WHEN ($1) IS NULL AND ($2) IS NULL THEN NULL
                WHEN ($1) IS NULL THEN ('00:00'::TIME,($2))::TIMERANGE
                WHEN ($2) IS NULL THEN (($1),'23:59:59'::TIME)::TIMERANGE
                ELSE (($1),($2))::TIMERANGE
                END;
$$
LANGUAGE 'sql' IMMUTABLE;

CREATE OR REPLACE FUNCTION timerange(XML,NAME,NAME)
RETURNS TIMERANGE AS $$
    SELECT timerange( (XPATH(($2),($1)))[1]::TEXT::TIME, (XPATH(($3),($1)))[1]::TEXT::TIME );
$$
LANGUAGE 'sql' IMMUTABLE;

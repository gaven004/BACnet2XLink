package com.serotonin.bacnet4j.adhoc;

import com.serotonin.bacnet4j.type.constructed.DateRange;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.primitive.Date;

public class DateRangeTest {
    public static void main(final String[] args) {
        final DateTime now = new DateTime(System.currentTimeMillis());
        final DateRange dateRange = new DateRange(new Date(2017, null, Date.UNSPECIFIED_DAY, null),
                new Date(2019, null, Date.UNSPECIFIED_DAY, null));
        System.out.println(dateRange.matches(now.getDate()));
    }
}

package com.serotonin.bacnet4j.type.constructed;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.serotonin.bacnet4j.enums.DayOfWeek;

public class DaysOfWeekTest {
    @Test
    public void containsDayOfWeek() {
        final DaysOfWeek dow = new DaysOfWeek(false);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setMonday(true);
        assertEquals(true, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setMonday(false);
        dow.setTuesday(true);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(true, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setTuesday(false);
        dow.setWednesday(true);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(true, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setWednesday(false);
        dow.setThursday(true);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(true, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setThursday(false);
        dow.setFriday(true);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(true, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setFriday(false);
        dow.setSaturday(true);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(true, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(false, dow.contains(DayOfWeek.SUNDAY));

        dow.setSaturday(false);
        dow.setSunday(true);
        assertEquals(false, dow.contains(DayOfWeek.MONDAY));
        assertEquals(false, dow.contains(DayOfWeek.TUESDAY));
        assertEquals(false, dow.contains(DayOfWeek.WEDNESDAY));
        assertEquals(false, dow.contains(DayOfWeek.THURSDAY));
        assertEquals(false, dow.contains(DayOfWeek.FRIDAY));
        assertEquals(false, dow.contains(DayOfWeek.SATURDAY));
        assertEquals(true, dow.contains(DayOfWeek.SUNDAY));
    }

    @Test
    public void containsIndex() {
        final DaysOfWeek dow = new DaysOfWeek(false);
        assertEquals(false, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setMonday(true);
        assertEquals(true, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setMonday(false);
        dow.setTuesday(true);
        assertEquals(false, dow.contains(0));
        assertEquals(true, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setTuesday(false);
        dow.setWednesday(true);
        assertEquals(false, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(true, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setWednesday(false);
        dow.setThursday(true);
        assertEquals(false, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(true, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setThursday(false);
        dow.setFriday(true);
        assertEquals(false, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(true, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setFriday(false);
        dow.setSaturday(true);
        assertEquals(false, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(true, dow.contains(5));
        assertEquals(false, dow.contains(6));

        dow.setSaturday(false);
        dow.setSunday(true);
        assertEquals(false, dow.contains(0));
        assertEquals(false, dow.contains(1));
        assertEquals(false, dow.contains(2));
        assertEquals(false, dow.contains(3));
        assertEquals(false, dow.contains(4));
        assertEquals(false, dow.contains(5));
        assertEquals(true, dow.contains(6));
    }
}

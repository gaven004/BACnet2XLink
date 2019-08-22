package com.serotonin.bacnet4j.npdu.ip;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IpNetworkUtilsTest {
    @Test
    public void createSubmask() {
        assertEquals(0x00000000L, IpNetworkUtils.createMask(0));
        assertEquals(0x80000000L, IpNetworkUtils.createMask(1));
        assertEquals(0xC0000000L, IpNetworkUtils.createMask(2));
        assertEquals(0xE0000000L, IpNetworkUtils.createMask(3));
        assertEquals(0xF0000000L, IpNetworkUtils.createMask(4));
        assertEquals(0xF8000000L, IpNetworkUtils.createMask(5));
        assertEquals(0xFC000000L, IpNetworkUtils.createMask(6));
        assertEquals(0xFE000000L, IpNetworkUtils.createMask(7));
        assertEquals(0xFF000000L, IpNetworkUtils.createMask(8));
        assertEquals(0xFF800000L, IpNetworkUtils.createMask(9));
        assertEquals(0xFFC00000L, IpNetworkUtils.createMask(10));
        assertEquals(0xFFE00000L, IpNetworkUtils.createMask(11));
        assertEquals(0xFFF00000L, IpNetworkUtils.createMask(12));
        assertEquals(0xFFF80000L, IpNetworkUtils.createMask(13));
        assertEquals(0xFFFC0000L, IpNetworkUtils.createMask(14));
        assertEquals(0xFFFE0000L, IpNetworkUtils.createMask(15));
        assertEquals(0xFFFF0000L, IpNetworkUtils.createMask(16));
        assertEquals(0xFFFF8000L, IpNetworkUtils.createMask(17));
        assertEquals(0xFFFFC000L, IpNetworkUtils.createMask(18));
        assertEquals(0xFFFFE000L, IpNetworkUtils.createMask(19));
        assertEquals(0xFFFFF000L, IpNetworkUtils.createMask(20));
        assertEquals(0xFFFFF800L, IpNetworkUtils.createMask(21));
        assertEquals(0xFFFFFC00L, IpNetworkUtils.createMask(22));
        assertEquals(0xFFFFFE00L, IpNetworkUtils.createMask(23));
        assertEquals(0xFFFFFF00L, IpNetworkUtils.createMask(24));
        assertEquals(0xFFFFFF80L, IpNetworkUtils.createMask(25));
        assertEquals(0xFFFFFFC0L, IpNetworkUtils.createMask(26));
        assertEquals(0xFFFFFFE0L, IpNetworkUtils.createMask(27));
        assertEquals(0xFFFFFFF0L, IpNetworkUtils.createMask(28));
        assertEquals(0xFFFFFFF8L, IpNetworkUtils.createMask(29));
        assertEquals(0xFFFFFFFCL, IpNetworkUtils.createMask(30));
        assertEquals(0xFFFFFFFEL, IpNetworkUtils.createMask(31));
        assertEquals(0xFFFFFFFFL, IpNetworkUtils.createMask(32));
    }
}

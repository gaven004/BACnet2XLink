package com.serotonin.bacnet4j.npdu.ip;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IpNetworkBuilderTest {
    @Test
    public void withSubnet16() {
        final IpNetworkBuilder builder = new IpNetworkBuilder().withSubnet("192.168.0.0", 16);
        assertEquals("192.168.255.255", builder.getBroadcastAddress());
        assertEquals("255.255.0.0", builder.getSubnetMask());
    }

    @Test
    public void withBroadcast16() {
        final IpNetworkBuilder builder = new IpNetworkBuilder().withBroadcast("192.168.255.255", 16);
        assertEquals("192.168.255.255", builder.getBroadcastAddress());
        assertEquals("255.255.0.0", builder.getSubnetMask());
    }

    @Test
    public void withSubnet24() {
        final IpNetworkBuilder builder = new IpNetworkBuilder().withSubnet("192.168.2.0", 24);
        assertEquals("192.168.2.255", builder.getBroadcastAddress());
        assertEquals("255.255.255.0", builder.getSubnetMask());
    }

    @Test
    public void withBroadcast24() {
        final IpNetworkBuilder builder = new IpNetworkBuilder().withBroadcast("192.168.4.255", 24);
        assertEquals("192.168.4.255", builder.getBroadcastAddress());
        assertEquals("255.255.255.0", builder.getSubnetMask());
    }

    @Test
    public void withSubnet19() {
        final IpNetworkBuilder builder = new IpNetworkBuilder().withSubnet("192.168.192.0", 19);
        assertEquals("192.168.223.255", builder.getBroadcastAddress());
        assertEquals("255.255.224.0", builder.getSubnetMask());
    }

    @Test
    public void withBroadcast19() {
        final IpNetworkBuilder builder = new IpNetworkBuilder().withBroadcast("192.168.223.255", 19);
        assertEquals("192.168.223.255", builder.getBroadcastAddress());
        assertEquals("255.255.224.0", builder.getSubnetMask());
    }
}

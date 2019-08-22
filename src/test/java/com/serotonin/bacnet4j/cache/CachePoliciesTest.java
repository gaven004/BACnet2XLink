package com.serotonin.bacnet4j.cache;

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.Test;

import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public class CachePoliciesTest {
    @Test
    public void devices() {
        final CachePolicies policies = new CachePolicies();

        policies.putDevicePolicy(1234, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        assertEquals(RemoteEntityCachePolicy.EXPIRE_1_DAY, policies.getDevicePolicy(1234));
        assertEquals(RemoteEntityCachePolicy.EXPIRE_15_MINUTES, policies.getDevicePolicy(1235));
    }

    @Test
    public void objects() {
        final CachePolicies policies = new CachePolicies();

        // Use object type ids that would not normally be found.
        final ObjectIdentifier oid = new ObjectIdentifier(2345, 0);
        final ObjectIdentifier oid2 = new ObjectIdentifier(2346, 1);

        policies.putObjectPolicy(1234, oid, RemoteEntityCachePolicy.EXPIRE_15_MINUTES);
        policies.putObjectPolicy(1234, null, RemoteEntityCachePolicy.EXPIRE_1_HOUR);
        policies.putObjectPolicy(null, oid, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        assertEquals(RemoteEntityCachePolicy.EXPIRE_15_MINUTES, policies.getObjectPolicy(1234, oid));
        assertEquals(RemoteEntityCachePolicy.EXPIRE_1_DAY, policies.getObjectPolicy(1235, oid));
        assertEquals(RemoteEntityCachePolicy.EXPIRE_1_HOUR, policies.getObjectPolicy(1234, oid2));
        assertEquals(RemoteEntityCachePolicy.NEVER_EXPIRE, policies.getObjectPolicy(1235, oid2));
    }

    @Test
    public void properties() {
        final CachePolicies policies = new CachePolicies();

        // Use object type ids that would not normally be found.
        final ObjectIdentifier oid = new ObjectIdentifier(2345, 0);
        final ObjectIdentifier oid2 = new ObjectIdentifier(2346, 1);

        // Use property ids that would not normally be found.
        final PropertyIdentifier pid = PropertyIdentifier.forId(3456);
        final PropertyIdentifier pid2 = PropertyIdentifier.forId(3457);

        final RemoteEntityCachePolicy policy1 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(2));
        final RemoteEntityCachePolicy policy2 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(3));
        final RemoteEntityCachePolicy policy3 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(4));
        final RemoteEntityCachePolicy policy4 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(5));
        final RemoteEntityCachePolicy policy5 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(6));
        final RemoteEntityCachePolicy policy6 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(7));
        final RemoteEntityCachePolicy policy7 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(8));
        final RemoteEntityCachePolicy policy8 = new RemoteEntityCachePolicy.TimedExpiry(Duration.ofDays(9));

        policies.putPropertyPolicy(1234, oid, pid, policy1);
        policies.putPropertyPolicy(1234, oid, null, policy2);
        policies.putPropertyPolicy(1234, null, pid, policy3);
        policies.putPropertyPolicy(1234, null, null, policy4);
        policies.putPropertyPolicy(null, oid, pid, policy5);
        policies.putPropertyPolicy(null, oid, null, policy6);
        policies.putPropertyPolicy(null, null, pid, policy7);
        policies.putPropertyPolicy(null, null, null, policy8);

        assertEquals(policy1, policies.getPropertyPolicy(1234, oid, pid));
        assertEquals(policy2, policies.getPropertyPolicy(1234, oid, pid2));
        assertEquals(policy3, policies.getPropertyPolicy(1234, oid2, pid));
        assertEquals(policy4, policies.getPropertyPolicy(1234, oid2, pid2));
        assertEquals(policy5, policies.getPropertyPolicy(1235, oid, pid));
        assertEquals(policy6, policies.getPropertyPolicy(1235, oid, pid2));
        assertEquals(policy7, policies.getPropertyPolicy(1235, oid2, pid));
        assertEquals(policy8, policies.getPropertyPolicy(1235, oid2, pid2));
    }
}

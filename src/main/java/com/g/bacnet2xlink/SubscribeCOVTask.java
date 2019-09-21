package com.g.bacnet2xlink;

import java.util.List;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVPropertyRequest;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeCOVTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SubscribeCOVTask.class);

    private LocalDevice ld;
    private RemoteDevice rd;
    private List<ObjectIdentifier> oids;
    private UnsignedInteger lifetime;

    public SubscribeCOVTask(LocalDevice ld, RemoteDevice rd, List<ObjectIdentifier> oids, UnsignedInteger lifetime) {
        this.ld = ld;
        this.rd = rd;
        this.oids = oids;
        this.lifetime = lifetime;
    }

    @Override
    public void run() {
        log.info("启动COV订阅任务...");

        UnsignedInteger subscriberProcessIdentifier = new UnsignedInteger(ld.getInstanceNumber());
        for (ObjectIdentifier oid : oids) {
            try {
                SubscribeCOVPropertyRequest req = new SubscribeCOVPropertyRequest(subscriberProcessIdentifier,
                        oid, Boolean.TRUE, lifetime, new PropertyReference(PropertyIdentifier.statusFlags), new Real(0));
                log.info("订阅[{}]的COV事件", oid);
                ld.send(rd, req).get();
            } catch (BACnetException e) {
                log.warn("COV订阅任务异常，oid={}, exception={}", oid, e.getMessage());
            }
        }

        log.info("COV订阅任务结束");
    }
}

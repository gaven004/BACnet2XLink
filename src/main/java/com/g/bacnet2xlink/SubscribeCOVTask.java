package com.g.bacnet2xlink;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVPropertyRequest;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Event;
import com.g.bacnet2xlink.definition.Product;

public class SubscribeCOVTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SubscribeCOVTask.class);

    private LocalDevice ld;
    private RemoteDevice rd;
    private Configuration cfg;
    private UnsignedInteger lifetime;

    public SubscribeCOVTask(LocalDevice ld, RemoteDevice rd, Configuration cfg, UnsignedInteger lifetime) {
        this.ld = ld;
        this.rd = rd;
        this.cfg = cfg;
        this.lifetime = lifetime;
    }

    @Override
    public void run() {
        log.info("启动COV订阅任务...");

        UnsignedInteger subscriberProcessIdentifier = new UnsignedInteger(ld.getInstanceNumber());

        for (Product product : cfg.getProducts()) {
            for (Device device : product.getDevices()) {
                for (Event event : device.getEvents()) {
                    log.info("订阅设备[mac: {}]对象[{} {}]属性[{}]的COV事件", device.getMac(), event.getObjectType(),
                            event.getObjectId(), event.getCovProperty());
                    try {
                        ObjectIdentifier oid = new ObjectIdentifier(ObjectType.forName(event.getObjectType()),
                                event.getObjectId());
                        PropertyReference pr = new PropertyReference(PropertyIdentifier.forName(event.getCovProperty()));
                        SubscribeCOVPropertyRequest req = new SubscribeCOVPropertyRequest(subscriberProcessIdentifier,
                                oid, Boolean.TRUE, lifetime, pr, new Real(0));
                        ld.send(rd, req).get();
                    } catch (BACnetException e) {
                        log.warn("COV订阅异常", e);
                    }
                }
            }
        }

        log.info("COV订阅任务结束");
    }
}

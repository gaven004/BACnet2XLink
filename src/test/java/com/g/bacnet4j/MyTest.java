package com.g.bacnet4j;

import java.util.List;
import java.util.Random;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTest {
    static final Logger LOG = LoggerFactory.getLogger(MyTest.class);

    IpNetworkBuilder builder;
    IpNetwork network;
    LocalDevice localDevice;
    RemoteDevice remoteDevice;

    @Before
    public void before() throws Exception {
        builder = new IpNetworkBuilder();
        builder.withBroadcast("192.168.31.255", 24);
        network = builder.build();
        localDevice = new LocalDevice(ObjectIdentifier.UNINITIALIZED, new DefaultTransport(network));

        try {
            LOG.info("Start initialize localDevice");
            localDevice.initialize();
        } catch (Exception e) {
            LOG.error("Unable to initialize localDevice", e);
            System.exit(1);
        }

        try {
            while (remoteDevice == null) {
                LOG.info("Start get remoteDevice");
                remoteDevice = localDevice.getRemoteDeviceBlocking(505528, 10000);
                LOG.info("Get extended device information");
                getExtendedDeviceInformation();
            }
        } catch (Exception e) {
            LOG.error("Unable to get remoteDevice", e);
            System.exit(1);
        }
    }

    @Test
    @Ignore
    public void writeAnalogValue() {
        LOG.info("");
        LOG.info("Write Analog Value Test");

        ObjectIdentifier oid = new ObjectIdentifier(ObjectType.analogValue, 3000143);
        Real value = new Real(new Random().nextFloat());
        try {
            LOG.info("Object before write");
            printObject(oid);

            RequestUtils.writePresentValue(localDevice, remoteDevice, oid, value);

            LOG.info("Object after write");
            printObject(oid);
        } catch (BACnetException e) {
            LOG.warn(String.format("Unable to write analog value to %s", oid), e);
        }
    }

    @Test
    @Ignore
    public void writeBinaryValue() {
        LOG.info("");
        LOG.info("Write Binary Value Test");

        ObjectIdentifier oid = new ObjectIdentifier(ObjectType.binaryValue, 3000147);
        try {
            LOG.info("Object before write");
            printObject(oid);

            Encodable encodable = RequestUtils.readProperty(localDevice, remoteDevice, oid, PropertyIdentifier.presentValue, null);

            if (encodable.equals(BinaryPV.inactive)) {
                RequestUtils.writePresentValue(localDevice, remoteDevice, oid, BinaryPV.active);
            } else {
                RequestUtils.writePresentValue(localDevice, remoteDevice, oid, BinaryPV.inactive);
            }

            LOG.info("Object after write");
            printObject(oid);
        } catch (BACnetException e) {
            LOG.warn(String.format("Unable to write analog value to %s", oid), e);
        }
    }

    @Test
    public void printDevice() throws BACnetException {
        LOG.info("");
        LOG.info("Print Device");

        showDeviceInformation();

        LOG.info("Start read properties");

        // 设备的对象列表中，已包含自身，不必单列处理
/*
        printObject(remoteDevice.getObjectIdentifier());
*/
        List<ObjectIdentifier> oids = ((SequenceOf<ObjectIdentifier>) RequestUtils.sendReadPropertyAllowNull(
                localDevice, remoteDevice, remoteDevice.getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();
        for (ObjectIdentifier oid : oids) {
            printObject(oid);
        }

        LOG.info("Print remote devices done...");
    }

    public void showDeviceInformation() throws BACnetException {
        LOG.info(remoteDevice.toExtendedString());
    }

    private void addPropertyReferences(ObjectIdentifier oid, PropertyReferences refs) {
        ObjectType ot = oid.getObjectType();

        List<ObjectPropertyTypeDefinition> definitions = ObjectProperties.getRequiredObjectPropertyTypeDefinitions(ot);
        for (ObjectPropertyTypeDefinition definition : definitions) {
            refs.add(oid, definition.getPropertyTypeDefinition().getPropertyIdentifier());
        }

        definitions = ObjectProperties.getOptionalObjectPropertyTypeDefinitions(ot);
        for (ObjectPropertyTypeDefinition definition : definitions) {
            refs.add(oid, definition.getPropertyTypeDefinition().getPropertyIdentifier());
        }
    }

    private void printObject(ObjectIdentifier oid) throws BACnetException {


        PropertyReferences refs = new PropertyReferences();
//            refs.add(oid, PropertyIdentifier.all); // 设备的属性，并不一定在BACnet4j有定义，所以只读取规范中必需的属性
        refs.add(oid, PropertyIdentifier.required);
        if (!oid.getObjectType().equals(ObjectType.program))
            refs.add(oid, PropertyIdentifier.optional);
//        addPropertyReferences(oid, refs);
        PropertyValues pvs = RequestUtils.readProperties(localDevice, remoteDevice, refs, true, null);

        LOG.info(String.format("\t%s", oid));
        for (ObjectPropertyReference opr : pvs) {
            if (oid.equals(opr.getObjectIdentifier())) {
                LOG.info(String.format("\t\t%s = %s", opr.getPropertyIdentifier().toString(),
                        pvs.getNoErrorCheck(opr)));
            }
        }
    }

    @After
    public void after() {
        // Shut down
        localDevice.terminate();
    }

    private void getExtendedDeviceInformation() throws BACnetException {
        Encodable property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.protocolObjectTypesSupported);
        remoteDevice.setDeviceProperty(PropertyIdentifier.protocolObjectTypesSupported, property);
        LOG.trace("protocolObjectTypesSupported: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.protocolServicesSupported);
        remoteDevice.setDeviceProperty(PropertyIdentifier.protocolServicesSupported, property);
        LOG.trace("protocolServicesSupported: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.protocolVersion);
        remoteDevice.setDeviceProperty(PropertyIdentifier.protocolVersion, property);
        LOG.trace("protocolVersion: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.protocolRevision);
        remoteDevice.setDeviceProperty(PropertyIdentifier.protocolRevision, property);
        LOG.trace("protocolRevision: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.objectName);
        remoteDevice.setDeviceProperty(PropertyIdentifier.objectName, property);
        LOG.trace("objectName: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.segmentationSupported);
        remoteDevice.setDeviceProperty(PropertyIdentifier.segmentationSupported, property);
        LOG.trace("segmentationSupported: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.maxApduLengthAccepted);
        remoteDevice.setDeviceProperty(PropertyIdentifier.maxApduLengthAccepted, property);
        LOG.trace("maxApduLengthAccepted: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.apduTimeout);
        remoteDevice.setDeviceProperty(PropertyIdentifier.apduTimeout, property);
        LOG.trace("apduTimeout: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.vendorIdentifier);
        remoteDevice.setDeviceProperty(PropertyIdentifier.vendorIdentifier, property);
        LOG.trace("vendorIdentifier: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.vendorName);
        remoteDevice.setDeviceProperty(PropertyIdentifier.vendorName, property);
        LOG.trace("vendorName: {}", property);

        property = RequestUtils.getProperty(localDevice, remoteDevice, PropertyIdentifier.modelName);
        remoteDevice.setDeviceProperty(PropertyIdentifier.modelName, property);
        LOG.trace("modelName: {}", property);
    }
}

package com.g.bacnet2xlink;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Event;
import com.g.bacnet2xlink.definition.EventMessage;
import com.g.bacnet2xlink.exception.UnknownDevice;
import com.g.bacnet2xlink.exception.UnknownEvent;
import com.g.bacnet2xlink.exception.UnknownEventMessage;

public class MyDeviceEventAdapter extends DeviceEventAdapter {
    private static final Logger log = LoggerFactory.getLogger(MyDeviceEventAdapter.class);

    private Configuration cfg;
    private Context context;

    public MyDeviceEventAdapter(Configuration cfg, Context context) {
        this.cfg = cfg;
        this.context = context;
    }

    @Override
    public void listenerException(final Throwable e) {
        log.warn("Listener Exception", e);
    }

    @Override
    public void covNotificationReceived(final UnsignedInteger subscriberProcessIdentifier,
                                        final ObjectIdentifier initiatingDeviceIdentifier,
                                        final ObjectIdentifier monitoredObjectIdentifier,
                                        final UnsignedInteger timeRemaining,
                                        final SequenceOf<PropertyValue> listOfValues) {
        if (initiatingDeviceIdentifier.getInstanceNumber() == cfg.getRemoteDeviceNumber()) {
            log.info("CovNotificationReceived: initiatingDeviceIdentifier - {}, monitoredObjectIdentifier - {}",
                    initiatingDeviceIdentifier, monitoredObjectIdentifier);
            log.info("\ttimeRemaining - {}", timeRemaining);
            log.info("\tlistOfValues - {}", listOfValues);

            try {
                Device device = context.getMonitoredDevice(monitoredObjectIdentifier);
                Event event = device.getEvent(monitoredObjectIdentifier);

                String eventName = event.getType(); // 物模型中的事件名
                Map<String, Object> pushData = new HashMap<>(); // 事件内容

                if ("present-value".equals(event.getCovProperty())) {
                    if (event.getMessageSet() != null) {
                        PropertyValue cov = null;
                        for (PropertyValue pv : listOfValues) {
                            if (pv.getPropertyIdentifier().toString().equals("present-value")) {
                                cov = pv;
                                break;
                            }
                        }
                        EventMessage message = event.getMessage(cov.getValue().toString());
                        pushData.put("code", message.getCode());
                        pushData.put("message", message.getMessage());
                    }
                } else if ("status_flags".equals(event.getCovProperty())) {
                    PropertyValue es = null; // EventState
                    for (PropertyValue pv : listOfValues) {
                        if (pv.getPropertyIdentifier().toString().equals("event_state")) {
                            es = pv;
                            break;
                        }
                    }
                    pushData.put("code", es.toString());
                    pushData.put("message", es.toString());
                }

                XlinkCmMqttClient xlinkClient = context.getXlinkClient(device.getProductId());
                log.info("发布事件[deviceId: {}, event: {}, data: {}]", device.getXDeviceId(), eventName, pushData);
                xlinkClient.publishEvent(device.getXDeviceId(), cfg.getVersion(), eventName, pushData, new Date());
            } catch (UnknownDevice | UnknownEvent | UnknownEventMessage unknown) {
                log.warn("发布事件失败：{}", unknown);
            } catch (Exception e) {
                log.warn("发布事件失败", e);
            }
        }
    }

    @Override
    public void eventNotificationReceived(final UnsignedInteger processIdentifier,
                                          final ObjectIdentifier initiatingDeviceIdentifier,
                                          final ObjectIdentifier eventObjectIdentifier,
                                          final TimeStamp timeStamp,
                                          final UnsignedInteger notificationClass,
                                          final UnsignedInteger priority,
                                          final EventType eventType,
                                          final CharacterString messageText,
                                          final NotifyType notifyType,
                                          final Boolean ackRequired,
                                          final EventState fromState,
                                          final EventState toState,
                                          final NotificationParameters eventValues) {
        if (initiatingDeviceIdentifier.getInstanceNumber() == cfg.getRemoteDeviceNumber()) {
            log.info("EventNotificationReceived：{}", messageText);
            log.info("\tinitiatingDeviceIdentifier：{}", initiatingDeviceIdentifier);
            log.info("\teventObjectIdentifier：{}", eventObjectIdentifier);
        }
    }

    @Override
    public void textMessageReceived(final ObjectIdentifier textMessageSourceDevice,
                                    final Choice messageClass,
                                    final MessagePriority messagePriority,
                                    final CharacterString message) {
        if (textMessageSourceDevice.getInstanceNumber() == cfg.getRemoteDeviceNumber()) {
            log.info("TextMessageReceived: {}", message);
            log.info("\ttextMessageSourceDevice: {}", textMessageSourceDevice);
        }
    }
}

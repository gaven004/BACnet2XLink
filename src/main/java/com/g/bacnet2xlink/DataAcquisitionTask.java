package com.g.bacnet2xlink;

import java.util.List;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAcquisitionTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DataAcquisitionTask.class);

    private LocalDevice ld;
    private RemoteDevice rd;
    private List<ObjectIdentifier> oids;

    public DataAcquisitionTask(LocalDevice ld, RemoteDevice rd, List<ObjectIdentifier> oids) {
        this.ld = ld;
        this.rd = rd;
        this.oids = oids;
    }

    @Override
    public void run() {
        log.info("启动数据采集上报任务...");

        try {
            log.info("读取对象数值：");
            PropertyValues pvs = RequestUtils.readOidPresentValues(ld, rd, oids, null);
            for (ObjectPropertyReference opr : pvs) {
                log.info("\t{} = {}", opr.getObjectIdentifier().toString(), pvs.getNoErrorCheck(opr));
            }

        } catch (BACnetException e) {
            // todo: handle exception
        }

        log.info("数据采集上报任务结束");
    }
}

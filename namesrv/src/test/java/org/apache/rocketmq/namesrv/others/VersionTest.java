package org.apache.rocketmq.namesrv.others;

import org.apache.rocketmq.common.MQVersion;
import org.junit.Test;

/**
 * @author jiangpingping
 * Created on 2020-09-29
 */
public class VersionTest {

    @Test
    public void printVersion() {
        System.out.println(MQVersion.CURRENT_VERSION);
    }

}

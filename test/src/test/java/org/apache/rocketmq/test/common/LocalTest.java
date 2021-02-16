package org.apache.rocketmq.test.common;

import org.apache.rocketmq.common.constant.PermName;
import org.junit.Test;

/**
 * @author itlemon
 * Created on 2021-02-16
 */
public class LocalTest {

    @Test
    public void testPerm() {
        System.out.println(((PermName.PERM_READ | PermName.PERM_WRITE) & PermName.PERM_WRITE) == PermName.PERM_WRITE);
    }

}

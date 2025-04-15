package org.wangyang.start_up;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.vm.VM;

public class ObjectAddressTest {
    @Test
    void test_address_change_after_gc() {
        Object object = new Object();
        long addressBefore = VM.current().addressOf(object);
        System.gc();
        long addressAfter = VM.current().addressOf(object);
        Assertions.assertNotEquals(addressBefore, addressAfter);
    }
}

package org.wangyang.start_up;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.vm.VM;
import org.wangyang.gc.BigObject;
import org.wangyang.gc.DirectBuffer;
import org.wangyang.gc.StaticMap;
import utils.DebugDriver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectAddressTest {
    void AssertEquals(Object a, Object b) {
        if (!a.equals(b)) {
            throw new IllegalStateException();
        }
    }

    void AssertNotEquals(Object a, Object b) {
        if (a.equals(b)) {
            throw new IllegalStateException();
        }
    }


    @Test
    void test_address_change_after_gc() {
        Object object = new Object();
        long addressBefore = VM.current().addressOf(object);
        System.gc();
        long addressAfter = VM.current().addressOf(object);
        Assertions.assertNotEquals(addressBefore, addressAfter);
    }

    @Test
    void test_big_object() throws IOException {
        Set<String> options = new HashSet<>();
        options.add("-Xmx500m");
        DebugDriver debugDriver = new DebugDriver(BigObject.class, options);
        AtomicInteger oomCaught = new AtomicInteger();
        debugDriver.addExceptionHandler(new DebugDriver.ExceptionHandler(
                OutOfMemoryError.class, event -> oomCaught.incrementAndGet()
        ));
        debugDriver.debug();
        AssertEquals(1, oomCaught.get());
    }

    @Test
    void test_direct_buffer() throws IOException {
        Set<String> options = new HashSet<>();
        options.add("-XX:MaxDirectMemorySize=500M");
        DebugDriver debugDriver = new DebugDriver(DirectBuffer.class, options);
        AtomicInteger oomCaught = new AtomicInteger();
        debugDriver.addExceptionHandler(new DebugDriver.ExceptionHandler(
                OutOfMemoryError.class, event -> {
            oomCaught.incrementAndGet();
            ObjectReference exception = event.exception();
            Field message = exception.referenceType().fieldByName("detailMessage");
            String msgValue = String.valueOf(exception.getValue(message));
            AssertEquals("\"Direct buffer memory\"", msgValue);
        }));
        debugDriver.debug();
    }

    @Test
    void test_static_map() throws IOException {
        Set<String> options = new HashSet<>();
        options.add("-Xmx200m");
        options.add("-Xlog:gc*");
        DebugDriver debugDriver = new DebugDriver(StaticMap.class, options);
        AtomicInteger oomCaught = new AtomicInteger();
        debugDriver.addExceptionHandler(new DebugDriver.ExceptionHandler(
                OutOfMemoryError.class, event -> {
            oomCaught.incrementAndGet();
        }));
        debugDriver.setDisconnectHandler(event -> {
            AssertNotEquals(0, oomCaught.get());
        });
        debugDriver.debug();
    }
}

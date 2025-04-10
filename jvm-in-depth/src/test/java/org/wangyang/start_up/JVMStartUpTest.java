package org.wangyang.start_up;

import com.sun.jdi.Value;
import org.junit.jupiter.api.Test;
import utils.DebugDriver;
import utils.Debugger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class JVMStartUpTest {
    void AssertEquals(Object a, Object b) {
        if (!a.equals(b)) {
            throw new IllegalStateException();
        }
    }

    @Test
    void test_load_superclass_object() throws IOException, ClassNotFoundException {
        DebugDriver debugDriver = new DebugDriver(InitialClass.class);
        Class<?> klass = InitialClass.class.getClassLoader().loadClass("jdk.internal.loader.ClassLoaders$AppClassLoader");
        int load_class_start = 170;
        AtomicInteger counter = new AtomicInteger();
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(klass, false, load_class_start, event -> {
            List<Value> argumentValues = Debugger.getArguments(event);
            String loadingClassName = argumentValues.get(0).toString();
            if (counter.get() == 0) {
                AssertEquals("\"org.wangyang.start_up.InitialClass\"", loadingClassName);
            } else if (counter.get() == 1){
                AssertEquals("\"org.wangyang.start_up.SuperInterface0\"", loadingClassName);
            } else if (counter.get() == 2) {
                AssertEquals("\"java.lang.Object\"", loadingClassName);
            } else if (counter.get() == 3) {
                AssertEquals("\"org.wangyang.start_up.SuperInterface1\"", loadingClassName);
            } else if (counter.get() == 4) {
                AssertEquals("\"org.wangyang.start_up.SuperClass\"", loadingClassName);
            }
            counter.incrementAndGet();
        }));
        debugDriver.debug();
    }

    @Test
    void test_super_class_init_first() throws IOException {
        DebugDriver debugDriver = new DebugDriver(SuperInterfaceNotInit.class);
        AtomicInteger counter = new AtomicInteger();
        int super_class_pos = 5;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(SuperClassInit.class, true, super_class_pos, event -> {
            AssertEquals(0, counter.getAndIncrement());
        }));
        int static_block_pos = 11;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(SuperInterfaceNotInit.class, true, static_block_pos, event -> {
                AssertEquals(1, counter.getAndIncrement());
        }));
        debugDriver.debug();
    }

    @Test
    void test_super_interface_init_not_triggered_by_interface_init() throws IOException {
        DebugDriver debugDriver = new DebugDriver(SuperInterfaceNotInit.class);
        AtomicInteger superInitCounter = new AtomicInteger();
        AtomicInteger initCounter = new AtomicInteger();
        int super_interface_pos = 4;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(SuperInterfaceInit.class, true, super_interface_pos, event -> {
            superInitCounter.incrementAndGet();
        }));
        int interface_pos = 8;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(InterfaceInit.class, true, interface_pos, event -> {
            initCounter.incrementAndGet();
        }));
        debugDriver.setDisconnectHandler(event -> {
            AssertEquals(1, initCounter.get());
            AssertEquals(0, superInitCounter.get());
        });
        debugDriver.debug();
    }

    @Test
    void test_static_field_reference_only_init_declaring_class() throws IOException {
        DebugDriver debugDriver = new DebugDriver(StaticFieldReference.class);
        AtomicInteger declaringCounter = new AtomicInteger();
        AtomicInteger referencingCounter = new AtomicInteger();
        int declaring_class_pos = 4;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(FieldDeclaringClass.class, true, declaring_class_pos, event -> {
            declaringCounter.incrementAndGet();
        }));
        int referencing_class = 8;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(FieldReferencingClass.class, true, referencing_class, event -> {
            referencingCounter.incrementAndGet();
        }));
        debugDriver.setDisconnectHandler(event -> {
            AssertEquals(0, referencingCounter.get());
            AssertEquals(1, declaringCounter.get());
        });
        debugDriver.debug();
    }

    @Test
    void test_class_instance_create_order() throws IOException {
        DebugDriver debugDriver = new DebugDriver(InstanceInit.class);
        AtomicInteger counter = new AtomicInteger();
        int super_class_pos = 5;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(SuperClassOfInstanceInit.class, true, super_class_pos, event -> {
            AssertEquals(0, counter.getAndIncrement());
        }));
        int init_block_pos = 11;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(InstanceInit.class, true, init_block_pos, event -> {
            AssertEquals(1, counter.getAndIncrement());
        }));
        int rest_instruction_pos = 15;
        debugDriver.addBreakPoint(new DebugDriver.BreakPointHandler(InstanceInit.class, true, rest_instruction_pos, event -> {
            AssertEquals(2, counter.getAndIncrement());
        }));
        debugDriver.debug();
    }
}


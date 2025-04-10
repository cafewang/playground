package utils;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;
import com.sun.jdi.request.ClassPrepareRequest;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.function.Consumer;

public class DebugDriver {
    private Debugger debugger;
    private List<BreakPointHandler> breakPointHandlers;
    private Consumer<VMDisconnectEvent> disconnectEventHandler;

    @AllArgsConstructor
    public static class BreakPointHandler {
        Class<?> targetClass;
        boolean setAfterPrepare;
        int pos;
        Consumer<BreakpointEvent> listener;
    }
    public DebugDriver(Class<?> mainClass) {
        debugger = new Debugger(mainClass);
        breakPointHandlers = new ArrayList<>();
    }

    public void setDisconnectHandler(Consumer<VMDisconnectEvent> disconnectEventHandler) {
        this.disconnectEventHandler = disconnectEventHandler;
    }

    public void addBreakPoint(BreakPointHandler breakPointHandler) {
        breakPointHandlers.add(breakPointHandler);
    }

    public void debug() throws IOException {
        VirtualMachine vm = null;
        try {
            vm = debugger.connectAndLaunchVM();
            Set<Class<?>> prepareRequestRegistered = new HashSet<>();
            for (BreakPointHandler handler : breakPointHandlers) {
                if (!handler.setAfterPrepare) {
                    debugger.setBreakPoint(vm, handler.targetClass.getName(), handler.pos);
                } else {
                    if (!prepareRequestRegistered.contains(handler.targetClass)) {
                        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
                        classPrepareRequest.addClassFilter(handler.targetClass.getName());
                        classPrepareRequest.enable();
                        prepareRequestRegistered.add(handler.targetClass);
                    }
                }
            }

            EventSet eventSet = null;
            while ((eventSet = vm.eventQueue().remove()) != null) {
                for (Event event : eventSet) {
                    if (event instanceof ClassPrepareEvent) {
                        for (BreakPointHandler handler : breakPointHandlers) {
                            if (handler.setAfterPrepare && handler.targetClass.getName().equals(((ClassPrepareEvent) event).referenceType().name())) {
                                debugger.setBreakPoint(vm, handler.targetClass.getName(), handler.pos);
                            }
                        }
                    }
                    if (event instanceof BreakpointEvent) {
                        for (BreakPointHandler handler : breakPointHandlers) {
                            if (handler.targetClass.getName().equals(((BreakpointEvent) event).location().declaringType().name())
                             && handler.pos == ((BreakpointEvent) event).location().lineNumber()) {
                                handler.listener.accept((BreakpointEvent) event);
                            }
                        }
                    }
                    if (event instanceof VMDisconnectEvent) {
                        if (Objects.nonNull(disconnectEventHandler)) {
                            disconnectEventHandler.accept((VMDisconnectEvent) event);
                        }
                    }
                    vm.resume();
                }
            }
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BufferedReader reader = new BufferedReader(new InputStreamReader(vm.process().getInputStream(), "gbk"));
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write("\n");
                writer.flush();
            }
        }
    }
}

package utils;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ExceptionRequest;
import lombok.AllArgsConstructor;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class DebugDriver {
    private final Debugger debugger;
    private final List<BreakPointHandler> breakPointHandlers;
    private final List<ExceptionHandler> exceptionHandlers;
    private Consumer<VMDisconnectEvent> disconnectEventHandler;
    private final Set<String> vmOptions;

    @AllArgsConstructor
    public static class BreakPointHandler {
        Class<?> targetClass;
        boolean setAfterPrepare;
        int pos;
        Consumer<BreakpointEvent> listener;
    }

    @AllArgsConstructor
    public static class ExceptionHandler {
        Class<? extends Throwable> targetException;
        Consumer<ExceptionEvent> listener;
    }

    public DebugDriver(Class<?> mainClass) {
        debugger = new Debugger(mainClass);
        breakPointHandlers = new ArrayList<>();
        vmOptions = new HashSet<>();
        exceptionHandlers = new ArrayList<>();
    }

    public DebugDriver(Class<?> mainClass, Set<String> vmOptions) {
        debugger = new Debugger(mainClass);
        breakPointHandlers = new ArrayList<>();
        this.vmOptions = new HashSet<>(vmOptions);
        exceptionHandlers = new ArrayList<>();
    }

    public void setDisconnectHandler(Consumer<VMDisconnectEvent> disconnectEventHandler) {
        this.disconnectEventHandler = disconnectEventHandler;
    }

    public void addExceptionHandler(ExceptionHandler exceptionEventHandler) {
        this.exceptionHandlers.add(exceptionEventHandler);
    }

    public void addBreakPoint(BreakPointHandler breakPointHandler) {
        breakPointHandlers.add(breakPointHandler);
    }

    public void debug() {
        VirtualMachine vm;
        try {
            vm = debugger.connectAndLaunchVM(vmOptions);
            InputStream inputStream = vm.process().getInputStream();
            InputStream errorStream = vm.process().getErrorStream();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"))) {
                    OutputStreamWriter writer = new OutputStreamWriter(System.out);
                    char[] buf = new char[4096];
                    int len;
                    while ((len = reader.read(buf)) != -1) {
                        writer.write(buf, 0, len);
                        writer.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "gbk"))) {
                    OutputStreamWriter writer = new OutputStreamWriter(System.err);
                    char[] buf = new char[4096];
                    int len;
                    while ((len = reader.read(buf)) != -1) {
                        writer.write(buf, 0, len);
                        writer.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            outputThread.start();
            errorThread.start();
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

            for (ExceptionHandler handler : exceptionHandlers) {
                ReferenceType referenceType = vm.classesByName(handler.targetException.getName()).get(0);
                ExceptionRequest request = vm.eventRequestManager().createExceptionRequest(referenceType, false, true);
                request.enable();
            }

            EventSet eventSet;
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
                    if (event instanceof ExceptionEvent) {
                        for (ExceptionHandler handler : exceptionHandlers) {
                            if (handler.targetException.getName().equals(((ExceptionEvent) event).exception().referenceType().name())) {
                                handler.listener.accept((ExceptionEvent) event);
                            }
                        }
                    }
                    vm.resume();
                }
            }
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

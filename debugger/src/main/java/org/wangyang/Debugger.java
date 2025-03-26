package org.wangyang;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.tools.jdi.GenericAttachingConnector;
import com.sun.tools.jdi.SocketAttachingConnector;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.Map;

@Getter
@Setter
public class Debugger {
    private Class<?> debuggee;
    private int[] breakPointLines;

    public VirtualMachine connectAndLaunchVM() throws Exception {

        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager()
                .defaultConnector();
        AttachingConnector attachingConnector = Bootstrap.virtualMachineManager()
                .attachingConnectors().stream()
                .filter(item -> item instanceof SocketAttachingConnector).findFirst().get();
        Map<String, Connector.Argument> arguments = attachingConnector.defaultArguments();
        arguments.get("port").setValue("5005");
        arguments.get("timeout").setValue("3000");
//        arguments.get("main").setValue(debuggee.getName());
//        String path = this.getClass().getClassLoader().getResource("").getPath();
//        arguments.get("options").setValue(String.format("-cp \"%s\"", path));
        VirtualMachine vm = attachingConnector.attach(arguments);
        vm.resume();
//        Thread outThread = redirect(vm.process().getInputStream(), System.out);
//        Thread errThread = redirect(vm.process().getErrorStream(), System.out);
        return vm;
    }

    private Thread redirect(InputStream inputStream, OutputStream out) {
        Thread t = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                    writer.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t.setDaemon(true);
        t.start();

        return t;
    }


    public void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debuggee.getName());
        classPrepareRequest.enable();
    }

    public void setBreakPoints(VirtualMachine vm) throws AbsentInformationException {
        ClassType classType = (ClassType) vm.classesByName(Debuggee.class.getName()).get(0);
        for(int lineNumber: breakPointLines) {
            Location location = classType.locationsOfLine(lineNumber).get(0);
            BreakpointRequest breakpointRequest = vm.eventRequestManager().createBreakpointRequest(location);
            breakpointRequest.enable();
        }
    }

    public void displayVariables(LocatableEvent event) throws IncompatibleThreadStateException,
            AbsentInformationException {
        StackFrame stackFrame = event.thread().frame(0);
        if(stackFrame.location().toString().contains(debuggee.getName())) {
            Map<LocalVariable, Value> visibleVariables = stackFrame
                    .getValues(stackFrame.visibleVariables());
            System.out.println("Variables at " + stackFrame.location().toString() +  " > ");
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                System.out.println(entry.getKey().name() + " = " + entry.getValue());
            }
        }
    }
}

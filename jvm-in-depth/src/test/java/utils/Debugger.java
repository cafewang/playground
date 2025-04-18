package utils;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Debugger {
    private final Class<?> mainClass;

    public Debugger(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public VirtualMachine connectAndLaunchVM(Set<String> vmOptions) throws Exception {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager()
                .defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(mainClass.getName());
        StringBuilder param = new StringBuilder();
        String workingDir = Debugger.class.getClassLoader().getResource("").getPath() + "../main";
        param.append(String.format("-Duser.dir=%s", workingDir));
        vmOptions.forEach(option -> param.append(" ").append(option));
        arguments.get("options").setValue(param.toString());
        return launchingConnector.launch(arguments);
    }

    public void setBreakPoint(VirtualMachine vm, String className, int pos) throws AbsentInformationException {
        ReferenceType referenceType = vm.classesByName(className).get(0);
        Location location = null;
        if (referenceType instanceof ClassType) {
            location = referenceType.locationsOfLine(pos).get(0);
        } else if (referenceType instanceof InterfaceType) {
            location = referenceType.locationsOfLine(pos).get(0);
        }
        BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
        bpReq.enable();
    }

    public static List<Value> getArguments(LocatableEvent event) {
        StackFrame frame;
        try {
            frame = event.thread().frame(0);
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
        return frame.getArgumentValues();
    }
}

package org.wangyang;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

public class Main {
    public static void main(String[] args) {
        Debugger debugger = new Debugger();
        debugger.setDebuggee(Debuggee.class);
        int[] breakPoints = {7};
        debugger.setBreakPointLines(breakPoints);
        VirtualMachine vm = null;

        try {
            vm = debugger.connectAndLaunchVM();
            debugger.setBreakPoints(vm);
            EventSet eventSet = null;
            while ((eventSet = vm.eventQueue().remove()) != null) {
                for (Event event : eventSet) {
                    if (event instanceof BreakpointEvent) {
                        debugger.displayVariables((LocatableEvent) event);
                    }
                    vm.resume();
                }
            }
        } catch (VMDisconnectedException e) {
            System.out.println("debug exit " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
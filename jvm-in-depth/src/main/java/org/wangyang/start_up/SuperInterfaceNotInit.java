package org.wangyang.start_up;

interface SuperInterfaceInit {
    int b = SuperInterfaceNotInit.printInt(2);
}

interface InterfaceInit extends SuperInterfaceInit {
    int a = SuperInterfaceNotInit.printInt(1);
}

public class SuperInterfaceNotInit {
    static int printInt(int v) {
        System.out.println(v);
        return v;
    }

    public static void main(String[] args) {
        System.out.println("main: " + InterfaceInit.a);
    }
}

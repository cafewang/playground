package org.wangyang.start_up;

class SuperClassOfInstanceInit {
    public SuperClassOfInstanceInit() {
        System.out.println("super class instance created");
    }
}

public class InstanceInit extends SuperClassOfInstanceInit {
    {
        System.out.println("instance created");
    }

    public InstanceInit() {
        System.out.println("executed last");
    }

    public static void main(String[] args) {
        InstanceInit instance = new InstanceInit();
    }
}

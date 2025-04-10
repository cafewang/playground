package org.wangyang.start_up;

class SuperClassInit {
    static {
        System.out.println("super class initializing");
    }
}

public class SuperClassInitFirst extends SuperClassInit {
    static {
        System.out.println("class initializing");
    }

    public static void main(String[] args) {
    }
}

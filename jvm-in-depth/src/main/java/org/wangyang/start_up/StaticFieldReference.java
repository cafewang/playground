package org.wangyang.start_up;

class FieldDeclaringClass {
    static int a = 1, b = StaticFieldReference.printInt(2);
}

class FieldReferencingClass extends FieldDeclaringClass {
    static int c = StaticFieldReference.printInt(3);
}

public class StaticFieldReference {
    static int printInt(int v) {
        System.out.println(v);
        return v;
    }

    public static void main(String[] args) {
        System.out.println("main: " + FieldReferencingClass.a);
    }
}

package org.wangyang;

public class Dog extends Animal implements  SuperInterface {
    private static final int LEGS = 4;
    private Dog[] kids;
    private String name = "Brian";

    public Dog() {
    }

    @Override
    public int doSomething(int param1) {
        return 100;
    }

    public void bark() {
        System.out.println("dog barking");
    }
}

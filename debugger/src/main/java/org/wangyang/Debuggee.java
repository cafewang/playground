package org.wangyang;

public class Debuggee {
    public static void main(String[] args) {
        String a = "asdfbdasf";
        for (int i = 0; i < 100_000_00; i++) {
            System.out.println(i + ":" + a);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

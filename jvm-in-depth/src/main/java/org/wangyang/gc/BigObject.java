package org.wangyang.gc;

public class BigObject {
    public static void main(String[] args) {
        int thirtyTwoMB = 1 << 25;
        byte[][] arrs = new byte[100][];
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            byte[] arr = new byte[thirtyTwoMB];
            arrs[i] = arr;
            System.out.println((System.currentTimeMillis() - start) + "ms");
        }
    }
}

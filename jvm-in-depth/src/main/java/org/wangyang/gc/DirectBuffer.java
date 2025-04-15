package org.wangyang.gc;

import java.nio.ByteBuffer;

public class DirectBuffer {
    public static void main(String[] args) {
        ByteBuffer[] arr = new ByteBuffer[10];
        int sixtyFourMB = 1 << 26;
        for (int i = 0; i < 10; i++) {
            arr[i] = ByteBuffer.allocateDirect(sixtyFourMB);
        }
    }
}

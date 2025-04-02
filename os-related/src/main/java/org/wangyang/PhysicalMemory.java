package org.wangyang;

public class PhysicalMemory {
    private final byte[] arr;

    public PhysicalMemory(int digits) {
        arr = new byte[1 << digits];
    }

    public byte read(int pos) {
        return arr[pos];
    }

    public void write(int pos, byte value) {
        arr[pos] = value;
    }

    public int size() {
        return arr.length;
    }
}

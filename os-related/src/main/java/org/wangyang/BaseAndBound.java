package org.wangyang;

public class BaseAndBound extends VirtualMemory {
    int base, bound;

    public BaseAndBound(int digits, PhysicalMemory physicalMemory, int base, int bound) throws IllegalAccessException {
        super(digits, physicalMemory);
        this.base = base;
        this.bound = bound;
        if (base + bound > physicalMemory.size()) {
            throw new IllegalAccessException();
        }
    }

    public void write(int pos, byte value) throws IllegalAccessException {
        if (pos < 0 || pos >= bound) {
            throw new IllegalAccessException();
        }
        physicalMemory.write(base + pos, value);
    }

    public byte read(int pos) throws IllegalAccessException {
        if (pos < 0 || pos >= bound) {
            throw new IllegalAccessException();
        }
        return physicalMemory.read(base + pos);
    }
}

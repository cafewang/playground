package org.wangyang;

public abstract class VirtualMemory {
    protected final int digits;
    protected final PhysicalMemory physicalMemory;

    public VirtualMemory(int digits, PhysicalMemory physicalMemory) {
        this.digits = digits;
        this.physicalMemory = physicalMemory;
    }
}

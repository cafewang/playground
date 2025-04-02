package org.wangyang;

public class Segment extends VirtualMemory {
    private final BaseAndBound codeSeg, heapSeg, stackSeg;

    public Segment(int digits, PhysicalMemory physicalMemory, int codeBase, int codeBound,
                   int heapBase, int heapBound, int stackBase, int stackBound) throws IllegalAccessException {
        super(digits, physicalMemory);
        this.codeSeg = new BaseAndBound(digits, physicalMemory, codeBase, codeBound);
        this.heapSeg = new BaseAndBound(digits, physicalMemory, heapBase, heapBound);
        this.stackSeg = new BaseAndBound(digits, physicalMemory, stackBase, stackBound);
    }

    public void writeCode(int pos, byte value) throws IllegalAccessException {
        codeSeg.write(pos, value);
    }

    public byte readCode(int pos) throws IllegalAccessException {
        return codeSeg.read(pos);
    }
}

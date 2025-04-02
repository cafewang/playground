package org.wangyang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SegmentTest {
    @Test
    public void illegal_base_and_bound() {
        PhysicalMemory physicalMemory = new PhysicalMemory(4);
        int codeBase = 10;
        int codeBound = 8;
        assertThrows(IllegalAccessException.class, () -> new Segment(3, physicalMemory,
                codeBase, codeBound, 0, 2, 2, 2));
    }

    @Test
    public void illegal_access() throws IllegalAccessException {
        PhysicalMemory physicalMemory = new PhysicalMemory(4);
        Segment segment = new Segment(3, physicalMemory,
                4, 4, 0, 2, 2, 2);
        segment.writeCode(0, (byte) 1);
        int illegal_pos = 8;
        assertThrows(IllegalAccessException.class, () -> segment.readCode(illegal_pos));
    }

    @Test
    public void read_and_write() throws IllegalAccessException {
        PhysicalMemory physicalMemory = new PhysicalMemory(4);
        Segment segment = new Segment(3, physicalMemory,
                4, 4, 0, 2, 2, 2);
        segment.writeCode(2, (byte) 1);
        assertEquals((byte) 1, segment.readCode(2));
    }
}
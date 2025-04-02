package org.wangyang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseAndBoundTest {
    @Test
    public void illegal_base_and_bound() {
        PhysicalMemory physicalMemory = new PhysicalMemory(4);
        int base = 10;
        int bound = 8;
        assertThrows(IllegalAccessException.class, () -> new BaseAndBound(3, physicalMemory, base, bound));
    }

    @Test
    public void illegal_access() throws IllegalAccessException {
        PhysicalMemory physicalMemory = new PhysicalMemory(4);
        BaseAndBound baseAndBound = new BaseAndBound(3, physicalMemory, 4, 8);
        baseAndBound.write(0, (byte) 1);
        int illegal_pos = 8;
        assertThrows(IllegalAccessException.class, () -> baseAndBound.read(illegal_pos));
    }

    @Test
    public void read_and_write() throws IllegalAccessException {
        PhysicalMemory physicalMemory = new PhysicalMemory(4);
        BaseAndBound baseAndBound = new BaseAndBound(3, physicalMemory, 4, 8);
        baseAndBound.write(7, (byte) 1);
        assertEquals((byte) 1, baseAndBound.read(7));
    }

}
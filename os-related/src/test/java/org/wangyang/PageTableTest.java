package org.wangyang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PageTableTest {
    @Test
    public void no_page_allocated() {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageTableTotal = 64;
        int pageTableBase = (1 << 8) - pageTableTotal;
        PageTable pageTable = new PageTable(8, physicalMemory, 4, pageTableBase);
        pageTable.printTable();
    }

    @Test
    public void duplicate_allocation() {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageTableTotal = 64;
        int pageTableBase = (1 << 8) - pageTableTotal;
        PageTable pageTable = new PageTable(8, physicalMemory, 4, pageTableBase);
        pageTable.allocatePage(0, 1);
        pageTable.printTable();
        Assertions.assertThrows(RuntimeException.class, () ->
                pageTable.allocatePage(2, 1));
        Assertions.assertThrows(RuntimeException.class, () ->
                pageTable.allocatePage(0, 2));
    }

    @Test
    public void allocate_write() throws IllegalAccessException {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageTableTotal = 64;
        int pageTableBase = (1 << 8) - pageTableTotal;
        PageTable pageTable = new PageTable(8, physicalMemory, 4, pageTableBase);
        pageTable.allocatePage(0, 1);
        pageTable.allocatePage(1, 0);
        pageTable.printTable();
        pageTable.write(0, 0, (byte) 1);
        Assertions.assertEquals((byte) 1, pageTable.read(0, 0));
    }

    @Test
    public void write_unallocated() {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageTableTotal = 64;
        int pageTableBase = (1 << 8) - pageTableTotal;
        PageTable pageTable = new PageTable(8, physicalMemory, 4, pageTableBase);
        Assertions.assertThrows(IllegalAccessException.class, () ->
                pageTable.write(0, 0, (byte) 1));
    }

}
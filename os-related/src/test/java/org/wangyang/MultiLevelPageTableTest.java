package org.wangyang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MultiLevelPageTableTest {
    @Test
    public void init_status() {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageDirTotal = 16;
        int pageTableTotal = 64;
        int pageDirBase = (1 << 8) - pageDirTotal - pageTableTotal;
        MultiLevelPageTable multiLevelPageTable = new MultiLevelPageTable(8, physicalMemory, 4, pageDirBase);
        multiLevelPageTable.printTable();
        for (int i = 0; i < multiLevelPageTable.dirCount(); i++) {
            Assertions.assertTrue(multiLevelPageTable.getDir(i).getValid() != 1);
        }
    }

    @Test
    public void allocated() {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageDirTotal = 16;
        int pageTableTotal = 64;
        int pageDirBase = (1 << 8) - pageDirTotal - pageTableTotal;
        MultiLevelPageTable multiLevelPageTable = new MultiLevelPageTable(8, physicalMemory, 4, pageDirBase);
        multiLevelPageTable.allocatePage(0, 12, 0, 0);
        multiLevelPageTable.allocatePage(1, 13, 1, 1);
        multiLevelPageTable.allocatePage(2, 14, 2, 2);
        multiLevelPageTable.allocatePage(3, 15, 3, 3);
        multiLevelPageTable.printTable();
        Assertions.assertEquals(1, multiLevelPageTable.getEntry(multiLevelPageTable
                .getDir(0).getPhysicalFrameNo(), 0).getValid());
    }

    @Test
    public void illegal_access() {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageDirTotal = 16;
        int pageTableTotal = 64;
        int pageDirBase = (1 << 8) - pageDirTotal - pageTableTotal;
        MultiLevelPageTable multiLevelPageTable = new MultiLevelPageTable(8, physicalMemory, 4, pageDirBase);
        multiLevelPageTable.allocatePage(0, 12, 0, 0);
        int invalidVFN = 1;
        Assertions.assertThrows(IllegalAccessException.class,
                () -> multiLevelPageTable.read(0, invalidVFN, 0));
        int invalidPDN = 2;
        Assertions.assertThrows(IllegalAccessException.class,
                () -> multiLevelPageTable.read(invalidPDN, 0, 0));
    }

    @Test
    public void write_and_read() throws IllegalAccessException {
        PhysicalMemory physicalMemory = new PhysicalMemory(8);
        int pageDirTotal = 16;
        int pageTableTotal = 64;
        int pageDirBase = (1 << 8) - pageDirTotal - pageTableTotal;
        MultiLevelPageTable multiLevelPageTable = new MultiLevelPageTable(8, physicalMemory, 4, pageDirBase);
        multiLevelPageTable.allocatePage(0, 12, 0, 0);
        multiLevelPageTable.write(0, 0, 10, (byte) 1);
        Assertions.assertEquals(1, multiLevelPageTable.read(0, 0, 10));
    }

}
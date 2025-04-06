package org.wangyang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApproximatingLRUTest {

    @Test
    void cant_alloc_when_memory_and_disk_full() {
        int memorySize = 4, swapSpaceSize = 8;
        ApproximatingLRU lru = new ApproximatingLRU(memorySize, swapSpaceSize);
        for (int i = 0; i < memorySize + swapSpaceSize; i++) {
            lru.alloc();
        }
        Assertions.assertThrows(IllegalStateException.class, lru::alloc);
    }

    @Test
    void swap_before_alloc_when_memory_full() {
        int memorySize = 4, swapSpaceSize = 8;
        ApproximatingLRU lru = new ApproximatingLRU(memorySize, swapSpaceSize);
        for (int i = 0; i < memorySize; i++) {
            lru.alloc();
        }
        lru.alloc();
        Assertions.assertEquals(memorySize, lru.allPageInMemory().size());
        Assertions.assertEquals(1, lru.allPageInDisk().size());
    }

    @Test
    void swap_before_read_absent_page() throws IllegalAccessException {
        int memorySize = 4, swapSpaceSize = 8;
        ApproximatingLRU lru = new ApproximatingLRU(memorySize, swapSpaceSize);
        for (int i = 0; i < memorySize; i++) {
            lru.alloc();
        }
        lru.alloc();
        ApproximatingLRU.PageEntry pageInDisk = lru.allPageInDisk().stream().findFirst().get();
        lru.read(pageInDisk.VFN);
        Assertions.assertTrue(lru.allPageInMemory().stream()
                .anyMatch(entry -> entry.present && entry.VFN == pageInDisk.VFN));
    }

}
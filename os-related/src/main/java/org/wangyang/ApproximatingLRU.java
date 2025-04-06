package org.wangyang;

import java.util.*;
import java.util.stream.Collectors;

public class ApproximatingLRU {

    public static class PageEntry {
        int VFN;
        int PFN;
        boolean present;
        int blockNo;
        boolean use;
    }

    private final byte[] physicalMemory;
    private final byte[] swapSpace;
    private final Map<Integer, PageEntry> pageTable;
    private int maxVFN;

    public ApproximatingLRU(int memorySize, int swapSpaceSize) {
        physicalMemory = new byte[memorySize];
        swapSpace = new byte[swapSpaceSize];
        pageTable = new HashMap<>();
        maxVFN = -1;
    }

    public byte read(int VFN) throws IllegalAccessException {
        if (pageTable.containsKey(VFN)) {
            PageEntry pageEntry = pageTable.get(VFN);
            if (!pageEntry.present) {
                swap(pageEntry);
            }
            pageEntry.use = true;
            return physicalMemory[pageEntry.PFN];
        }
        throw new IllegalAccessException();
    }

    public int alloc() {
        if (pageTable.size() == physicalMemory.length + swapSpace.length) {
            throw new IllegalStateException();
        }

        int freePFN = getFstFreePFN();
        if (freePFN == physicalMemory.length) {
            for (PageEntry pageEntry : pageTable.values()) {
                if (pageEntry.present) {
                    store(pageEntry);
                    freePFN = pageEntry.PFN;
                    break;
                }
            }
        }

        PageEntry newEntry = new PageEntry();
        newEntry.VFN = ++maxVFN;
        newEntry.present = true;
        newEntry.PFN = freePFN;
        newEntry.use = true;
        pageTable.put(maxVFN, newEntry);
        return maxVFN;
    }

    private int getFstFreePFN() {
        Set<Integer> pfnInUse = new HashSet<>();
        for (PageEntry entry : pageTable.values()) {
            if (entry.present) {
                pfnInUse.add(entry.PFN);
            }
        }
        int freePFN = 0;
        while (pfnInUse.contains(freePFN)) {
            freePFN++;
        }
        return freePFN;
    }

    private int getFstFreeBlock() {
        Set<Integer> blockInUse = new HashSet<>();
        for (PageEntry entry : pageTable.values()) {
            if (!entry.present) {
                blockInUse.add(entry.blockNo);
            }
        }
        int freeBlockNo = 0;
        while (blockInUse.contains(freeBlockNo)) {
            freeBlockNo++;
        }
        return freeBlockNo;
    }

    private void store(PageEntry entryToStore) {
        int freeBlockNo = getFstFreeBlock();
        if (freeBlockNo == swapSpace.length) {
            throw new IllegalStateException();
        }
        entryToStore.present = false;
        entryToStore.blockNo = freeBlockNo;
        swapSpace[freeBlockNo] = physicalMemory[entryToStore.PFN];
    }

    private void swap(PageEntry entryToSwap)  {
        int freePFN = getFstFreePFN();
        if (freePFN < physicalMemory.length) {
            entryToSwap.present = true;
            entryToSwap.PFN = freePFN;
            return;
        }

        // no PFN available
        while (true) {
            for (PageEntry entryToReplace : pageTable.values()) {
                if (entryToReplace != entryToSwap && entryToReplace.present) {
                    if (entryToReplace.use) {
                        entryToReplace.use = false;
                    } else {
                        entryToSwap.present = true;
                        entryToReplace.present = false;
                        byte tmp = physicalMemory[entryToReplace.PFN];
                        physicalMemory[entryToReplace.PFN] = swapSpace[entryToReplace.blockNo];
                        swapSpace[entryToReplace.blockNo] = tmp;
                        entryToSwap.PFN = entryToReplace.PFN;
                        entryToReplace.blockNo = entryToSwap.blockNo;
                        return;
                    }
                }
            }
        }
    }

    public List<PageEntry> allPageInMemory() {
        return pageTable.values().stream()
                .filter(page -> page.present).collect(Collectors.toList());
    }

    public List<PageEntry> allPageInDisk() {
        return pageTable.values().stream()
                .filter(page -> !page.present).collect(Collectors.toList());
    }
}

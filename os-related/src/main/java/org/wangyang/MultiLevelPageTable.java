package org.wangyang;

import lombok.Data;

public class MultiLevelPageTable extends VirtualMemory {
    int pageSizeDigits;
    int pageDirBase;
    private static final int PAGE_DIR_ENTRY_DIGITS = 2;
    private static final int PAGE_TABLE_ENTRY_DIGITS = 2;

    @Data
    public static class PageTableEntry {
        private final byte physicalFrameNo;
        private final byte valid;
        private byte notDefined0, notDefined1;

        public PageTableEntry(byte physicalFrameNo, byte valid) {
            this.physicalFrameNo = physicalFrameNo;
            this.valid = valid;
        }
    }

    @Data
    public static class PageDirEntry {
        private final byte physicalFrameNo;
        private final byte valid;
        private byte notDefined0, notDefined1;

        public PageDirEntry(byte physicalFrameNo, byte valid) {
            this.physicalFrameNo = physicalFrameNo;
            this.valid = valid;
        }
    }

    public MultiLevelPageTable(int digits, PhysicalMemory physicalMemory, int pageSizeDigits, int pageDirBase) {
        super(digits, physicalMemory);
        this.pageDirBase = pageDirBase;
        this.pageSizeDigits = pageSizeDigits;
    }

    public void allocatePage(int dirNo, int dirPhysicalFrameNo, int virtualFrameNo, int physicalFrameNo) {
        PageDirEntry pageDirEntry = getDir(dirNo);
        if (pageDirEntry.valid != 1) {
            writeDirEntry(dirNo, dirPhysicalFrameNo);
        }
        PageTableEntry pageTableEntry = getEntry(dirPhysicalFrameNo, virtualFrameNo);
        if (pageTableEntry.valid != 1) {
            writeEntry(dirPhysicalFrameNo, virtualFrameNo, physicalFrameNo);
        }
    }

    private void writeDirEntry(int dirNo, int dirPhysicalFrameNo) {
        int pos = pageDirBase + dirNo * (1 << PAGE_DIR_ENTRY_DIGITS);
        physicalMemory.write(pos, (byte) dirPhysicalFrameNo);
        physicalMemory.write(pos + 1, (byte) 1);
    }

    private void writeEntry(int dirPhysicalFrameNo ,int virtualFrameNo, int physicalFrameNo) {
        int pageTableBase = dirPhysicalFrameNo * (1 << pageSizeDigits);
        int entryPos = pageTableBase + virtualFrameNo * (1 << PAGE_TABLE_ENTRY_DIGITS);
        physicalMemory.write(entryPos, (byte)physicalFrameNo);
        physicalMemory.write(entryPos + 1, (byte) 1);
    }

    public int dirCount() {
        return 1 << (digits - 2 * pageSizeDigits + PAGE_DIR_ENTRY_DIGITS);
    }

    public int entryCount() {
        return 1 << (pageSizeDigits - PAGE_DIR_ENTRY_DIGITS);
    }

    public void printTable() {
        for (int dirNo = 0; dirNo < dirCount(); dirNo++) {
            PageDirEntry pageDirEntry = getDir(dirNo);
            if (pageDirEntry.valid != 1) {
                System.out.println("PDN:" + dirNo + "| PFN:" + pageDirEntry.physicalFrameNo
                        + "| VALID:" + false);
            } else {
                System.out.println("PDN:" + dirNo + "| PFN:" + pageDirEntry.physicalFrameNo
                        + "| VALID:" + true);
                for (int virtualFrameNo = 0; virtualFrameNo < entryCount(); virtualFrameNo++) {
                    PageTableEntry pageTableEntry = getEntry(pageDirEntry.physicalFrameNo, virtualFrameNo);
                    System.out.println("    VFN:" + virtualFrameNo + "| PFN:" + pageTableEntry.physicalFrameNo
                            + "| VALID:" + (pageTableEntry.valid == 1));
                }
            }
        }

    }

    public PageDirEntry getDir(int dirNo) {
        int pos = pageDirBase + dirNo * (1 << PAGE_DIR_ENTRY_DIGITS);
        return new PageDirEntry(physicalMemory.read(pos), physicalMemory.read(pos + 1));
    }

    public int read(int dirNo, int virtualFrameNo, int pos) throws IllegalAccessException {
        PageDirEntry pageDirEntry = getDir(dirNo);
        if (pageDirEntry.valid != 1) {
            throw new IllegalAccessException();
        }
        PageTableEntry pageTableEntry = getEntry(pageDirEntry.physicalFrameNo, virtualFrameNo);
        int physicalFrameNo = pageTableEntry.physicalFrameNo;
        boolean valid = pageTableEntry.valid == 1;
        if (!valid) {
            throw new IllegalAccessException();
        }
        return physicalMemory.read(physicalFrameNo * (1 << pageSizeDigits) + pos);
    }

    public void write(int dirNo, int virtualFrameNo, int pos, byte value) throws IllegalAccessException {
        PageDirEntry pageDirEntry = getDir(dirNo);
        if (pageDirEntry.valid != 1) {
            throw new IllegalAccessException();
        }
        PageTableEntry pageTableEntry = getEntry(pageDirEntry.physicalFrameNo, virtualFrameNo);
        int physicalFrameNo = pageTableEntry.physicalFrameNo;
        boolean valid = pageTableEntry.valid == 1;
        if (!valid) {
            throw new IllegalAccessException();
        }
        physicalMemory.write(physicalFrameNo * (1 << pageSizeDigits) + pos, value);
    }

    public PageTableEntry getEntry(int physicalFrameNo, int virtualFrameNo) {
        int pageTableBase = physicalFrameNo * (1 << pageSizeDigits);
        int entryPos = pageTableBase + virtualFrameNo * (1 << PAGE_TABLE_ENTRY_DIGITS);
        return new PageTableEntry(physicalMemory.read(entryPos),
                physicalMemory.read(entryPos + 1));
    }
}

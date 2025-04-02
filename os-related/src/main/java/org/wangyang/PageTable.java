package org.wangyang;

public class PageTable extends VirtualMemory {
    int pageSizeDigits;
    int pageTableBase;
    private static final int PAGE_TABLE_ENTRY_DIGITS = 2;

    private static class PageTableEntry {
        private final byte physicalFrameNo;
        private final byte valid;
        private byte notDefined0, notDefined1;

        public PageTableEntry(byte physicalFrameNo, byte valid) {
            this.physicalFrameNo = physicalFrameNo;
            this.valid = valid;
        }
    }

    public PageTable(int digits, PhysicalMemory physicalMemory, int pageSizeDigits, int pageTableBase) {
        super(digits, physicalMemory);
        this.pageSizeDigits = pageSizeDigits;
        this.pageTableBase = pageTableBase;
    }

    public void allocatePage(int virtualFrameNo, int physicalFrameNo) {
        for (int i = 0; i < entryCount(); i++) {
            PageTableEntry pageTableEntry = getEntry(i);
            if (pageTableEntry.valid == 1 && (pageTableEntry.physicalFrameNo) == physicalFrameNo) {
                throw new IllegalArgumentException(String.format("physical frame %s allocated", physicalFrameNo));
            }
        }
        PageTableEntry pageTableEntry = getEntry(virtualFrameNo);
        if (pageTableEntry.valid == 1) {
            throw new IllegalArgumentException(String.format("virtual frame %s allocated", virtualFrameNo));
        }
        writeEntry(virtualFrameNo, physicalFrameNo);
    }

    private void writeEntry(int virtualFrameNo, int physicalFrameNo) {
        int entryPos = pageTableBase + virtualFrameNo * (1 << PAGE_TABLE_ENTRY_DIGITS);
        physicalMemory.write(entryPos, (byte)physicalFrameNo);
        physicalMemory.write(entryPos + 1, (byte) 1);
    }

    public int entryCount() {
        return 1 << (digits - pageSizeDigits);
    }

    public void printTable() {
        for (int virtualFrameNo = 0; virtualFrameNo < entryCount(); virtualFrameNo++) {
            PageTableEntry pageTableEntry = getEntry(virtualFrameNo);
            System.out.println("VFN:" + virtualFrameNo + "| PFN:" + pageTableEntry.physicalFrameNo
                    + "| VALID:" + (pageTableEntry.valid == 1));
        }
    }

    public int read(int virtualFrameNo, int pos) throws IllegalAccessException {
        PageTableEntry pageTableEntry = getEntry(virtualFrameNo);
        int physicalFrameNo = pageTableEntry.physicalFrameNo;
        boolean valid = pageTableEntry.valid == 1;
        if (!valid) {
            throw new IllegalAccessException();
        }
        return physicalMemory.read(physicalFrameNo * (1 << pageSizeDigits) + pos);
    }

    public void write(int virtualFrameNo, int pos, byte value) throws IllegalAccessException {
        PageTableEntry pageTableEntry = getEntry(virtualFrameNo);
        int physicalFrameNo = pageTableEntry.physicalFrameNo;
        boolean valid = pageTableEntry.valid == 1;
        if (!valid) {
            throw new IllegalAccessException();
        }
        physicalMemory.write(physicalFrameNo * (1 << pageSizeDigits) + pos, value);
    }

    private PageTableEntry getEntry(int virtualFrameNo) {
        int entryPos = pageTableBase + virtualFrameNo * (1 << PAGE_TABLE_ENTRY_DIGITS);
        return new PageTableEntry(physicalMemory.read(entryPos),
                physicalMemory.read(entryPos + 1));
    }
}

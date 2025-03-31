package org.wangyang.wal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;

public class MiniDB {
    private enum OpType {
        SET, INC, DEC
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class WriteAheadLog {
        OpType opType;
        Integer key;
        Integer value;
    }
    private final List<WriteAheadLog> persistentList;
    private final Map<Integer, Integer> persistentStorage;
    private boolean somethingWrongHappens;
    private final Random random;

    public MiniDB() {
        persistentList = new ArrayList<>();
        persistentStorage = new HashMap<>();
        random = new Random();
    }

    public Integer get(int key) {
        if (somethingWrongHappens) {
            build();
            somethingWrongHappens = false;
        }
        return persistentStorage.get(key);
    }

    private void build() {
        persistentStorage.clear();
        for (WriteAheadLog wal : persistentList) {
            if (wal.opType == OpType.SET) {
                doSet(wal.key, wal.value);
            } else if (wal.opType == OpType.INC) {
                doIncrement(wal.key);
            } else if (wal.opType == OpType.DEC) {
                doDecrement(wal.key);
            }
        }
    }

    public void set(int key, int value) {
        persistentList.add(new WriteAheadLog(OpType.SET, key, value));
        somethingMayGoWrong(() -> doSet(key, value));
    }

    private void doSet(int key, int value) {
        persistentStorage.put(key, value);
    }

    public void increment(int key) {
        persistentList.add(new WriteAheadLog(OpType.INC, key, null));
        somethingMayGoWrong(() -> doIncrement(key));
    }

    private void doIncrement(int key) {
        if (!persistentStorage.containsKey(key)) {
            return;
        }
        persistentStorage.put(key, persistentStorage.get(key) + 1);
    }

    public void decrement(int key) {
        persistentList.add(new WriteAheadLog(OpType.DEC, key, null));
        somethingMayGoWrong(() -> doDecrement(key));
    }

    private void doDecrement(int key) {
        if (!persistentStorage.containsKey(key)) {
            return;
        }
        persistentStorage.put(key, persistentStorage.get(key) - 1);
    }

    public boolean somethingGoesWrong() {
        return somethingWrongHappens;
    }

    private void somethingMayGoWrong(Runnable task) {
        if (random.nextInt(10) >= 5) {
            task.run();
            return;
        }
        somethingWrongHappens = true;
    }
}

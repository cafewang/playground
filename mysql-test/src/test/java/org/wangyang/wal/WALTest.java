package org.wangyang.wal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WALTest {

    @Test
    public void data_is_consistent_even_write_goes_wrong() {
        MiniDB miniDB = new MiniDB();
        miniDB.set(1, 100);
        for (int i = 0; i < 50; i++) {
            miniDB.increment(1);
        }
        Assertions.assertTrue(miniDB.somethingGoesWrong());
        Assertions.assertEquals(150, miniDB.get(1));
        for (int i = 0; i < 100; i++) {
            miniDB.decrement(1);
        }
        Assertions.assertTrue(miniDB.somethingGoesWrong());
        Assertions.assertEquals(50, miniDB.get(1));
    }
}
package org.wangyang.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.wangyang.entity.MysqlLock;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootTest
@ActiveProfiles("test")
class UniqueKeyQueryTest {
    @Resource
    private MysqlService mysqlService;

    void canUpdateByNo(int no, String name) {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(100),
                () -> mysqlService.updateByNoThenRollback(no, name));
    }

    void canNotUpdateByNo(int no, String name) {
        Assertions.assertThrows(TimeoutException.class, () -> CompletableFuture.runAsync(() ->
                mysqlService.updateByNoThenRollback(no, name)).get(100, TimeUnit.MILLISECONDS));
    }

    void canNotInsert(int id, int no) {
        Assertions.assertThrows(TimeoutException.class, () -> CompletableFuture.runAsync(() ->
                mysqlService.insertThenRollback(id, no)).get(100, TimeUnit.MILLISECONDS));
    }

    void canInsert(int id, int no) {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(100),
                () -> mysqlService.insertThenRollback(id, no));
    }

    @Test
    @Transactional
    void query_uk_hit_only_lock_record() {
        mysqlService.selectForUpdateByNo(110);
        List<MysqlLock> mysqlLocks = mysqlService.allLocks();
        Assertions.assertTrue(mysqlLocks.stream()
                .anyMatch(item -> item.getLockMode().equals("X,REC_NOT_GAP")
                 && item.getIndexName().equals("PRIMARY")));
        Assertions.assertTrue(mysqlLocks.stream()
                .anyMatch(item -> item.getLockMode().equals("X,REC_NOT_GAP")
                        && item.getIndexName().equals("no_UNIQUE")));
        canInsert(9, 109);
        canNotUpdateByNo(110, "name");
        canInsert(12, 112);
    }

    @Test
    @Transactional
    void query_uk_miss_lock_gap_insert() {
        mysqlService.selectForUpdateByNo(112);
        canUpdateByNo(110, "name");
        canNotInsert(14, 114);
        canUpdateByNo(115, "name");
    }

    @Test
    @Transactional
    void range_query_uk_one_point() {
        mysqlService.leftCloseRightOpenByNo(110, 111);
        canUpdateByNo(105, "name");
        canNotInsert(9, 109);
        canNotUpdateByNo(110, "name");
        canNotInsert(12, 112);
        canNotUpdateByNo(115, "name");
        canInsert(16, 116);
    }


    @Test
    @Transactional
    void range_query_uk_wide_range() {
        mysqlService.leftCloseRightOpenByNo(105, 112);
        canNotInsert(1, 101);
        canNotUpdateByNo(105, "name");
        canNotInsert(8, 108);
        canNotUpdateByNo(110, "name");
        canNotInsert(14, 114);
        canNotUpdateByNo(115, "name");
        canInsert(16, 116);
    }

    @Test
    @Transactional
    void range_query_pk_upper_boundary() {
        mysqlService.leftCloseRightOpenByNo(115, 121);
        canUpdateByNo(110, "name");
        canNotInsert(14, 114);
        canNotUpdateByNo(115, "name");
        canNotInsert(17, 117);
        canNotUpdateByNo(120, "name");
        canNotInsert(30, 130);
    }
}
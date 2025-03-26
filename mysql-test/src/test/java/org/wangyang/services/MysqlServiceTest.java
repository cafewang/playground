package org.wangyang.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.wangyang.entity.Employee;
import org.wangyang.entity.MysqlLock;
import org.wangyang.repo.MysqlRepo;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootTest
@ActiveProfiles("test")
class MysqlServiceTest {
    @Resource
    private MysqlService mysqlService;
    @Resource
    private MysqlRepo mysqlRepo;

    Runnable rollbackTransaction(Runnable task) {
        return () -> runInRollbackTransaction(task);
    }

    void runInRollbackTransaction(Runnable task) {
        TestTransaction.start();
        task.run();
        TestTransaction.end();
    }

    void canUpdate(int id, int no) {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(50),
                () -> mysqlService.updateNoThenRollback(id, no));
    }

    void canNotUpdate(int id, int no) {
        Assertions.assertThrows(TimeoutException.class, () -> CompletableFuture.runAsync(() ->
                mysqlService.updateNoThenRollback(id, no)).get(50, TimeUnit.MILLISECONDS));
    }

    void canNotInsert(int id, int no) {
        Assertions.assertThrows(TimeoutException.class, () -> CompletableFuture.runAsync(() ->
                mysqlService.insertThenRollback(id, no)).get(50, TimeUnit.MILLISECONDS));
    }

    void canInsert(int id, int no) {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(50),
                () -> mysqlService.insertThenRollback(id, no));
    }

    @Test
    @Transactional
    void query_pk_hit_only_lock_record() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.selectForUpdate(10);
        List<MysqlLock> mysqlLocks = mysqlService.allLocks();
        Assertions.assertTrue(mysqlLocks.stream().map(MysqlLock::getLockMode)
                .anyMatch(mode -> mode.equals("X,REC_NOT_GAP")));
        canUpdate(5, 205);
        canUpdate(15, 215);
    }

    @Test
    @Transactional
    void query_pk_miss_lock_gap_insert() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.selectForUpdate(12);
        List<MysqlLock> mysqlLocks = mysqlService.allLocks();
        Assertions.assertTrue(mysqlLocks.stream().map(MysqlLock::getLockMode)
                .anyMatch(mode -> mode.equals("X,GAP")));
        canUpdate(15, 215);
        canUpdate(10, 210);
        canNotInsert(11, 211);
    }

    @Test
    @Transactional
    void range_query_pk_one_point() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftCloseRightOpen(10, 11);
        List<MysqlLock> mysqlLocks = mysqlService.allLocks();
        canInsert(9, 209);
        canNotUpdate(10, 210);
        canNotInsert(11, 211);
        canNotInsert(14, 214);
        canUpdate(15, 215);
    }

    @Test
    @Transactional
    void range_query_pk_between() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftCloseRightOpen(11, 15);
        List<MysqlLock> mysqlLocks = mysqlService.allLocks();
        canUpdate(10, 210);
        canNotInsert(11, 211);
        canNotInsert(14, 214);
        canUpdate(15, 215);
    }

    @Test
    @Transactional
    void range_query_pk_wide_range() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftCloseRightOpen(4, 12);
        canNotInsert(1, 201);
        canNotUpdate(5, 205);
        canNotInsert(6, 206);
        canNotUpdate(10, 210);
        canNotInsert(12, 212);
        canUpdate(15, 215);
    }

    @Test
    @Transactional
    void range_query_pk_right_close_one_point() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftOpenRightClose(9, 10);
        List<MysqlLock> mysqlLocks = mysqlService.allLocks();
        canUpdate(5, 205);
        canNotInsert(9, 209);
        canNotUpdate(10, 210);
        canInsert(11, 211);
    }

    @Test
    @Transactional
    void range_query_pk_right_close() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftOpenRightClose(10, 15);
        canUpdate(10, 210);
        canNotInsert(11, 211);
        canNotUpdate(15, 215);
        canInsert(16, 216);
    }

    @Test
    @Transactional
    void range_query_pk_right_close_wide() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftOpenRightClose(10, 17);
        canUpdate(10, 210);
        canNotInsert(11, 211);
        canNotUpdate(15, 215);
        canNotInsert(16, 216);
        canUpdate(20, 220);
    }

    @Test
    @Transactional
    void range_query_pk_upper_boundary() throws ExecutionException, InterruptedException, TimeoutException {
        List<Employee> employees = mysqlService.leftCloseRightOpen(16, 21);
        canUpdate(15, 215);
        canNotInsert(16, 216);
        canNotUpdate(20, 220);
        canNotInsert(21, 221);
    }
}
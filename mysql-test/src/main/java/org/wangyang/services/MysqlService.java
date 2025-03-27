package org.wangyang.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wangyang.entity.Employee;
import org.wangyang.entity.MysqlLock;
import org.wangyang.repo.MysqlRepo;
import org.wangyang.util.TransactionWrapper;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class MysqlService {
    @Resource
    private MysqlRepo mysqlRepo;
    @PersistenceContext
    private EntityManager entityManager;
    @Resource
    private TransactionWrapper transactionWrapper;

    @Transactional
    public List<Employee> selectForUpdate(int id) {
        return mysqlRepo.selectForUpdate(id);
    }

    public List<Employee> selectForUpdateByNo(int no) {
        return mysqlRepo.selectForUpdateByNo(no);
    }

    public List<Employee> leftCloseRightOpen(int min, int max) {
        return mysqlRepo.leftCloseRightOpen(min, max);
    }

    public List<Employee> leftCloseRightOpenByNo(int min, int max) {
        return mysqlRepo.selectForUpdateByNoLeftCloseRightOpen(min, max);
    }

    public List<Employee> leftOpenRightClose(int min, int max) {
        return mysqlRepo.leftOpenRightClose(min, max);
    }

    @Transactional
    public List<MysqlLock> allLocks() {
        return mysqlRepo.allLocks();
    }

    public void updateNoThenRollback(int id, int no) {
        transactionWrapper.rollbackInNewTransaction(() ->
                mysqlRepo.updateNo(id, no)).run();
    }

    public void insertThenRollback(int id, int no) {
        transactionWrapper.rollbackInNewTransaction(() ->
                mysqlRepo.saveAndFlush(new Employee(id, no))).run();
    }

    public void updateByNoThenRollback(int no, String name) {
        transactionWrapper.rollbackInNewTransaction(() ->
                mysqlRepo.updateByNo(no, name)).run();
    }
}

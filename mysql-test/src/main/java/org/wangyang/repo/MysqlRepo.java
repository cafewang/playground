package org.wangyang.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.wangyang.entity.Employee;
import org.wangyang.entity.MysqlLock;

import java.util.List;

@Repository
public interface MysqlRepo extends JpaRepository<Employee, Integer> {
    @Query(nativeQuery = true, value = "select * from user.employee where id=:id for update")
    List<Employee> selectForUpdate(@Param("id") int id);

    @Query(nativeQuery = true, value = "select INDEX_NAME as indexName, LOCK_MODE as lockMode, LOCK_TYPE as lockType, LOCK_DATA as lockData from performance_schema.data_locks")
    List<MysqlLock> allLocks();

    @Modifying
    @Query(value = "update Employee set no=:no where id=:id")
    void updateNo(@Param("id") int id, @Param("no") int no);

    @Query(nativeQuery = true, value = "select * from user.employee where id >= :min and id < :max for update")
    List<Employee> leftCloseRightOpen(@Param("min") int min, @Param("max")int max);

    @Query(nativeQuery = true, value = "select * from user.employee where no >= :min and no < :max for update")
    List<Employee> selectForUpdateByNoLeftCloseRightOpen(@Param("min") int min, @Param("max")int max);

    @Query(nativeQuery = true, value = "select * from user.employee where id > :min and id <= :max for update")
    List<Employee> leftOpenRightClose(@Param("min") int min, @Param("max")int max);

    @Query(nativeQuery = true, value = "select * from employee where no=:no for update")
    List<Employee> selectForUpdateByNo(@Param("no") int no);

    @Modifying
    @Query("update Employee set name=:name where no=:no")
    void updateByNo(@Param("no")int no, @Param("name")String name);
}

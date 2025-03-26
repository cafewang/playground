package org.wangyang.entity;

public interface MysqlLock {
    String getIndexName();
    String getLockMode();
    String getLockData();
    String getLockType();
}

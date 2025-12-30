package com.app.dao.Interface;

import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {

    void insert(T t) throws SQLException;

    void update(T t) throws SQLException;

    void delete(int id) throws SQLException;

    T findById(int id) throws SQLException;
    
    List<T> findAll() throws SQLException;
}
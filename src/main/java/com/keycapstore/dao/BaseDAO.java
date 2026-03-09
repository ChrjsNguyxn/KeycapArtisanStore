package com.keycapstore.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.keycapstore.config.ConnectDB;

public abstract class BaseDAO {

    protected Connection getConnection() {
        return ConnectDB.getConnection();
    }

    protected void closeQuietly(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            // Bo qua loi
        }
    }
}
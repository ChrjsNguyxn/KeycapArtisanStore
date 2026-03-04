package com.keycapstore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=JavaKADB;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USER = "sa";        // đổi nếu bạn dùng user khác
    private static final String PASSWORD = "123456"; // đổi đúng password SQL Server của bạn

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
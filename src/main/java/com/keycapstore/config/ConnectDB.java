package com.keycapstore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static final String SERVER_NAME = "127.0.0.1";
    private static final String PORT = "1433";
    private static final String DB_NAME = "JavaKADB";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    private static final boolean ENCRYPT = false;
    private static final boolean TRUST_CERT = true;

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;",
                    SERVER_NAME, PORT, DB_NAME, ENCRYPT, TRUST_CERT);

            conn = DriverManager.getConnection(url, USER, PASSWORD);
            System.out.println("Kết nối SQL Server thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Kết nối thất bại! Lỗi: " + e.getMessage());
        }
        return conn;
    }

    public static void main(String[] args) {
        getConnection();
    }

}
package com.keycapstore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    // Thong tin cau hinh DB
    private static final String SERVER_NAME = "localhost";
    private static final String PORT = "1433";
    private static final String DB_NAME = "JavaKADB";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    // Cau hinh SSL
    private static final boolean ENCRYPT = false;
    private static final boolean TRUST_CERT = true;

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Tạo chuỗi kết nối
            String url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;",
                    SERVER_NAME, PORT, DB_NAME, ENCRYPT, TRUST_CERT);

            // Kết nối
            conn = DriverManager.getConnection(url, USER, PASSWORD);
            System.out.println("Kết nối SQL Server thành công!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Kết nối thất bại! Hãy kiểm tra lại User/Pass hoặc tên Database.");
        }
        return conn;
    }

    // Chay thu ket noi
    public static void main(String[] args) {
        getConnection();
    }

}
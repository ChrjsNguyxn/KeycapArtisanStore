package com.keycapstore.dao;

import com.keycapstore.model.SupplierDTO;
import com.keycapstore.config.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    // =========================
    // Lấy toàn bộ supplier
    // =========================
    public List<SupplierDTO> getAll() {

        List<SupplierDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SupplierDTO s = new SupplierDTO();
                s.setSupplierId(rs.getInt("supplier_id"));
                s.setName(rs.getString("name"));
                s.setPhone(rs.getString("phone"));
                s.setAddress(rs.getString("address"));
                s.setEmail(rs.getString("email"));
                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // Thêm supplier
    // =========================
    public boolean insert(SupplierDTO s) {

        String sql = "INSERT INTO suppliers (name, phone, address, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getEmail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================
    // Cập nhật supplier
    // =========================
    public boolean update(SupplierDTO s) {

        String sql = "UPDATE suppliers SET name=?, phone=?, address=?, email=? WHERE supplier_id=?";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getEmail());
            ps.setInt(5, s.getSupplierId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================
    // Xóa supplier
    // =========================
    public boolean delete(int id) {

        String sql = "DELETE FROM suppliers WHERE supplier_id=?";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================
    // Tìm supplier theo ID
    // =========================
    public SupplierDTO findById(int id) {

        String sql = "SELECT * FROM suppliers WHERE supplier_id=?";
        SupplierDTO s = null;

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                s = new SupplierDTO();
                s.setSupplierId(rs.getInt("supplier_id"));
                s.setName(rs.getString("name"));
                s.setPhone(rs.getString("phone"));
                s.setAddress(rs.getString("address"));
                s.setEmail(rs.getString("email"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    public SupplierDTO getById(int id) {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SupplierDTO s = new SupplierDTO();
                s.setSupplierId(rs.getInt("supplier_id"));
                s.setName(rs.getString("name"));
                return s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SupplierDTO findByName(String name) {
        String sql = "SELECT * FROM suppliers WHERE name = ?";
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SupplierDTO s = new SupplierDTO();
                s.setSupplierId(rs.getInt("supplier_id"));
                s.setName(rs.getString("name"));
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
package com.keycapstore.model;

import java.time.LocalDateTime;

public class SystemLog {
    private int logId;
    private int employeeId;
    private String action;
    private String targetTable;
    private int recordId;
    private String description;
    private LocalDateTime logDate;

    public SystemLog() {
    }

    public SystemLog(int employeeId, String action, String targetTable, int recordId, String description) {
        this.employeeId = employeeId;
        this.action = action;
        this.targetTable = targetTable;
        this.recordId = recordId;
        this.description = description;
        this.logDate = LocalDateTime.now();
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }
}
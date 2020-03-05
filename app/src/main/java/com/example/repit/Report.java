package com.example.repit;


public class Report {

    private String reportType;
    private String description;
    private String seriousness;
    private String date;
    private String time;
    private String location;
    private String reportPicture;
    private String reportedBy;
    private String reportedByUserID;
    private String reportStatus;

    public Report(){}

    public Report(String reportType, String description, String seriousness, String date, String time, String location, String reportPicture, String reportedBy, String reportedByUserID, String reportStatus) {
        this.reportType = reportType;
        this.description = description;
        this.seriousness = seriousness;
        this.date = date;
        this.time = time;
        this.location = location;
        this.reportPicture = reportPicture;
        this.reportedBy = reportedBy;
        this.reportedByUserID = reportedByUserID;
        this.reportStatus = reportStatus;
    }

    public String getReportType() {
        return reportType;
    }

    public String getDescription() {
        return description;
    }

    public String getSeriousness() {
        return seriousness;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getReportPicture() {
        return reportPicture;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public String getReportedByUserID() {
        return reportedByUserID;
    }

    public String getReportStatus() {
        return reportStatus;
    }
}




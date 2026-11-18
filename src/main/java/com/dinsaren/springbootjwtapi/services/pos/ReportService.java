package com.dinsaren.springbootjwtapi.services.pos;

import java.util.Map;

public interface ReportService {
    Map<String, Object> getReports(String startDateStr, String endDateStr);
    Map<String, Object> getExpiryReport(String search, String type, String status);
}

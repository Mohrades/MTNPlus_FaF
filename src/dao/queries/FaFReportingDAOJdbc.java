package dao.queries;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;
import dao.mapping.FaFReportingRowMapper;
import domain.models.FaFReporting;

public class FaFReportingDAOJdbc {

	private DAO dao;

	public FaFReportingDAOJdbc(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public void saveOneFaFReporting(FaFReporting reporting) {
		if(reporting.getChargingAmount() == 0) {
			getJdbcTemplate().update("INSERT INTO MTN_PLUS_FAF_REPORT_EBA (SUBSCRIBER,FAF_NUMBER,FLAG,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID) VALUES(" + reporting.getSubscriber() + ",'" + reporting.getFafNumber() + "'," + (reporting.isFlag() ? 1 : 0) + ",TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(reporting.getCreated_date_time()) + "','" + reporting.getOriginOperatorID().replace("'", "''") + "')");
		}
		else {
			getJdbcTemplate().update("INSERT INTO MTN_PLUS_FAF_REPORT_EBA (SUBSCRIBER,FAF_NUMBER,FLAG,CHARGING_AMOUNT,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID) VALUES(" + reporting.getSubscriber() + ",'" + reporting.getFafNumber() + "'," + (reporting.isFlag() ? 1 : 0) + "," + reporting.getChargingAmount() + ",TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(reporting.getCreated_date_time()) + "','" + reporting.getOriginOperatorID().replace("'", "''") + "')");
		}
	}

	public FaFReporting getLastFaFChangeRequestReporting(int subscriber, String fafNumber) {
		List<FaFReporting> reporting = getJdbcTemplate().query("SELECT ID,SUBSCRIBER,FAF_NUMBER,FLAG,CHARGING_AMOUNT,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID FROM MTN_PLUS_FAF_REPORT_EBA WHERE ((SUBSCRIBER = " + subscriber + ") AND (FAF_NUMBER = '" + fafNumber + "')) ORDER BY CREATED_DATE_TIME DESC", new FaFReportingRowMapper());
		return reporting.isEmpty() ? null : reporting.get(0);
	}

	public List<FaFReporting> getFaFReporting(int subscriber, String fafNumber) {
		return getJdbcTemplate().query("SELECT ID,SUBSCRIBER,FAF_NUMBER,FLAG,CHARGING_AMOUNT,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID FROM MTN_PLUS_FAF_REPORT_EBA WHERE ((SUBSCRIBER = " + subscriber + ") AND (FAF_NUMBER = '" + fafNumber + "')) ORDER BY CREATED_DATE_TIME DESC", new FaFReportingRowMapper());
	}

	public List<FaFReporting> getFaFReporting(int subscriber) {
		return getJdbcTemplate().query("SELECT ID,SUBSCRIBER,FAF_NUMBER,FLAG,CHARGING_AMOUNT,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID FROM MTN_PLUS_FAF_REPORT_EBA WHERE (SUBSCRIBER = " + subscriber + ") ORDER BY CREATED_DATE_TIME DESC", new FaFReportingRowMapper());
	}

}

package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;
import dao.mapping.SubscriptionReportingRowMapper;
import domain.models.SubscriptionReporting;

public class JdbcSubscriptionReportingDao {

	private DAO dao;

	public JdbcSubscriptionReportingDao(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public void saveOneSubscriptionReporting(SubscriptionReporting reporting) {
		Date now = new Date();

		if(reporting.getChargingAmount() == 0) {
			getJdbcTemplate().update("INSERT INTO MTN_PLUS_SUBSCRIPTION_REPORT_E (SUBSCRIBER,FLAG,CREATED_DATE_TIME,CREATED_DATE_TIME_INDEX,ORIGIN_OPERATOR_ID) VALUES(" + reporting.getSubscriber() + "," + (reporting.isFlag() ? 1 :0) + ",TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(now) + "'," + Integer.parseInt((new SimpleDateFormat("yyyyMMdd")).format(now)) + ",'" + reporting.getOriginOperatorID().replace("'", "''") + "')");
		}
		else if(reporting.getChargingAmount() > 0) {
			getJdbcTemplate().update("INSERT INTO MTN_PLUS_SUBSCRIPTION_REPORT_E (SUBSCRIBER,FLAG,CHARGING_AMOUNT,CREATED_DATE_TIME,CREATED_DATE_TIME_INDEX,ORIGIN_OPERATOR_ID) VALUES(" + reporting.getSubscriber() + "," + (reporting.isFlag() ? 1 : 0) + "," + reporting.getChargingAmount() + ",TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(now) + "'," + Integer.parseInt((new SimpleDateFormat("yyyyMMdd")).format(now)) + ",'" + reporting.getOriginOperatorID().replace("'", "''") + "')");
		}
	}

	public List<SubscriptionReporting> getSubscriptionReporting(int subscriber) {
		return getJdbcTemplate().query("SELECT ID,SUBSCRIBER,FLAG,CHARGING_AMOUNT,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID FROM MTN_PLUS_SUBSCRIPTION_REPORT_E WHERE (SUBSCRIBER = " + subscriber + ") ORDER BY CREATED_DATE_TIME DESC", new SubscriptionReportingRowMapper());
	}

}

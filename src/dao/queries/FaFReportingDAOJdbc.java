package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
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
		getJdbcTemplate().update("INSERT INTO MTN_PLUS_FAF_REPORT_EBA (SUBSCRIBER,FLAG,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID) VALUES(" + reporting.getSubscriber() + "," + (reporting.isFlag() ? 1 : 0) + ",TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "','" + reporting.getOriginOperatorID().replace("'", "''") + "')");
	}

	public List<FaFReporting> getFaFReporting(int subscriber) {
		return getJdbcTemplate().query("SELECT ID,SUBSCRIBER,FLAG,CREATED_DATE_TIME,ORIGIN_OPERATOR_ID FROM MTN_PLUS_FAF_REPORT_EBA WHERE SUBSCRIBER = " + subscriber, new FaFReportingRowMapper());
	}

}

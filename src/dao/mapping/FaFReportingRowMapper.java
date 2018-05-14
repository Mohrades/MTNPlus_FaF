package dao.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

import domain.models.FaFReporting;

public class FaFReportingRowMapper implements RowMapper<FaFReporting> {

	@Override
	public FaFReporting mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub

		return new FaFReporting(rs.getInt("ID"), rs.getInt("SUBSCRIBER"), ((rs.getInt("FLAG") == 1) ? true : false), rs.getTimestamp("CREATED_DATE_TIME"), rs.getString("ORIGIN_OPERATOR_ID"));
	}

}
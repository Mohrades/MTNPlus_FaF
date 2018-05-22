package dao.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

import domain.models.Subscriber;

public class SubscriberRowMapper implements RowMapper<Subscriber> {

	@Override
	public Subscriber mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub

		return new Subscriber(rs.getInt("ID"), rs.getString("MSISDN"), ((rs.getInt("FLAG") == 1) ? true : false), ((rs.getInt("FAF_CR_CHARGING_ENABLED") == 1) ? true : false), rs.getTimestamp("LAST_UPDATE_TIME"), ((rs.getInt("LOCKED") == 1) ? true : false));
	}

}
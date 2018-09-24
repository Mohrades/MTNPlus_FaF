package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;
import dao.mapping.SubscriberRowMapper;
import domain.models.Subscriber;

public class JdbcSubscriberDao {

	private DAO dao;

	public JdbcSubscriberDao(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public int saveOneSubscriber(Subscriber subscriber) {
		try {
			if(subscriber.getId() == 0) {
				getJdbcTemplate().update("INSERT INTO MTN_PLUS_MSISDN_EBA (MSISDN,FLAG,FAF_CR_CHARGING_ENABLED,LOCKED) VALUES('" + subscriber.getValue() + "'," + (subscriber.isFlag() ? 1 : 0) + "," + (subscriber.isFafChangeRequestChargingEnabled() ? 1 : 0) + "," + (subscriber.isLocked() ? 1 : 0) + ")");
				return 1;
			}
			else if(subscriber.getId() < 0) {
				return getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET FLAG = " + (subscriber.isFlag() ? 1 : 0) + " WHERE ((ID = " + (-subscriber.getId()) + ") AND (FLAG = " + (subscriber.isFlag() ? 0 : 1) + ") AND (LOCKED = 0))");
			}
			else if(subscriber.getId() > 0) {
				return getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET FLAG = " + (subscriber.isFlag() ? 1 : 0) + ", LOCKED = 1 WHERE ((ID = " + subscriber.getId() + ") AND (FLAG = " + (subscriber.isFlag() ? 0 : 1) + ") AND (LOCKED = 0))");
			}

		} catch(EmptyResultDataAccessException emptyEx) {
			return -1;

		} catch(Throwable th) {
			return -1;
		}

		return 0;
	}

	@SuppressWarnings("deprecation")
	public void releasePricePlanCurrentStatusAndLock(Subscriber subscriber, boolean rollback, int days) {
		try {
			if(rollback) {
				if(subscriber.getId() > 0) getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET FLAG = (CASE FLAG WHEN 1 THEN 0 ELSE 1 END), LOCKED = 0 WHERE ((ID = " + subscriber.getId() + ") AND (LOCKED = 1))");
				else getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET FLAG = (CASE FLAG WHEN 1 THEN 0 ELSE 1 END), LOCKED = 0 WHERE ((MSISDN = '" + subscriber.getValue() + "') AND (LOCKED = 1))");
			}
			else {
				Date now = new Date();
				Date next_month = (Date) now.clone();
				next_month.setDate(now.getDate() + days);

				if(subscriber.getId() > 0) getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET LAST_UPDATE_TIME = TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(now) + "', LOCKED = 0 WHERE ((ID = " + subscriber.getId() + ") AND (LOCKED = 1))");
				else getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET LAST_UPDATE_TIME = TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(now) + "', LOCKED = 0 WHERE ((MSISDN = '" + subscriber.getValue() + "') AND (LOCKED = 1))");
			}

		} catch(Throwable th) {

		}
	}
	
	public int lock(Subscriber subscriber) {
		try {
			if(subscriber.getId() > 0) {
				return getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET LOCKED = 1 WHERE ((ID = " + subscriber.getId() + ") AND (LOCKED = 0))");
			}

		} catch(EmptyResultDataAccessException emptyEx) {
			return -1;

		} catch(Throwable th) {
			return -1;
		}

		return 0;
	}

	
	public void unLock(Subscriber subscriber) {
		getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET LOCKED = 0 WHERE ((ID = " + subscriber.getId() + ") AND (LOCKED = 1))");
	}

	public int saveFafChargingStatus(Subscriber subscriber) {
		try {
			if(subscriber.getId() > 0) {
				// return getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET FAF_CHARGING_ENABLED = " + (subscriber.isFafChargingEnabled() ? 1 : 0) + " WHERE ((ID = " + subscriber.getId() + ") AND (FLAG = 1) AND (FAF_CR_CHARGING_ENABLED = 0))");
				return getJdbcTemplate().update("UPDATE MTN_PLUS_MSISDN_EBA SET FAF_CR_CHARGING_ENABLED = " + (subscriber.isFafChangeRequestChargingEnabled() ? 1 : 0) + " WHERE ((ID = " + subscriber.getId() + ") AND (FAF_CR_CHARGING_ENABLED = 0))");
			}

		} catch(EmptyResultDataAccessException emptyEx) {
			return -1;

		} catch(Throwable th) {
			return -1;
		}

		return 0;
	}

	public Subscriber getOneSubscriber(int id, boolean locked) {
		List<Subscriber> subscribers = getJdbcTemplate().query("SELECT ID,MSISDN,FLAG,LAST_UPDATE_TIME,FAF_CR_CHARGING_ENABLED,LOCKED FROM MTN_PLUS_MSISDN_EBA WHERE ((ID = " + id + ") AND (LOCKED = " + (locked ? 1 : 0) + "))", new SubscriberRowMapper());
		return subscribers.isEmpty() ? null : subscribers.get(0);
	}

	public Subscriber getOneSubscriber(int id) {
		List<Subscriber> subscribers = getJdbcTemplate().query("SELECT ID,MSISDN,FLAG,LAST_UPDATE_TIME,FAF_CR_CHARGING_ENABLED,LOCKED FROM MTN_PLUS_MSISDN_EBA WHERE ID = " + id, new SubscriberRowMapper());
		return subscribers.isEmpty() ? null : subscribers.get(0);
	}

	public Subscriber getOneSubscriber(String msisdn, boolean locked) {
		List<Subscriber> subscribers = getJdbcTemplate().query("SELECT ID,MSISDN,FLAG,LAST_UPDATE_TIME,FAF_CR_CHARGING_ENABLED,LOCKED FROM MTN_PLUS_MSISDN_EBA WHERE ((MSISDN = '" + msisdn + "') AND (LOCKED = " + (locked ? 1 : 0) + "))", new SubscriberRowMapper());
		return subscribers.isEmpty() ? null : subscribers.get(0);
	}

	public Subscriber getOneSubscriber(String msisdn) {
		List<Subscriber> subscribers = getJdbcTemplate().query("SELECT ID,MSISDN,FLAG,LAST_UPDATE_TIME,FAF_CR_CHARGING_ENABLED,LOCKED FROM MTN_PLUS_MSISDN_EBA WHERE (MSISDN = '" + msisdn + "')", new SubscriberRowMapper());
		return subscribers.isEmpty() ? null : subscribers.get(0);
	}

	public List<Subscriber> getAllSubscribers(boolean locked) {
		return  getJdbcTemplate().query("SELECT ID,MSISDN,FLAG,LAST_UPDATE_TIME,FAF_CR_CHARGING_ENABLED,LOCKED FROM MTN_PLUS_MSISDN_EBA WHERE LOCKED = " + (locked ? 1 : 0), new SubscriberRowMapper());
	}

	public List<Subscriber> getAllSubscribers() {
		return  getJdbcTemplate().query("SELECT ID,MSISDN,FLAG,LAST_UPDATE_TIME,FAF_CR_CHARGING_ENABLED,LOCKED FROM MTN_PLUS_MSISDN_EBA", new SubscriberRowMapper());
	}

	public void deleteOneSubscriber(int id) {
		getJdbcTemplate().update("DELETE FROM MTN_PLUS_MSISDN_EBA WHERE ((ID = " + id + ") AND (LOCKED = 0))");
	}

	public void deleteOneSubscriber(String msisdn) {
		getJdbcTemplate().update("DELETE FROM MTN_PLUS_MSISDN_EBA WHERE ((MSISDN = '" + msisdn + "') AND (LOCKED = 0))");
	}
}

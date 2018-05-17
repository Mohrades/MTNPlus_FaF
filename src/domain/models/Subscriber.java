package domain.models;

import java.util.Date;

public class Subscriber extends MSISDN {

	private Date last_update_time;
	private boolean flag, fafChargingEnabled, locked;

	public Subscriber() {
		super();
	}

	public Subscriber(int id, String msisdn, boolean flag, boolean fafChargingEnabled, Date last_update_time, boolean locked) {
		super(id, msisdn);
		this.flag = flag;
		this.fafChargingEnabled = fafChargingEnabled;
		this.last_update_time = last_update_time;
		this.locked = locked;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public boolean isFafChargingEnabled() {
		return fafChargingEnabled;
	}

	public void setFafChargingEnabled(boolean fafChargingEnabled) {
		this.fafChargingEnabled = fafChargingEnabled;
	}

	public Date getLast_update_time() {
		return last_update_time;
	}

	public void setLast_update_time(Date last_update_time) {
		this.last_update_time = last_update_time;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public int hashCode() {
		return getValue().hashCode();
	}

	public boolean equals (Object pp) {
		try {
			Subscriber p = (Subscriber) pp;

			return this.getValue().equals(p.getValue());

		} catch(Throwable th) {

		}

		return false;
	}

}

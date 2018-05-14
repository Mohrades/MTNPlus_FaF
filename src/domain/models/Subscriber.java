package domain.models;

import java.util.Date;

public class Subscriber extends MSISDN {

	private Date last_update_time, fafChangeUnbarDate;
	private boolean flag, locked;

	public Subscriber() {
		super();
	}

	public Subscriber(int id, String msisdn, boolean flag, Date last_update_time, Date fafChangeUnbarDate, boolean locked) {
		super(id, msisdn);
		this.flag = flag;
		this.last_update_time = last_update_time;
		this.fafChangeUnbarDate = fafChangeUnbarDate;
		this.locked = locked;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public Date getLast_update_time() {
		return last_update_time;
	}

	public void setLast_update_time(Date last_update_time) {
		this.last_update_time = last_update_time;
	}

	public Date getFafChangeUnbarDate() {
		return fafChangeUnbarDate;
	}

	public void setFafChangeUnbarDate(Date fafChangeUnbarDate) {
		this.fafChangeUnbarDate = fafChangeUnbarDate;
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

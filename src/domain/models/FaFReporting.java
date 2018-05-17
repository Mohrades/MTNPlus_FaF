package domain.models;

import java.util.Date;

public class FaFReporting implements Comparable<FaFReporting> {

	private int id, subscriber;
	private boolean flag;
	private long chargingAmount;
	private Date created_date_time;
	private String fafNumber, originOperatorID;

	public FaFReporting() {

	}

	public FaFReporting(int id, int subscriber, String fafNumber, boolean flag, long chargingAmount, Date created_date_time, String originOperatorID) {
		this.id = id;
		this.subscriber = subscriber;
		this.fafNumber = fafNumber;
		this.flag = flag;
		this.chargingAmount = chargingAmount;
		this.created_date_time = created_date_time;
		this.originOperatorID = originOperatorID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(int subscriber) {
		this.subscriber = subscriber;
	}

	public String getFafNumber() {
		return fafNumber;
	}

	public void setFafNumber(String fafNumber) {
		this.fafNumber = fafNumber;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public long getChargingAmount() {
		return chargingAmount;
	}

	public void setChargingAmount(long chargingAmount) {
		this.chargingAmount = chargingAmount;
	}

	public Date getCreated_date_time() {
		return created_date_time;
	}

	public void setCreated_date_time(Date created_date_time) {
		this.created_date_time = created_date_time;
	}

	public String getOriginOperatorID() {
		return originOperatorID;
	}

	public void setOriginOperatorID(String originOperatorID) {
		this.originOperatorID = originOperatorID;
	}

	public int hashCode() {
		return subscriber;
	}

	public boolean equals (Object pp) {
		try {
			FaFReporting p = (FaFReporting) pp;

			return this.id == p.getId();

		} catch(Throwable th) {

		}

		return false;
	}

	@Override
	public int compareTo(FaFReporting pp) {
		// TODO Auto-generated method stub
		
		FaFReporting p = (FaFReporting) pp;
		
		if(this.created_date_time.before(p.getCreated_date_time())) return -1;
		else if(this.created_date_time.after(p.getCreated_date_time())) return 1;
		else return 0;
		// return this.created_date_time.compareTo(p.getCreated_date_time());
	}

}

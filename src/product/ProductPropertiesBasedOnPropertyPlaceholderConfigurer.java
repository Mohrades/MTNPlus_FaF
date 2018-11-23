package product;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

@Component(value="productProperties")
public class ProductPropertiesBasedOnPropertyPlaceholderConfigurer implements ProductProperties {

	@Value("${gsm.mcc}")
	private short mcc;

	@Value("${gsm.name}")
	private String gsm_name;

	@Value("${gsm.short_code}")
	private short sc;

	@Value("${gsm.short_code.secondary}")
	private short sc_secondary;

	@Value("${sms.notifications.header}")
	private String sms_notifications_header;

	private List<String> mnc;

	@Value("${msisdn.length}")
	private byte msisdn_length;
	
	@Value("${chargingDA}")
	private int chargingDA;

	@Value("${activation.chargingAmount}")
	private long activation_chargingAmount;

	@Value("${default.price.plan}")
	private String default_price_plan;

	@Value("${default.price.plan.deactivated}")
	private boolean default_price_plan_deactivated;

	@Value("${default.price.plan.url}")
	private String default_price_plan_url;

	@Value("${advantages.always}")
	private boolean advantages_always;

	@Value("${advantages.sms.da}")
	private int advantages_sms_da;

	@Value("${advantages.sms.value}")
	private long advantages_sms_value;

	@Value("${advantages.data.da}")
	private int advantages_data_da;

	@Value("${advantages.data.value}")
	private long advantages_data_value;

	@Value("${deactivation.freeCharging.days}")
	private short deactivation_freeCharging_days;

	@Value("${fafChangeRequestAllowedDays}")
	private short fafChangeRequestAllowedDays;

	@Value("${deactivation.chargingAmount}")
	private long deactivation_chargingAmount;

	@Value("${faf.requestedOwner}")
	private byte fafRequestedOwner;

	@Value("${fafIndicator}")
	private int fafIndicator;

	@Value("${fafMaxAllowedNumbers}")
	private short fafMaxAllowedNumbers;

	@Value("${fafMaxAllowedOffNetNumbers}")
	private short fafMaxAllowedOffNetNumbers;

	private List<String> xtra_serviceOfferings_IDs;
	private List<String> xtra_serviceOfferings_activeFlags;
	private List<String> xtra_removal_offer_IDs;

	private List<String> serviceOfferings_IDs;
	private List<String> serviceOfferings_activeFlags;

	@Value("${community.id}")
	private int community_id;
	
	@Value("${offer.id}")
	private int offer_id;

	@Value("${faf.chargingAmount}")
	private long faf_chargingAmount;

	private List<String> Anumber_serviceClass_include_filter;
	private List<String> Anumber_db_include_filter;
	private List<String> Anumber_serviceClass_exclude_filter;
	private List<String> Anumber_db_exclude_filter;

	private List<String> Bnumber_serviceClass_include_filter;
	private List<String> Bnumber_db_include_filter;
	private List<String> Bnumber_serviceClass_exclude_filter;
	private List<String> Bnumber_db_exclude_filter;

	/*private List<String> originOperatorIDs_list;*/

	private List<String> air_hosts;
	@Value("${air.io.sleep}")
	private int air_io_sleep;
	@Value("${air.io.timeout}")
	private int air_io_timeout;
	@Value("${air.io.threshold}")
	private int air_io_threshold;
	@Value("${air.test.connection.msisdn}")
	private String air_test_connection_msisdn;
	@Value("${air.preferred.host}")
	private byte air_preferred_host;

	@Value("${bonus.reset.required}")
	private boolean bonus_reset_required;
	@Value("${billed.sms.counter.accumulator}")
	private int billed_sms_counter_accumulator;
	@Value("${billed.sms.amount.accumulator}")
	private int billed_sms_amount_accumulator;
	@Value("${billed.services.amount.usageCounterID}")
	public int billed_services_amount_usageCounterID;
	@Value("${billed.services.amount.usageCounterID.isMonetary}")
	private boolean billed_services_amount_usageCounterID_isMonetary;
	@Value("${bonus.sms.onNet.da}")
	private int bonus_sms_onNet_da;
	@Value("${bonus.sms.onNet.fees}")
	private long bonus_sms_onNet_fees;
	@Value("${bonus.sms.offNet.da}")
	private int bonus_sms_offNet_da;
	@Value("${bonus.sms.offNet.fees}")
	private long bonus_sms_offNet_fees;
	@Value("${bonus.sms.threshold}")
	private int bonus_sms_threshold;

	@Value("${gsm.mnc}")
	public void setMnc(final String gsmmnc) {
		if(isSet(gsmmnc)) {
			mnc = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(gsmmnc);
		}
	 }

	@Value("${air.hosts}")
	public void setAir_hosts(final String air_hosts) {
		if(isSet(air_hosts)) {
			this.air_hosts = Splitter.onPattern("[;]").trimResults().omitEmptyStrings().splitToList(air_hosts);
		}
	}

	@Value("${xtra.serviceOfferings.IDs}")
	public void setXtra_serviceOfferings_IDs(final String xtra_serviceOfferings_IDs) {
		if(isSet(xtra_serviceOfferings_IDs)) {
			this.xtra_serviceOfferings_IDs = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(xtra_serviceOfferings_IDs);
		}
	}

	@Value("${xtra.serviceOfferings.activeFlags}")
	public void setXtra_serviceOfferings_activeFlags(final String xtra_serviceOfferings_activeFlags) {
		if(isSet(xtra_serviceOfferings_activeFlags)) {
			this.xtra_serviceOfferings_activeFlags = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(xtra_serviceOfferings_activeFlags);
		}
	}

	@Value("${serviceOfferings.IDs}")
	public void setServiceOfferings_IDs(final String serviceOfferings_IDs) {
		if(isSet(serviceOfferings_IDs)) {
			this.serviceOfferings_IDs = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(serviceOfferings_IDs);
		}
	}

	@Value("${serviceOfferings.activeFlags}")
	public void setServiceOfferings_activeFlags(final String serviceOfferings_activeFlags) {
		if(isSet(serviceOfferings_activeFlags)) {
			this.serviceOfferings_activeFlags = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(serviceOfferings_activeFlags);
		}
	}

	@Value("${xtra.removal.offer.IDs}")
	public void setXtra_removal_offer_IDs(final String xtra_removal_offer_IDs) {
		if(isSet(xtra_removal_offer_IDs)) {
			this.xtra_removal_offer_IDs = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(xtra_removal_offer_IDs);
		}
	}

	@Value("${Anumber.serviceClass.include_filter}")
	public void setAnumber_serviceClass_include_filter(final String anumber_serviceClass_include_filter) {
		if(isSet(anumber_serviceClass_include_filter)) {
			Anumber_serviceClass_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_serviceClass_include_filter);
		}
	}

	@Value("${Anumber.db.include_filter}")
	public void setAnumber_db_include_filter(final String anumber_db_include_filter) {
		if(isSet(anumber_db_include_filter)) {
			Anumber_db_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_db_include_filter);
		}
	}

	@Value("${Anumber.serviceClass.exclude_filter}")
	public void setAnumber_serviceClass_exclude_filter(final String anumber_serviceClass_exclude_filter) {
		if(isSet(anumber_serviceClass_exclude_filter)) {
			Anumber_serviceClass_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_serviceClass_exclude_filter);
		}
	}

	@Value("${Anumber.db.exclude_filter}")
	public void setAnumber_db_exclude_filter(final String anumber_db_exclude_filter) {
		if(isSet(anumber_db_exclude_filter)) {
			Anumber_db_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_db_exclude_filter);
		}
	}

	@Value("${Bnumber.serviceClass.include_filter}")
	public void setBnumber_serviceClass_include_filter(final String bnumber_serviceClass_include_filter) {
		if(isSet(bnumber_serviceClass_include_filter)) {
			Bnumber_serviceClass_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_serviceClass_include_filter);
		}
	}

	@Value("${Bnumber.db.include_filter}")
	public void setBnumber_db_include_filter(final String bnumber_db_include_filter) {
		if(isSet(bnumber_db_include_filter)) {
			Bnumber_db_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_db_include_filter);
		}
	}

	@Value("${Bnumber.serviceClass.exclude_filter}")
	public void setBnumber_serviceClass_exclude_filter(final String bnumber_serviceClass_exclude_filter) {
		if(isSet(bnumber_serviceClass_exclude_filter)) {
			Bnumber_serviceClass_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_serviceClass_exclude_filter);
		}
	}

	@Value("${Bnumber.db.exclude_filter}")
	public void setBnumber_db_exclude_filter(final String bnumber_db_exclude_filter) {
		if(isSet(bnumber_db_exclude_filter)) {
			Bnumber_db_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_db_exclude_filter);
		}
	}

	/*@Value("${originOperatorIDs.list}")
	public void setOriginOperatorIDs_list(final String originOperatorIDs_list) {
		if(isSet(originOperatorIDs_list)) {
			this.originOperatorIDs_list = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(originOperatorIDs_list);
		}
	}*/

	public short getMcc() {
		return mcc;
	}

	public String getGsm_name() {
		return gsm_name;
	}

	public short getSc() {
		return sc;
	}

	public short getSc_secondary() {
		return sc_secondary;
	}

	public String getSms_notifications_header() {
		return sms_notifications_header;
	}

	public List<String> getMnc() {
		return mnc;
	}

	public byte getMsisdn_length() {
		return msisdn_length;
	}

	public boolean isBonus_reset_required() {
		return bonus_reset_required;
	}

	public int getBilled_sms_counter_accumulator() {
		return billed_sms_counter_accumulator;
	}

	public int getBilled_sms_amount_accumulator() {
		return billed_sms_amount_accumulator;
	}

	public int getBilled_services_amount_usageCounterID() {
		return billed_services_amount_usageCounterID;
	}

	public boolean isBilled_services_amount_usageCounterID_Monetary() {
		return billed_services_amount_usageCounterID_isMonetary;
	}

	public int getBonus_sms_onNet_da() {
		return bonus_sms_onNet_da;
	}

	public long getBonus_sms_onNet_fees() {
		return bonus_sms_onNet_fees;
	}

	public int getBonus_sms_offNet_da() {
		return bonus_sms_offNet_da;
	}

	public long getBonus_sms_offNet_fees() {
		return bonus_sms_offNet_fees;
	}

	public int getBonus_sms_threshold() {
		return bonus_sms_threshold;
	}

	public List<String> getXtra_serviceOfferings_IDs() {
		return xtra_serviceOfferings_IDs;
	}

	public List<String> getXtra_serviceOfferings_activeFlags() {
		return xtra_serviceOfferings_activeFlags;
	}

	public String getDefault_price_plan() {
		return default_price_plan;
	}

	public boolean isDefault_price_plan_deactivated() {
		return default_price_plan_deactivated;
	}

	public String getDefault_price_plan_url() {
		return default_price_plan_url;
	}

	public int getFafIndicator() {
		return fafIndicator;
	}

	public byte getFafRequestedOwner() {
		return fafRequestedOwner;
	}

	public short getFafMaxAllowedNumbers() {
		return fafMaxAllowedNumbers;
	}

	public short getFafMaxAllowedOffNetNumbers() {
		return fafMaxAllowedOffNetNumbers;
	}

	public boolean isAdvantages_always() {
		return advantages_always;
	}

	public int getAdvantages_sms_da() {
		return advantages_sms_da;
	}

	public long getAdvantages_sms_value() {
		return advantages_sms_value;
	}

	public int getAdvantages_data_da() {
		return advantages_data_da;
	}

	public long getAdvantages_data_value() {
		return advantages_data_value;
	}

	public int getChargingDA() {
		return chargingDA;
	}

	public long getActivation_chargingAmount() {
		return activation_chargingAmount;
	}

	public short getDeactivation_freeCharging_days() {
		return deactivation_freeCharging_days;
	}

	public short getFafChangeRequestAllowedDays() {
		return fafChangeRequestAllowedDays;
	}

	public long getFaf_chargingAmount() {
		return faf_chargingAmount;
	}

	public long getDeactivation_chargingAmount() {
		return deactivation_chargingAmount;
	}

	public List<String> getServiceOfferings_IDs() {
		return serviceOfferings_IDs;
	}

	public List<String> getServiceOfferings_activeFlags() {
		return serviceOfferings_activeFlags;
	}

	public int getCommunity_id() {
		return community_id;
	}

	public int getOffer_id() {
		return offer_id;
	}

	public List<String> getXtra_removal_offer_IDs() {
		return xtra_removal_offer_IDs;
	}

	public List<String> getAnumber_serviceClass_include_filter() {
		return Anumber_serviceClass_include_filter;
	}

	public List<String> getAnumber_db_include_filter() {
		return Anumber_db_include_filter;
	}

	public List<String> getAnumber_serviceClass_exclude_filter() {
		return Anumber_serviceClass_exclude_filter;
	}

	public List<String> getAnumber_db_exclude_filter() {
		return Anumber_db_exclude_filter;
	}

	public List<String> getBnumber_serviceClass_include_filter() {
		return Bnumber_serviceClass_include_filter;
	}

	public List<String> getBnumber_db_include_filter() {
		return Bnumber_db_include_filter;
	}

	public List<String> getBnumber_serviceClass_exclude_filter() {
		return Bnumber_serviceClass_exclude_filter;
	}

	public List<String> getBnumber_db_exclude_filter() {
		return Bnumber_db_exclude_filter;
	}

	/*public List<String> getOriginOperatorIDs_list() {
		return originOperatorIDs_list;
	}*/

	public List<String> getAir_hosts() {
		return air_hosts;
	}

	public int getAir_io_sleep() {
		return air_io_sleep;
	}

	public int getAir_io_timeout() {
		return air_io_timeout;
	}

	public int getAir_io_threshold() {
		return air_io_threshold;
	}

	public String getAir_test_connection_msisdn() {
		return air_test_connection_msisdn;
	}

	public byte getAir_preferred_host() {
		return air_preferred_host;
	}

	public void setAir_preferred_host(byte air_preferred_host) {
		this.air_preferred_host = air_preferred_host;
	}

	public boolean isSet(String property_value) {
		if((property_value == null) || (property_value.trim().length() == 0)) return false;
		else return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

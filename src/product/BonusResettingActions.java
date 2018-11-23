package product;

import java.util.HashSet;

import connexions.AIRRequest;
import util.AccumulatorInformation;
import util.DedicatedAccount;
import util.UsageCounterUsageThresholdInformation;

public class BonusResettingActions {

	public BonusResettingActions() {
		
	}

	public void execute(String msisdn, ProductProperties productProperties) {
		HashSet<DedicatedAccount> dedicatedAccounts = new HashSet<DedicatedAccount>();
		DedicatedAccount dedicatedAccount = null;

		// sms advantages
		if(productProperties.getAdvantages_sms_da() == 0) ;
		else {
			dedicatedAccount = new DedicatedAccount(productProperties.getAdvantages_sms_da(), 0, null);
			dedicatedAccount.setRelative(false);
			dedicatedAccounts.add(dedicatedAccount);
		}

		// data advantages
		if(productProperties.getAdvantages_data_da() == 0) ;
		else {
			dedicatedAccount = new DedicatedAccount(productProperties.getAdvantages_data_da(), 0, null);
			dedicatedAccount.setRelative(false);
			dedicatedAccounts.add(dedicatedAccount);
		}

		// bonus sms onNet
		if(productProperties.getBonus_sms_onNet_da() == 0) ;
		else {
			dedicatedAccount = new DedicatedAccount(productProperties.getBonus_sms_onNet_da(), 0, null);
			dedicatedAccount.setRelative(false);
			dedicatedAccounts.add(dedicatedAccount);
		}

		// bonus sms offNet
		if(productProperties.getBonus_sms_offNet_da() == 0) ;
		else {
			dedicatedAccount = new DedicatedAccount(productProperties.getBonus_sms_offNet_da(), 0, null);
			dedicatedAccount.setRelative(false);
			dedicatedAccounts.add(dedicatedAccount);
		}



		// reset billed sms accumulator
		HashSet<AccumulatorInformation> accumulatorIDs = new HashSet<AccumulatorInformation>();
		AccumulatorInformation accumulatorInformation = null;

		if(productProperties.getBilled_sms_counter_accumulator() == 0) ;
		else {
	        accumulatorInformation = new AccumulatorInformation(productProperties.getBilled_sms_counter_accumulator(), 0, null, null);
	        accumulatorInformation.setAccumulatorValueRelative(false);
	        accumulatorIDs.add(accumulatorInformation);			
		}

		if(productProperties.getBilled_sms_amount_accumulator() == 0) ;
		else {
	        accumulatorInformation = new AccumulatorInformation(productProperties.getBilled_sms_amount_accumulator(), 0, null, null);
	        accumulatorInformation.setAccumulatorValueRelative(false);
	        accumulatorIDs.add(accumulatorInformation);
		}



        // reset billed services amount usageCounter
        HashSet<UsageCounterUsageThresholdInformation> counters = new HashSet<UsageCounterUsageThresholdInformation>();

        if(productProperties.getBilled_services_amount_usageCounterID() == 0) ;
        else {
            UsageCounterUsageThresholdInformation counter = new UsageCounterUsageThresholdInformation(productProperties.getBilled_services_amount_usageCounterID(), 0, productProperties.isBilled_services_amount_usageCounterID_Monetary());
            counter.setAdjustmentUsageCounterRelative(false);
            counters.add(counter);
        }



        // don't waiting for the response : set waitingForResponse false
        AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
        request.setWaitingForResponse(false);

		// delete DAs
        if(dedicatedAccounts.size() > 0) request.deleteDedicatedAccounts(msisdn, null, dedicatedAccounts, "eBA");
		// update Accumulators
        if(accumulatorIDs.size() > 0) request.updateAccumulators(msisdn, accumulatorIDs, "eBA");
        // update UC values
        if(counters.size() > 0) request.updateUsageThresholdsAndCounters(msisdn, counters, null, "eBA");

        // release waiting for the response : set waitingForResponse true
        request.setWaitingForResponse(true); request.setSuccessfully(true);
	}

}

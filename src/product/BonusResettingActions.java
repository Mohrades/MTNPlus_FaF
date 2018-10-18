package product;

import java.util.HashSet;

import connexions.AIRRequest;
import util.DedicatedAccount;

public class BonusResettingActions {

	public BonusResettingActions() {
		
	}

	public void execute(String msisdn, ProductProperties productProperties) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
		HashSet<DedicatedAccount> dedicatedAccounts = new HashSet<DedicatedAccount>();

		// sms advantages
		if(productProperties.getAdvantages_sms_da() == 0) ;
		else dedicatedAccounts.add(new DedicatedAccount(productProperties.getAdvantages_sms_da(), 0, null));

		// data advantages
		if(productProperties.getAdvantages_data_da() == 0) ;
		else dedicatedAccounts.add(new DedicatedAccount(productProperties.getAdvantages_data_da(), 0, null));

		// bonus sms onNet
		if(productProperties.getBonus_sms_onNet_da() == 0) ;
		else dedicatedAccounts.add(new DedicatedAccount(productProperties.getBonus_sms_onNet_da(), 0, null));

		// bonus sms offNet
		if(productProperties.getBonus_sms_offNet_da() == 0) ;
		else dedicatedAccounts.add(new DedicatedAccount(productProperties.getBonus_sms_offNet_da(), 0, null));

        // don't waiting for the response : set waitingForResponse false
        request.setWaitingForResponse(false);

		// delete DAs
        if(dedicatedAccounts.size() > 0) request.deleteDedicatedAccounts(msisdn, null, dedicatedAccounts, "eBA");

        // release waiting for the response : set waitingForResponse true
        request.setWaitingForResponse(true); request.setSuccessfully(true);
	}

}

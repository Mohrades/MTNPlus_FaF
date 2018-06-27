package product;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;

import org.springframework.context.MessageSource;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.FaFReportingDAOJdbc;
import dao.queries.SubscriberDAOJdbc;
import domain.models.FaFReporting;
import domain.models.Subscriber;
import util.BalanceAndDate;
import util.FafInformation;

public class PricePlanCurrent {

	public PricePlanCurrent() {

	}

	public Object [] activation(DAO dao, String msisdn, Subscriber subscriber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new PricePlanCurrentActivation()).execute(dao, msisdn, subscriber, i18n, language, productProperties, "eBA");
	}

	public Object [] deactivation(DAO dao, String msisdn, Subscriber subscriber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new PricePlanCurrentDeactivation()).execute(dao, msisdn, subscriber, i18n, language, productProperties, "eBA");
	}

	public Object[] getStatus(ProductProperties productProperties, MessageSource i18n, DAO dao, String msisdn, int language, boolean bonus) {
		Subscriber subscriber = new SubscriberDAOJdbc(dao).getOneSubscriber(msisdn);
		int statusCode = -1; // default

		if(subscriber == null) {
			statusCode = new PricePlanCurrentActions().isActivated(productProperties, dao, msisdn);

			// initialization the former price plan Status (formerly)
			if((statusCode == 0) || (statusCode == 1)) {
				AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
				HashSet<FafInformation> fafNumbers = request.getFaFList(msisdn, productProperties.getFafRequestedOwner()).getList();

				// be sure the initialization of the subscriber status and his previous attached fafNumbers is done with AIR availability
				if(request.isSuccessfully()) {
					subscriber = new Subscriber(0, msisdn, (statusCode == 0) ? true : false, (fafNumbers.size() >= productProperties.getFafMaxAllowedNumbers()) ? true : false, null, false);
					boolean registered = (new SubscriberDAOJdbc(dao).saveOneSubscriber(subscriber) == 1) ? true : false;

					if(registered) {
						subscriber = (new SubscriberDAOJdbc(dao)).getOneSubscriber(msisdn);
						// log the former fafNumber Status (formerly)
						for(FafInformation fafInformation : fafNumbers) {
							(new FaFReportingDAOJdbc(dao)).saveOneFaFReporting(new FaFReporting(0, subscriber.getId(), fafInformation.getFafNumber(), true, 0, null, "eBA")); // reporting
						}
					}
					else {
						statusCode = -1;
					}
				}
				else { // report the initialization to the next AIR availability
					statusCode = -1;
				}
			}
		}
		else {
			 if(subscriber.isLocked()) statusCode = -1;
			 else {
				 if(subscriber.isFlag()) {
					 statusCode = new PricePlanCurrentActions().isActivated(productProperties, dao, msisdn);

					 if(statusCode == 0) statusCode = 0; // success
					 else if(statusCode == 1) statusCode = -1; // anormal
					 else if(statusCode == -1) statusCode = -1; // erreur AIR
				 }
				 else statusCode = 1;
			 }
		}

		String message = null;

		if((statusCode == 0) && bonus) {
			Object[] bonusSMS = getBonusSMS(productProperties, msisdn, language);

			if(bonusSMS == null) message = i18n.getMessage("status.successful", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH);
			else message = i18n.getMessage("status.successful_with_bonus", new Object[] {(int)(bonusSMS[0]), (String)(bonusSMS[1])}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH);
		}
		else message = i18n.getMessage((statusCode == 0) ? "status.successful" : (statusCode == 1) ? "status.failed" : "service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH);

		return new Object [] {statusCode, message, subscriber};
	}

	public Object[] getBonusSMS(ProductProperties productProperties, String msisdn, int language) {
		long count = -1;
		String expires = null;

		try {
			AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());

			BalanceAndDate balanceBonusSmsOnNet = (productProperties.getBonus_sms_onNet_da() > 0) ? request.getBalanceAndDate(msisdn, productProperties.getBonus_sms_onNet_da()) : null;
			BalanceAndDate balanceBonusSmsOffNet = (productProperties.getBonus_sms_offNet_da() > 0) ? request.getBalanceAndDate(msisdn, productProperties.getBonus_sms_offNet_da()) : null;

			if(balanceBonusSmsOnNet != null) {
				count += ((productProperties.getBonus_sms_onNet_fees() * productProperties.getBonus_sms_threshold()) - balanceBonusSmsOnNet.getAccountValue()) / (productProperties.getBonus_sms_onNet_fees() * 100);
			}
			if(balanceBonusSmsOffNet != null) {
				count += ((productProperties.getBonus_sms_offNet_fees() * productProperties.getBonus_sms_threshold()) - balanceBonusSmsOffNet.getAccountValue()) / (productProperties.getBonus_sms_offNet_fees() * 100);
			}

			if(count >= 0) {
				expires = (language == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(balanceBonusSmsOnNet.getExpiryDate()) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH'H'mm")).format(balanceBonusSmsOffNet.getExpiryDate());
			}

		} catch(Throwable th) {
			
		}

		return (count == -1) ? null : new Object[] {(int) (productProperties.getBonus_sms_threshold() - count), expires};
	}
}

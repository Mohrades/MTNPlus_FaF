package product;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import org.springframework.context.MessageSource;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.FaFReportingDAOJdbc;
import dao.queries.RollBackDAOJdbc;
import domain.models.FaFReporting;
import domain.models.RollBack;
import domain.models.Subscriber;
import filter.MSISDNValidator;
import util.BalanceAndDate;
import util.DedicatedAccount;
import util.FaFAction;
import util.FafInformation;
import util.FafInformationList;

public class FaFNumberAdding {

	public FaFNumberAdding() {

	}

	public Object [] add(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID, boolean replace) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold());
		// Object [] requestStatus = new Object [2];

		if((request.getBalanceAndDate(subscriber.getValue(), 0)) != null) {
			if((subscriber.getId() <= 0) || (!subscriber.isFlag())) {
				return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			HashSet<FafInformation> fafNumbers = request.getFaFList(subscriber.getValue(), productProperties.getFafRequestedOwner()).getList();

			if((fafNumbers.size() > productProperties.getFafMaxAllowedNumbers()) || ((fafNumbers.size() >= productProperties.getFafMaxAllowedNumbers()) && (!replace))) {
				return new Object [] {1, i18n.getMessage("fafMaxAllowedNumbers.limit.reachedFlag", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}
			else {
				// check fafNumber already exist in list
				int offnet_count = 0;

				for(FafInformation fafInformation : fafNumbers) {
					if(fafInformation.getFafNumber().equalsIgnoreCase(fafNumber)) {
						return new Object [] {1, i18n.getMessage("fafNumberFound", new Object[] {fafNumber}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
					}
					// check offnet fafNumber
					if((new MSISDNValidator()).onNet(productProperties, productProperties.getMcc() + fafInformation.getFafNumber()));
					else offnet_count++;
				}

				// check offnet fafNumber limit reached
				if((offnet_count > productProperties.getFafMaxAllowedOffNetNumbers()) || ((!(new MSISDNValidator()).onNet(productProperties, productProperties.getMcc() + fafNumber)) && (offnet_count >= productProperties.getFafMaxAllowedOffNetNumbers()) && (!replace))) {
					return new Object [] {1, i18n.getMessage("fafMaxAllowedOffNetNumbers.limit.reachedFlag", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
				}
				else {
					// fire fafNumber Adding flow
					 boolean charged = subscriber.isFafChangeRequestChargingEnabled();
					 if(charged && productProperties.getFaf_chargingAmount() == 0) charged = false;

					 HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
					 if(productProperties.getChargingDA() == 0) balances.add(new BalanceAndDate(0, -productProperties.getFaf_chargingAmount(), null));
					 else balances.add(new DedicatedAccount(productProperties.getChargingDA(), -productProperties.getFaf_chargingAmount(), null));

					// update Anumber Balance
					if((!charged) || (request.updateBalanceAndDate(subscriber.getValue(), balances, productProperties.getSms_notifications_header(), "FAFADDINGCHARGING", "eBA"))) {
						HashSet<FafInformation> fafInformationList = new HashSet<FafInformation>();
						fafInformationList.add(new FafInformation(fafNumber, productProperties.getFafIndicator()));
						FafInformationList fafList = new FafInformationList(fafInformationList);

						// add fafNumber
				        if(request.updateFaFList(subscriber.getValue(), FaFAction.ADD, fafList, "eBA")) {
							(new FaFReportingDAOJdbc(dao)).saveOneFaFReporting(new FaFReporting(0, subscriber.getId(), fafNumber, true, charged ? productProperties.getFaf_chargingAmount() : 0, new Date(), originOperatorID)); // reporting
							return new Object [] {0, i18n.getMessage("fafAddingRequest.successful", new Object[] {fafNumber, (language == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(new Date()) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(new Date())}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
				        }
						else {
							if(request.isSuccessfully()) {
								balances = new HashSet<BalanceAndDate>();
								if(productProperties.getChargingDA() == 0) balances.add(new BalanceAndDate(0, productProperties.getFaf_chargingAmount(), null));
								else balances.add(new DedicatedAccount(productProperties.getChargingDA(), productProperties.getFaf_chargingAmount(), null));

								// refund
								if((!charged) || (request.updateBalanceAndDate(subscriber.getValue(), balances, productProperties.getSms_notifications_header(), "FAFADDINGREFUNDING", "eBA"))) {
									return new Object [] {1, i18n.getMessage("fafAddingRequest.failed", new Object[] {fafNumber}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
								}
								else {
									if(request.isSuccessfully()) {
										new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 101, 3, subscriber.getValue(), fafNumber, null));
									}
									else {
										new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -101, 3, subscriber.getValue(), fafNumber, null));
									}
								}

								return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
							}
							else {
								new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -2, 3, subscriber.getValue(), fafNumber, null));
								return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
							}
						}
					}
					else {
						if(request.isSuccessfully()) {
							return new Object [] {1, i18n.getMessage("balance.insufficient", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
						}
						else {
							new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, 3, subscriber.getValue(), fafNumber, null));
							return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
						}
					}
				}
			}
		}
		else {
			return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
	}

}

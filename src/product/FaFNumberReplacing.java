package product;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import org.springframework.context.MessageSource;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.JdbcFaFReportingDao;
import dao.queries.JdbcRollBackDao;
import domain.models.FaFReporting;
import domain.models.RollBack;
import domain.models.Subscriber;
import filter.MSISDNValidator;
import util.FafInformation;

public class FaFNumberReplacing {

	public FaFNumberReplacing() {

	}

	@SuppressWarnings("deprecation")
	public Object [] replace(DAO dao, Subscriber subscriber, String fafNumberOld, String fafNumberNew, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
		// Object [] requestStatus = new Object [2];

		if((request.getBalanceAndDate(subscriber.getValue(), 0)) != null) {
			if((subscriber.getId() <= 0) || (!subscriber.isFlag())) {
				return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			HashSet<FafInformation> fafNumbers = request.getFaFList(subscriber.getValue(), productProperties.getFafRequestedOwner()).getList();

			if(fafNumbers.size() > productProperties.getFafMaxAllowedNumbers()) {
				return new Object [] {1, i18n.getMessage("fafMaxAllowedNumbers.limit.reachedFlag", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			// check fafNumberNew already exist in list
			// check fafNumberOld exist in list
			boolean foundOld = false;
			int offnet_count = 0;

			for(FafInformation fafInformation : fafNumbers) {
				if(fafInformation.getFafNumber().equalsIgnoreCase(fafNumberNew)) {
					return new Object [] {1, i18n.getMessage("fafNumberFound", new Object[] {fafNumberNew}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
				}

				if(fafInformation.getFafNumber().equalsIgnoreCase(fafNumberOld)) {
					foundOld = true;
				}

				// check offnet fafNumber
				if((new MSISDNValidator()).onNet(productProperties, ((((productProperties.getMcc() + "").length() + productProperties.getMsisdn_length()) == (fafInformation.getFafNumber().length())) ? fafInformation.getFafNumber() : (productProperties.getMcc() + fafInformation.getFafNumber()))));
				else offnet_count++;
			}

			if(!foundOld) {
				return new Object [] {1, i18n.getMessage("fafNumberNotFound", new Object[] {fafNumberOld}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			// check offnet fafNumber limit reached
			if((!(new MSISDNValidator()).onNet(productProperties, ((((productProperties.getMcc() + "").length() + productProperties.getMsisdn_length()) == (fafNumberNew.length())) ? fafNumberNew : (productProperties.getMcc() + fafNumberNew)))) && ((offnet_count >= productProperties.getFafMaxAllowedOffNetNumbers()) && ((new MSISDNValidator()).onNet(productProperties, ((((productProperties.getMcc() + "").length() + productProperties.getMsisdn_length()) == (fafNumberOld.length())) ? fafNumberOld : (productProperties.getMcc() + fafNumberOld)))))) {
				return new Object [] {1, i18n.getMessage("fafMaxAllowedOffNetNumbers.limit.reachedFlag", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			// check fafNumberOld is older than FafChangeRequest startDate
			FaFReporting reporting = new JdbcFaFReportingDao(dao).getLastFaFChangeRequestReporting(subscriber.getId(), fafNumberOld);

			if((reporting == null) || (reporting.isFlag())) {
				Date CREATED_DATE_TIME = null;
				if((reporting != null) && (reporting.getCreated_date_time() != null)) {
					CREATED_DATE_TIME = reporting.getCreated_date_time();
					CREATED_DATE_TIME.setDate(CREATED_DATE_TIME.getDate() + productProperties.getFafChangeRequestAllowedDays());						
				}

				if((CREATED_DATE_TIME == null) || ((new Date()).after(CREATED_DATE_TIME))) {
					// add new faf
					Object [] requestStatus  = (new FaFNumberAdding()).add(dao, subscriber, fafNumberNew, i18n, language, productProperties, originOperatorID, true);
					if(((int)requestStatus[0] == 0)) {
						requestStatus  = (new FaFNumberDeletion()).delete(dao, subscriber, fafNumberOld, i18n, language, productProperties, originOperatorID);

						if(((int)requestStatus[0] == 0)) {
							Date fafChangeRequestDate = new Date();
							fafChangeRequestDate.setDate(fafChangeRequestDate.getDate() + productProperties.getFafChangeRequestAllowedDays());
							/*return new Object [] {0, i18n.getMessage("fafChangeRequest.successful", new Object[] {(((subscriber != null) && (subscriber.isFafChangeRequestChargingEnabled())) ? ((productProperties.getFaf_chargingAmount()/100) + "") : "0")}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};*/
							return new Object [] {0, i18n.getMessage("fafChangeRequest.successful", new Object[] {fafNumberNew, (language == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(fafChangeRequestDate) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(fafChangeRequestDate)}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
						}
						else {
							if((int)requestStatus[0] == 1) {
								new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, 1, 4, subscriber.getValue(), fafNumberOld, null));
							}
							else {
								new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, -1, 4, subscriber.getValue(), fafNumberOld, null));
							}

							return new Object [] {(int)requestStatus[0], ((int)requestStatus[0] == -1) ? i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH) : i18n.getMessage("fafChangeRequest.failed", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
						}
					}
					else {
						return requestStatus;
					}
				}
				else return new Object [] {1, i18n.getMessage("fafChangeRequestNotAllowed", new Object[] {fafNumberOld, (language == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(CREATED_DATE_TIME) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(CREATED_DATE_TIME)}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}
			else return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
		else {
			return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
	}

}

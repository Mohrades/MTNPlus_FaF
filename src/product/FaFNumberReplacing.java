package product;

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
import util.FafInformation;

public class FaFNumberReplacing {

	public FaFNumberReplacing() {

	}

	@SuppressWarnings("deprecation")
	public Object [] replace(DAO dao, Subscriber subscriber, String fafNumberOld, String fafNumberNew, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		AIRRequest request = new AIRRequest();
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
					return new Object [] {1, i18n.getMessage("fafNumberFound", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
				}

				if(fafInformation.getFafNumber().equalsIgnoreCase(fafNumberOld)) {
					foundOld = true;
				}

				// check offnet fafNumber
				if((new MSISDNValidator()).onNet(productProperties, fafInformation.getFafNumber()));
				else offnet_count++;
			}

			if(!foundOld) {
				return new Object [] {1, i18n.getMessage("fafNumberNotFound", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			// check offnet fafNumber limit reached
			if((!(new MSISDNValidator()).onNet(productProperties, fafNumberNew)) && ((offnet_count >= productProperties.getFafMaxAllowedOffNetNumbers()) && ((new MSISDNValidator()).onNet(productProperties, fafNumberOld)))) {
				return new Object [] {1, i18n.getMessage("FafMaxAllowedOffNetNumbers.limit.reachedFlag", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			// check fafNumberOld is older than FafChangeRequest startDate
			FaFReporting reporting = new FaFReportingDAOJdbc(dao).getLastFaFChangeRequestReporting(subscriber.getId(), fafNumberOld);

			if((reporting == null) || (reporting.isFlag())) {
				Date CREATED_DATE_TIME = null;
				if(reporting != null) {
					CREATED_DATE_TIME = reporting.getCreated_date_time();
					CREATED_DATE_TIME.setDate(CREATED_DATE_TIME.getDate() + productProperties.getFafChangeRequestAllowedDays());						
				}

				if((CREATED_DATE_TIME == null) || ((new Date()).after(CREATED_DATE_TIME))) {
					// add new faf
					Object [] requestStatus  = (new FaFNumberAdding()).add(dao, subscriber, fafNumberNew, i18n, language, productProperties, originOperatorID);
					if(((int)requestStatus[0] == 0)) {
						requestStatus  = (new FaFNumberDeletion()).delete(dao, subscriber, fafNumberOld, i18n, language, productProperties, originOperatorID);
						
						if(((int)requestStatus[0] == 0)) {
							return new Object [] {0, i18n.getMessage("fafChangeRequest.successful", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
						}
						else {
							if((int)requestStatus[0] == 1) {
								new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, 4, subscriber.getValue(), fafNumberOld, null));
							}
							else {
								new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, 4, subscriber.getValue(), fafNumberOld, null));
							}

							return new Object [] {(int)requestStatus[0], ((int)requestStatus[0] == -1) ? i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH) : i18n.getMessage("fafChangeRequest.failed", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
						}
					}
					else {
						return requestStatus;
					}
				}
				else return new Object [] {1, i18n.getMessage("fafChangeRequestNotAllowed", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}
			else return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
		else {
			return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
	}

}

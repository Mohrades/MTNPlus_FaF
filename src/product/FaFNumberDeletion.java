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
import util.FaFAction;
import util.FafInformation;
import util.FafInformationList;

public class FaFNumberDeletion {

	public FaFNumberDeletion() {

	}

	@SuppressWarnings("deprecation")
	public Object [] delete(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
		// Object [] requestStatus = new Object [2];

		if((request.getBalanceAndDate(subscriber.getValue(), 0)) != null) {
			if((subscriber.getId() <= 0) || (!subscriber.isFlag())) {
				return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			HashSet<FafInformation> fafNumbers = request.getFaFList(subscriber.getValue(), productProperties.getFafRequestedOwner()).getList();
			boolean found = false;

			for(FafInformation fafInformation : fafNumbers) {
				if(fafInformation.getFafNumber().equalsIgnoreCase(fafNumber)) {
					found = true;
					break;
				}
			}

			if(found) {
				FaFReporting reporting = (new JdbcFaFReportingDao(dao)).getLastFaFChangeRequestReporting(subscriber.getId(), fafNumber);

				if((reporting == null) || (reporting.isFlag())) {
					Date CREATED_DATE_TIME = null;
					if((reporting != null) && (reporting.getCreated_date_time() != null)) {
						CREATED_DATE_TIME = reporting.getCreated_date_time();
						CREATED_DATE_TIME.setDate(CREATED_DATE_TIME.getDate() + productProperties.getFafChangeRequestAllowedDays());
					}

					// check fafNumberOld is older than FafChangeRequest startDate
					if((CREATED_DATE_TIME == null) || ((new Date()).after(CREATED_DATE_TIME))) {
						HashSet<FafInformation> fafInformationList = new HashSet<FafInformation>();
						fafInformationList.add(new FafInformation(fafNumber, productProperties.getFafIndicator()));
						FafInformationList fafList = new FafInformationList(fafInformationList);

						// remove fafNumber
				        if(request.updateFaFList(subscriber.getValue(), FaFAction.DELETE, fafList, "eBA")) {
							(new JdbcFaFReportingDao(dao)).saveOneFaFReporting(new FaFReporting(0, subscriber.getId(), fafNumber, false, 0, new Date(), originOperatorID)); // reporting
							return new Object [] {0, i18n.getMessage("fafRemovalRequest.successful", new Object[] {fafNumber}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};				        	
				        }
						else {
							if(request.isSuccessfully()) {
								return new Object [] {1, i18n.getMessage("fafRemovalRequest.failed", new Object[] {fafNumber}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
							}
							else {
								new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, -1, 5, subscriber.getValue(), fafNumber, null));
								return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
							}
						}
					}
					else return new Object [] {1, i18n.getMessage("fafRemovalRequestNotAllowed", new Object[] {fafNumber, (language == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(CREATED_DATE_TIME) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(CREATED_DATE_TIME)}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
				}
				else return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}
			else return new Object [] {1, i18n.getMessage("fafNumberNotFound", new Object[] {fafNumber}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
		else {
			return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
	}

}

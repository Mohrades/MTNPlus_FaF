package product;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.JdbcFaFReportingDao;
import dao.queries.JdbcSubscriberDao;
import domain.models.FaFReporting;
import domain.models.Subscriber;
import util.FafInformation;

public class FaFManagement {

	public FaFManagement() {

	}

	public Object [] add(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberAdding()).add(dao, subscriber, fafNumber, i18n, language, productProperties, originOperatorID, false);
	}

	public Object [] delete(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberDeletion()).delete(dao, subscriber, fafNumber, i18n, language, productProperties, originOperatorID);
	}

	public Object [] replace(DAO dao, Subscriber subscriber, String fafNumberOld, String fafNumberNew, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberReplacing()).replace(dao, subscriber, fafNumberOld, fafNumberNew, i18n, language, productProperties, originOperatorID);
	}

	public Object[] getStatus(ProductProperties productProperties, MessageSource i18n, DAO dao, String msisdn, int language) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
		HashSet<FafInformation> fafNumbers = request.getFaFList(msisdn, productProperties.getFafRequestedOwner()).getList();

		if(!request.isSuccessfully()) {
			return new Object [] {-1, i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
		else {
			if((fafNumbers == null) || (fafNumbers.isEmpty())) {
				return new Object [] {1, i18n.getMessage("fafNumbers.empty", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
			}

			LinkedList<Long> fafNumbers_copy = new LinkedList<Long>();

			for(FafInformation fafInformation : fafNumbers) {
				fafNumbers_copy.add(Long.parseLong(fafInformation.getFafNumber()));
			}

			Collections.sort (fafNumbers_copy) ;
			// Collections.sort (fafNumbers_copy, Collections.reverseOrder()) ;

			String fafNumbersList = "";

			int index = 0;
			for(Long fafInformation : fafNumbers_copy) {
				index++;

				if(fafNumbersList.isEmpty()) fafNumbersList = index + ". " + fafInformation;
				else {
					fafNumbersList += "\n" + index + ". " + fafInformation;
				}
			}

			return new Object [] {0, i18n.getMessage("fafNumbers.list", new Object[] {fafNumbersList}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
	}

	public void setFafChangeRequestChargingEnabled(DAO dao, ProductProperties productProperties, Subscriber subscriber) {
		// check if fafChangeRequest must be charged
		if(!subscriber.isFafChangeRequestChargingEnabled()) {
			List<FaFReporting> reports = (new JdbcFaFReportingDao(dao)).getFaFReporting(subscriber.getId());
			int count = 0;

			for(FaFReporting faFReporting : reports) {
				if(faFReporting.isFlag()) {
					count++;
					if(count >= productProperties.getFafMaxAllowedNumbers()) {
						subscriber.setFafChangeRequestChargingEnabled(true);
						(new JdbcSubscriberDao(dao)).saveFafChargingStatus(subscriber);
						break;
					}
				}
			}
		}
	}
}

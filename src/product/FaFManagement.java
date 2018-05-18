package product;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.FaFReportingDAOJdbc;
import dao.queries.SubscriberDAOJdbc;
import domain.models.FaFReporting;
import domain.models.Subscriber;
import util.FafInformation;

public class FaFManagement {

	public FaFManagement() {

	}

	public Object [] add(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberAdding()).add(dao, subscriber, fafNumber, i18n, language, productProperties, originOperatorID);
	}

	public Object [] delete(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberDeletion()).delete(dao, subscriber, fafNumber, i18n, language, productProperties, originOperatorID);
	}

	public Object [] replace(DAO dao, Subscriber subscriber, String fafNumberOld, String fafNumberNew, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberReplacing()).replace(dao, subscriber, fafNumberOld, fafNumberNew, i18n, language, productProperties, originOperatorID);
	}

	public Object[] getStatus(ProductProperties productProperties, MessageSource i18n, DAO dao, String msisdn, int language) {
		AIRRequest request = new AIRRequest();
		HashSet<FafInformation> fafNumbers = request.getFaFList(msisdn, productProperties.getFafRequestedOwner()).getList();

		if(fafNumbers.isEmpty()) {
			return new Object [] {1, i18n.getMessage("fafNumbers.empty", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
		else {
			String fafNumbersList = "";

			for(FafInformation fafInformation : fafNumbers) {
				if(fafNumbersList.isEmpty()) fafNumbersList = fafInformation.getFafNumber();
				else {
					fafNumbersList += ", " + fafInformation.getFafNumber();
				}
			}

			return new Object [] {1, i18n.getMessage("fafNumbers.list", new Object[] {fafNumbersList}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH)};
		}
	}

	public void setFafChargingEnabled(DAO dao, ProductProperties productProperties, Subscriber subscriber) {
		// check if fafChangeRequest is charged
		if(!subscriber.isFafChargingEnabled()) {
			List<FaFReporting> reports = (new FaFReportingDAOJdbc(dao)).getFaFReporting(subscriber.getId());
			int count = 0;

			for(FaFReporting faFReporting : reports) {
				if(faFReporting.isFlag()) {
					count++;
					if(count >= productProperties.getFafMaxAllowedNumbers()) {
						subscriber.setFafChargingEnabled(true);
						(new SubscriberDAOJdbc(dao)).saveFafChargingStatus(subscriber);
						break;
					}
				}
			}
		}
	}
}

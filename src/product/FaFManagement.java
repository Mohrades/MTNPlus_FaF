package product;

import java.util.List;
import org.springframework.context.MessageSource;

import dao.DAO;
import dao.queries.FaFReportingDAOJdbc;
import dao.queries.SubscriberDAOJdbc;
import domain.models.FaFReporting;
import domain.models.Subscriber;

public class FaFManagement {

	public FaFManagement() {

	}

	public Object [] add(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberAdding()).add(dao, subscriber, fafNumber, i18n, language, productProperties, originOperatorID);
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

	public Object [] delete(DAO dao, Subscriber subscriber, String fafNumber, MessageSource i18n, int language, ProductProperties productProperties, String originOperatorID) {
		return (new FaFNumberDeletion()).delete(dao, subscriber, fafNumber, i18n, language, productProperties, originOperatorID);
	}
}

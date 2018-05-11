package product;

import java.util.Date;
import java.util.HashSet;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.RollBackDAOJdbc;
import dao.queries.SharingDAOJdbc;
import domain.models.RollBack;
import domain.models.Sharing;
import util.BalanceAndDate;
import util.DedicatedAccount;

public class ProductActions {
	
	public ProductActions() {
		
	}

	@SuppressWarnings("deprecation")
	public boolean doActions(ProductProperties productProperties, DAO dao, String Anumber, String Bnumber, int choice) {
		try {
			long volume_data = 0;

			try {
				volume_data = Long.parseLong(productProperties.getData_volume().get(choice-1));

			} catch(NullPointerException|NumberFormatException ex) {

			} catch(Exception ex) {

			} catch(Throwable th) {

			}

			// do action only if volume_data is strictly positive
			if(volume_data <= 0) return false;

			HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
			if((int) productProperties.getAnumber_da() == 0) balances.add(new BalanceAndDate((int) productProperties.getAnumber_da(), -volume_data, null));
			else balances.add(new DedicatedAccount((int) productProperties.getAnumber_da(), -volume_data, null));

			// update Anumber Balance
			if(new AIRRequest().updateBalanceAndDate(Anumber, balances, "TEST", "TEST", "eBA")) {
				balances.clear();
				Date expires = new Date();
				expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);expires.setDate(31);expires.setMonth(4);expires.setYear(118);
				if((int) productProperties.getBnumber_da() == 0) balances.add(new BalanceAndDate((int) productProperties.getBnumber_da(), volume_data, expires));
				else balances.add(new DedicatedAccount((int) productProperties.getBnumber_da(), volume_data, expires));

				// update Bnumber Balance
				if(new AIRRequest().updateBalanceAndDate(Bnumber, balances, "TEST", "TEST", "eBA")) {
					// update Bnumber sharing
					Sharing sharing = new SharingDAOJdbc(dao).getOneSharing(Bnumber);
					if(sharing == null) sharing = new Sharing(0, Bnumber, volume_data);
					else sharing.setValue(volume_data);

					new SharingDAOJdbc(dao).saveOneSharing(sharing);

					return true;
				}
				// rollback
				else {
					balances.clear();
					if((int) productProperties.getAnumber_da() == 0) balances.add(new BalanceAndDate((int) productProperties.getAnumber_da(), volume_data, null));
					else balances.add(new DedicatedAccount((int) productProperties.getAnumber_da(), volume_data, null));

					if(new AIRRequest().updateBalanceAndDate(Anumber, balances, "TEST", "TEST", "eBA"));
					else new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, choice, Anumber, Bnumber, null));
				}
			}

		} catch(Throwable th) {

		}

		return false;
	}

}

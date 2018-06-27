package product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.RollBackDAOJdbc;
import domain.models.RollBack;
import util.AccountDetails;
import util.BalanceAndDate;
import util.DedicatedAccount;
import util.ServiceOfferings;

public class PricePlanCurrentActions {

	public PricePlanCurrentActions() {

	}

	public String getInfo(MessageSource i18n, ProductProperties productProperties, String msisdn) {
		AccountDetails accountDetails = getAccountDetails((new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())), msisdn);
		int language = (accountDetails == null) ? 1 : accountDetails.getLanguageIDCurrent();

		return i18n.getMessage("info", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH);
	}

	public int isActivated(ProductProperties productProperties, DAO dao, String msisdn) {
		try {
			AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());

			if((productProperties.getOffer_id() == 0) || (!(request.getOffers(msisdn, new int[][] {{productProperties.getOffer_id(), productProperties.getOffer_id()}}, false, null, false).isEmpty()))) {
				if(productProperties.getServiceOfferings_IDs() != null) {
					AccountDetails accountDetails = getAccountDetails(request, msisdn);

					if(accountDetails == null) {
						if(request.isSuccessfully()) return 1;
						else return -1;
					}
					else {
						ServiceOfferings serviceOfferings = accountDetails.getServiceOfferings();

						serviceOfferings = accountDetails.getServiceOfferings();
						int size = productProperties.getServiceOfferings_IDs().size();

						for(int index = 0; index < size; index++) {
							int serviceOfferingID = Integer.parseInt(productProperties.getServiceOfferings_IDs().get(index));
							boolean activeFlag = (Integer.parseInt(productProperties.getServiceOfferings_activeFlags().get(index)) == 1) ? true : false;

							if((activeFlag && !serviceOfferings.isActiveFlag(serviceOfferingID)) || (!activeFlag && serviceOfferings.isActiveFlag(serviceOfferingID))) {
								return 1;
							}
						}

						return 0;
					}
				}
				else {
					return 0;
				}
			}
			else {
				if(request.isSuccessfully()) return 1;
				else return -1;
			}

		} catch(NullPointerException ex) {

		} catch(NumberFormatException ex) {

		} catch(Exception ex) {

		} catch(Throwable th) {

		}

		return -1;
	}

	@SuppressWarnings("deprecation")
	public int activation(ProductProperties productProperties, DAO dao, String msisdn, boolean charged, boolean advantages, String originOperatorID) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
		if(charged && productProperties.getActivation_chargingAmount() == 0) charged = false;

		HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
		if(productProperties.getChargingDA() == 0) balances.add(new BalanceAndDate(0, -productProperties.getActivation_chargingAmount(), null));
		else balances.add(new DedicatedAccount(productProperties.getChargingDA(), -productProperties.getActivation_chargingAmount(), null));

		// update Anumber Balance
		if((!charged) || (request.updateBalanceAndDate(msisdn, balances, productProperties.getSms_notifications_header(), "ACTIVATION", "eBA"))) {
			// update Anumber serviceOfferings
			ServiceOfferings serviceOfferings = null;
			if(productProperties.getServiceOfferings_IDs() != null) {
				serviceOfferings = new ServiceOfferings();
				int size = productProperties.getServiceOfferings_IDs().size();

				for(int index = 0; index < size; index++) {
					int serviceOfferingID = Integer.parseInt(productProperties.getServiceOfferings_IDs().get(index));
					boolean activeFlag = (Integer.parseInt(productProperties.getServiceOfferings_activeFlags().get(index)) == 1) ? true : false;
					serviceOfferings.SetActiveFlag(serviceOfferingID, activeFlag);
				}
			}

			if((serviceOfferings == null) || (request.updateSubscriberSegmentation(msisdn, null, serviceOfferings, "eBA"))) {
				// update Anumber Offer
				if((productProperties.getOffer_id() == 0) || (request.updateOffer(msisdn, productProperties.getOffer_id(), null, null, null, "eBA"))) {
					// community
					int[] communityInformationCurrent = null;
					int[] communityInformationNew = null;

					try {
						communityInformationCurrent = (productProperties.getCommunity_id() == 0) ? null : request.getAccountDetails(msisdn).getCommunityInformationCurrent();
						if(productProperties.getCommunity_id() == 0);
						else {
							if(communityInformationCurrent == null) communityInformationCurrent = new int[]{};

							// Java 8, convert array to List, primitive int[] to List<Integer>
							// List<Integer> list21 =  Arrays.asList(integers); // Cannot modify returned list
							List<Integer> communityInformationCurrentasList = new ArrayList<>(Arrays.stream(communityInformationCurrent).boxed().collect(Collectors.toList()));  // good : can modify returned list
							if(communityInformationCurrentasList.contains(new Integer(productProperties.getCommunity_id())));
							else {
								communityInformationCurrentasList.add(new Integer(productProperties.getCommunity_id()));
								communityInformationNew = communityInformationCurrentasList.stream().mapToInt(Integer::intValue).toArray();
							}
						}

					} catch(Throwable th) {
						if(productProperties.getCommunity_id() != 0) {
							// save rollback
							new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -4, 1, msisdn, msisdn, null));

							if(request.isSuccessfully()) ;
							else {
								// re-test connection
								productProperties.setAir_preferred_host((byte) (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).testConnection(productProperties.getAir_test_connection_msisdn(), productProperties.getAir_preferred_host()));
							}

							return -1;
						}
					}

					if((productProperties.getCommunity_id() == 0) || (communityInformationNew == null) || (communityInformationNew.length == 0) || (request.updateCommunityList(msisdn, communityInformationCurrent, communityInformationNew, "eBA"))) {
						// ADVANTAGES
						if(advantages) {
							Date expires = new Date();
							expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);

							balances = new HashSet<BalanceAndDate>();
							// sms advantages
							if(productProperties.getAdvantages_sms_value() != 0) {
								if(productProperties.getAdvantages_sms_da() == 0) balances.add(new BalanceAndDate(0, productProperties.getAdvantages_sms_value(), expires));
								else balances.add(new DedicatedAccount(productProperties.getAdvantages_sms_da(), productProperties.getAdvantages_sms_value(), expires));
							}

							// data advantages
							if(productProperties.getAdvantages_data_value() != 0) {
								if(productProperties.getAdvantages_data_da() == 0) balances.add(new BalanceAndDate(0, productProperties.getAdvantages_data_value(), expires));
								else balances.add(new DedicatedAccount(productProperties.getAdvantages_data_da(), productProperties.getAdvantages_data_value(), expires));
							}

							// update Anumber Balance
							if((balances.isEmpty()) || (request.updateBalanceAndDate(msisdn, balances, productProperties.getSms_notifications_header(), "ACTIVATION", "eBA"))) {

							}
							else {
								// save rollback
								new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, request.isSuccessfully() ? 5 : -5, 1, msisdn, msisdn, null));
							}
						}

						// delete others settings
						// delete serviceOfferings
						if(productProperties.getXtra_serviceOfferings_IDs() != null) {
							serviceOfferings = new ServiceOfferings();
							int size = productProperties.getXtra_serviceOfferings_IDs().size();

							for(int index = 0; index < size; index++) {
								int serviceOfferingID = Integer.parseInt(productProperties.getXtra_serviceOfferings_IDs().get(index));
								boolean activeFlag = (Integer.parseInt(productProperties.getXtra_serviceOfferings_activeFlags().get(index)) == 1) ? true : false;
								serviceOfferings.SetActiveFlag(serviceOfferingID, activeFlag);
							}
						}

						if((serviceOfferings == null) || (request.updateSubscriberSegmentation(msisdn, null, serviceOfferings, "eBA"))) {
							// delete offers
							int[] offerIDs = null;
							if(productProperties.getXtra_removal_offer_IDs() != null) {
								offerIDs = new int[productProperties.getXtra_removal_offer_IDs().size()];

								for(int index = 0; index < offerIDs.length; index++) {
									offerIDs[index] = Integer.parseInt(productProperties.getXtra_removal_offer_IDs().get(index));
								}
							}

							if(offerIDs != null) {
								for(int offerID : offerIDs) {
									if(request.deleteOffer(msisdn, offerID, "eBA", true));
									else {
										// save rollback
										new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, request.isSuccessfully() ? 7 : -7, 1, msisdn, msisdn, null));
									}
								}
							}
						}
						else {
							// save rollback
							new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, request.isSuccessfully() ? 6 : -6, 1, msisdn, msisdn, null));
						}

						return 0;
					}
					else {
						if(request.isSuccessfully()) {
							return new PricePlanCurrentRollBackActions().activation(3, productProperties, dao, msisdn, charged);
						}
						else {
							new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -4, 1, msisdn, msisdn, null));
							return -1;
						}						
					}
				}
				else {
					if(request.isSuccessfully()) {
						return new PricePlanCurrentRollBackActions().activation(2, productProperties, dao, msisdn, charged);
					}
					else {
						new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -3, 1, msisdn, msisdn, null));
						return -1;
					}
				}
			}
			else {
				if(request.isSuccessfully()) {
					return new PricePlanCurrentRollBackActions().activation(1, productProperties, dao, msisdn, charged);
				}
				else {
					new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -2, 1, msisdn, msisdn, null));
					return -1;
				}
			}
		}
		else {
			if(request.isSuccessfully()) {
				return 1;
			}
			else {
				new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, 1, msisdn, msisdn, null));
				return -1;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public int deactivation(ProductProperties productProperties, DAO dao, String msisdn, boolean charged) {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());
		if(charged && productProperties.getActivation_chargingAmount() == 0) charged = false;

		HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
		if(productProperties.getChargingDA() == 0) balances.add(new BalanceAndDate(0, -productProperties.getDeactivation_chargingAmount(), null));
		else balances.add(new DedicatedAccount((int) productProperties.getChargingDA(), -productProperties.getDeactivation_chargingAmount(), null));

		// update Anumber Balance
		if((!charged) || (request.updateBalanceAndDate(msisdn, balances, productProperties.getSms_notifications_header(), "DEACTIVATION", "eBA"))) {
			// update Anumber serviceOfferings
			ServiceOfferings serviceOfferings = null;
			if(productProperties.getServiceOfferings_IDs() != null) {
				serviceOfferings = new ServiceOfferings();
				int size = productProperties.getServiceOfferings_IDs().size();

				for(int index = 0; index < size; index++) {
					int serviceOfferingID = Integer.parseInt(productProperties.getServiceOfferings_IDs().get(index));
					boolean activeFlag = (Integer.parseInt(productProperties.getServiceOfferings_activeFlags().get(index)) == 1) ? false : true;
					serviceOfferings.SetActiveFlag(serviceOfferingID, activeFlag);
				}
			}

			if((serviceOfferings == null) || (request.updateSubscriberSegmentation(msisdn, null, serviceOfferings, "eBA"))) {
				// update Anumber Offer
				if((productProperties.getOffer_id() == 0) || (request.deleteOffer(msisdn, productProperties.getOffer_id(), "eBA", true))) {
					// community
					int[] communityInformationCurrent = null;
					int[] communityInformationNew = null;

					try {
						communityInformationCurrent = (productProperties.getCommunity_id() == 0) ? null : request.getAccountDetails(msisdn).getCommunityInformationCurrent();
						if((productProperties.getCommunity_id() == 0) || (communityInformationCurrent == null) || (communityInformationCurrent.length == 0)) ;
						else {
							// Java 8, convert array to List, primitive int[] to List<Integer>
							// List<Integer> list21 =  Arrays.asList(integers); // Cannot modify returned list
							List<Integer> communityInformationCurrentasList = new ArrayList<>(Arrays.stream(communityInformationCurrent).boxed().collect(Collectors.toList()));  // good : can modify returned list
							if(communityInformationCurrentasList.contains(new Integer(productProperties.getCommunity_id()))) {
								communityInformationCurrentasList.remove(new Integer(productProperties.getCommunity_id()));
								communityInformationNew = communityInformationCurrentasList.stream().mapToInt(Integer::intValue).toArray();
							}
						}

					} catch(Throwable th) {
						if(productProperties.getCommunity_id() != 0) {
							// save rollback
							new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -4, 2, msisdn, msisdn, null));

							if(request.isSuccessfully()) ;
							else {
								// re-test connection
								productProperties.setAir_preferred_host((byte) (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).testConnection(productProperties.getAir_test_connection_msisdn(), productProperties.getAir_preferred_host()));
							}

							return -1;
						}
					}

					if((productProperties.getCommunity_id() == 0) || (communityInformationNew == null) || (communityInformationCurrent == null) || (communityInformationCurrent.length == 0) ||  (request.updateCommunityList(msisdn, communityInformationCurrent, communityInformationNew, "eBA"))) {
						return 0;
					}
					else {
						if(request.isSuccessfully()) {
							return new PricePlanCurrentRollBackActions().deactivation(3, productProperties, dao, msisdn, charged);
						}
						else {
							new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -4, 2, msisdn, msisdn, null));
							return -1;
						}
					}
				}
				else {
					if(request.isSuccessfully()) {
						return new PricePlanCurrentRollBackActions().deactivation(2, productProperties, dao, msisdn, charged);
					}
					else {
						new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -3, 2, msisdn, msisdn, null));
						return -1;
					}
				}
			}
			else {
				if(request.isSuccessfully()) {
					return new PricePlanCurrentRollBackActions().deactivation(1, productProperties, dao, msisdn, charged);
				}
				else {
					new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -2, 2, msisdn, msisdn, null));
					return -1;
				}

			}
		}
		else {
			if(request.isSuccessfully()) {
				return 1;
			}
			else {
				new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, 2, msisdn, msisdn, null));
				return -1;
			}
		}
	}

	public AccountDetails getAccountDetails(AIRRequest request, String msisdn) {
		return request.getAccountDetails(msisdn);
	}

}

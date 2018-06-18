package handlers;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.context.MessageSource;

import com.google.common.base.Splitter;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.SubscriberDAOJdbc;
import domain.models.Subscriber;
import domain.models.USSDRequest;
import filter.MSISDNValidator;
import product.PricePlanCurrent;
import product.ProductProperties;
import util.FafInformation;

@SuppressWarnings("unused")
public class USSDFlow {

	public USSDFlow() {

	}

	public Map<String, Object> validate(USSDRequest ussd, int language, Document document, ProductProperties productProperties, MessageSource i18n, DAO dao) {
		// on crée le modèle de la vue à afficher
		Map<String, Object> modele = new HashMap<String, Object>();
		// initialization
		modele.put("status", -1);

		// on crée le modèle de l'arborescence
		StringJoiner tree = new StringJoiner(".", ".", "");
		tree.setEmptyValue("");

		try {
			// USSD(int id, long sessionId, String msisdn, String input, int step, Date last_update_time)
			// 250*1**263*abc*1*97975506  ==>  [250, 1, , 263, abc, 1, 97975506]
			// List<String> inputs = Splitter.onPattern("[.|,|;]").trimResults().omitEmptyStrings().splitToList(ussd.getInput());
			// List<String> inputs = Splitter.onPattern("[*]").trimResults().splitToList(ussd.getInput());
			List<String> inputs = Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput());

			int index = 0;
			Element currentState = null;

			transitions : for(String input : inputs) {
				if((input == null) || (input.isEmpty()) ||(input.length() == 0)) {
					return handleInvalidInput(i18n.getMessage("request.unavailable", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
				}

				// on-entry : verify service code
				if(index == 0) {
					if(document.getRootElement().getName().equals("SERVICE-CODE-"  + input)) {
						currentState = (document.getRootElement()).getChild("menu");
						index++;
					}
					else {
						return handleInvalidInput(i18n.getMessage("service.unavailable", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
					}
				}
				// transition : verify each step of the flow
				else {
					if(hasChildren(currentState)) {
						@SuppressWarnings("rawtypes")
						List children = currentState.getChildren("input");
						Element choice = currentState.getChild("choice-" + input);

						if(choice != null) {
							children = currentState.getChildren();

							@SuppressWarnings("rawtypes")
							ListIterator iterator = children.listIterator();
							int step = 0;

							while (iterator.hasNext()) {
								step++;
								Element element = (Element) iterator.next();

								if(element.getName().equals("choice-" + input)) {
									tree.add(step + "");
									break;
								}
							}

							currentState = choice;
							continue transitions;
						}
						else if(children.isEmpty()) {
							return handleInvalidInput(i18n.getMessage("integer.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else {
							children = currentState.getChildren();

							@SuppressWarnings("rawtypes")
							ListIterator iterator = children.listIterator();
							int step = 0;

							while (iterator.hasNext()) {
								step++;
								Element element = (Element) iterator.next();

								if(element.getName().startsWith("choice-")) {

								}
								else if(element.getName().equals("input")) {
									if(element.getAttributeValue("type").equals("static")) {
										if(input.equals(element.getAttributeValue("value"))) {
											currentState = element;
											tree.add(step + "");
											continue transitions;
										}
										else {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("request.unavailable", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
											}
										}
									}
									else if(element.getAttributeValue("type").equals("text")) {
										if(input.isEmpty()) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("argument.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
											}
										}
										else {
											currentState = element;
											tree.add(step + "");
											continue transitions;
										}
									}
									else if(element.getAttributeValue("type").equals("number")) {
										try {
											long number = Long.parseLong(input);

											if((((element.getAttributeValue("min") != null) && (number < Long.parseLong(element.getAttributeValue("min")))) || ((element.getAttributeValue("max") != null) && (number > Long.parseLong(element.getAttributeValue("max")))))) {
												if(children.size() == 1) {
													if((element.getAttributeValue("min") != null) && (element.getAttributeValue("max") != null)) {
														return handleInvalidInput(i18n.getMessage("integer.range", new Object[] {Long.parseLong(element.getAttributeValue("min")), Long.parseLong(element.getAttributeValue("max"))}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
													}
													else if(element.getAttributeValue("min") != null) {
														return handleInvalidInput(i18n.getMessage("integer.min", new Object[] {Long.parseLong(element.getAttributeValue("min"))}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
													}
													else if(element.getAttributeValue("max") != null) {
														return handleInvalidInput(i18n.getMessage("integer.max", new Object[] {Long.parseLong(element.getAttributeValue("max"))}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
													}
												}
											}
											else {
												currentState = element;
												tree.add(step + "");
												continue transitions;												
											}

										} catch(NullPointerException|NumberFormatException ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("integer.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
											}

										} catch(Throwable ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("integer.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
											}
										}
									}
									else if(element.getAttributeValue("type").equals("msisdn")) {
										try {
											String msisdn = Long.parseLong(input) + "";

											if((element.getAttributeValue("ton").equals("International") && (msisdn.startsWith(productProperties.getMcc() + "")) && (((productProperties.getMcc() + "").length() + productProperties.getMsisdn_length()) == msisdn.length())) || ((element.getAttributeValue("ton").equals("National")) && (productProperties.getMsisdn_length() == msisdn.length()))) {
											/*if((element.getAttributeValue("ton").equals("International")) || ((element.getAttributeValue("ton").equals("National")) && (webAppProperties.getMsisdn_length() == msisdn.length()))) {*/
												if((element.getAttributeValue("network") == null) || (element.getAttributeValue("network").isEmpty()) || (element.getAttributeValue("network").equals("off"))) {
													currentState = element;
													tree.add(step + "");
													continue transitions;
												}
												else if(element.getAttributeValue("network").equals("off")) {
													if(((element.getAttributeValue("ton").equals("National")) && !(new MSISDNValidator()).onNet(productProperties, productProperties.getMcc() + "" + msisdn)) || ((element.getAttributeValue("ton").equals("International")) && !(new MSISDNValidator()).onNet(productProperties, msisdn))) {
														currentState = element;
														tree.add(step + "");
														continue transitions;
													}
													else {
														if(children.size() == 1) {
															return handleInvalidInput(i18n.getMessage("msisdn.offnet.required", new Object[] {productProperties.getGsm_name()}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
														}
													}
												}
												else if(element.getAttributeValue("network").equals("on")) {
													if(((element.getAttributeValue("ton").equals("National")) && (new MSISDNValidator()).onNet(productProperties, productProperties.getMcc() + "" + msisdn)) || ((element.getAttributeValue("ton").equals("International")) && (new MSISDNValidator()).onNet(productProperties, msisdn))) {
														currentState = element;
														tree.add(step + "");
														continue transitions;
													}
													else {
														if(children.size() == 1) {
															return handleInvalidInput(i18n.getMessage("msisdn.onnet.required", new Object[] {productProperties.getGsm_name()}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
														}
													}
												}
											}
											else {
												if(children.size() == 1) {
													return handleInvalidInput(i18n.getMessage("msisdn.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
												}
											}

										} catch(NullPointerException|NumberFormatException ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("msisdn.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
											}

										} catch(Throwable ex) {
											if(children.size() == 1) {
												return handleInvalidInput(i18n.getMessage("msisdn.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
											}
										}
									}									
								}
							}

							return handleInvalidInput(i18n.getMessage("argument.required", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
					}
					else {
						return handleInvalidInput(i18n.getMessage("request.unavailable", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
					}
				}
			}

			// StringJoiner is used internally by static String.join().
			// String.join("-", "2015", "10", "31" ); // Join String by a delimiter ==> 2015-10-31
			// correct subscriber inputs in the good and recommended format
			ussd.setInput(String.join("*", inputs)); // Join a List by a delimiter

			// on-transition : view-state
			if(hasChildren(currentState)) {
				String transitions = tree.toString();
				/*transitions = transitions.replace(" ", "");
				transitions = transitions.replace("[", "");
				transitions = transitions.replace("]", "");
				transitions = transitions.replace(",", ".");*/
				transitions = transitions.trim();

				modele.put("status", 1);
				/*if(transitions.length() == 0) modele.put("message", i18n.getMessage("menu", null, null, null));
				else modele.put("message", i18n.getMessage("menu." + transitions, null, null, null));*/
				if(("menu" + transitions).startsWith("menu.4")) {
					Object [] requestStatus = (new PricePlanCurrent()).getStatus(productProperties, i18n, dao, ussd.getMsisdn(), language);

					// subscriber in price plan current
					if((int)(requestStatus[0]) == 0) {
						AIRRequest request = (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host()));

						if(("menu" + transitions).equals("menu.4")) {
							HashSet<FafInformation> fafNumbers = request.getFaFList(ussd.getMsisdn(), productProperties.getFafRequestedOwner()).getList();

							if(fafNumbers.isEmpty()) {
								modele.put("message", i18n.getMessage("menu.4_add", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
							}
							else if(fafNumbers.size() > productProperties.getFafMaxAllowedNumbers()) {
								modele.put("message", i18n.getMessage("menu.4_delete_and_status", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));								
							}
							else if(fafNumbers.size() >= productProperties.getFafMaxAllowedNumbers()) {
								modele.put("message", i18n.getMessage("menu.4_without_adding", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));								
							}
							else {
								modele.put("message", i18n.getMessage("menu.4_complete", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
							}
						}
						else if(("menu" + transitions).equals("menu.4.1.1")) {
							String fafNumber = Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput()).get(3);
							Subscriber subscriber = new SubscriberDAOJdbc(dao).getOneSubscriber(ussd.getMsisdn());

							modele.put("message", i18n.getMessage("menu" + transitions, new Object[] {fafNumber, (((subscriber != null) && (subscriber.isFafChangeRequestChargingEnabled())) ? " (" + productProperties.getFaf_chargingAmount() + "F)" : "")}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else if(("menu" + transitions).equals("menu.4.3")) {
							modele.put("message", i18n.getMessage("menu" + transitions, new Object[] {getFafNumbersList(request.getFaFList(ussd.getMsisdn(), productProperties.getFafRequestedOwner()).getList())}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else if(("menu" + transitions).equals("menu.4.3.1")) {
							int indexOld = Integer.parseInt(Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput()).get(3));
							HashSet<Integer> indexes = new HashSet<Integer>(); indexes.add(indexOld);
							HashSet<FafInformation> fafNumbers = request.getFaFList(ussd.getMsisdn(), productProperties.getFafRequestedOwner()).getList();
							String fafNumber = (getFaFNumbers(fafNumbers, productProperties, indexes)).get(indexOld);

							if(fafNumber == null) {
								modele.put("status", -1);
								modele.put("message", i18n.getMessage("integer.max", new Object[] {fafNumbers.size()}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
							}
							else modele.put("message", i18n.getMessage("menu" + transitions, new Object[] {fafNumber}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else if(("menu" + transitions).equals("menu.4.2")) {
							modele.put("message", i18n.getMessage("menu" + transitions, new Object[] {getFafNumbersList(request.getFaFList(ussd.getMsisdn(), productProperties.getFafRequestedOwner()).getList())}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else if(("menu" + transitions).equals("menu.4.2.1")) {
							int indexOld = Integer.parseInt(Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput()).get(3));
							HashSet<Integer> indexes = new HashSet<Integer>(); indexes.add(indexOld);
							HashSet<FafInformation> fafNumbers = request.getFaFList(ussd.getMsisdn(), productProperties.getFafRequestedOwner()).getList();
							HashMap<Integer, String> result = (getFaFNumbers(fafNumbers, productProperties, indexes));
							String fafNumberOld = result.get(indexOld);

							if(fafNumberOld == null) {
								modele.put("status", -1);
								modele.put("message", i18n.getMessage("integer.max", new Object[] {fafNumbers.size()}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
							}
							else modele.put("message", i18n.getMessage("menu" + transitions, null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else if(("menu" + transitions).equals("menu.4.2.1.1")) {
							int indexOld = Integer.parseInt(Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput()).get(3));
							HashSet<Integer> indexes = new HashSet<Integer>(); indexes.add(indexOld);
							HashSet<FafInformation> fafNumbers = request.getFaFList(ussd.getMsisdn(), productProperties.getFafRequestedOwner()).getList();
							HashMap<Integer, String> result = (getFaFNumbers(fafNumbers, productProperties, indexes));
							String fafNumberOld = result.get(indexOld);
							String fafNumberNew = Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput()).get(4);

							if(fafNumberOld == null) {
								modele.put("status", -1);
								modele.put("message", i18n.getMessage("integer.max", new Object[] {fafNumbers.size()}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
							}
							else modele.put("message", i18n.getMessage("menu" + transitions, new Object[] {fafNumberOld, fafNumberNew}, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));							
						}
						else modele.put("message", i18n.getMessage("menu" + transitions, null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
					}
					// subscriber not in price plan current
					else if((int)(requestStatus[0]) == 1) {
						if(("menu" + transitions).equals("menu.4")) {// output only fafNumbers status choice
							modele.put("message", i18n.getMessage("menu.4_status", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						}
						else handleNotAllowedMenu(modele, i18n, language);
					}
					// subscriber price plan status unknown
					else handleServiceError(modele, i18n, language);
				}
				else {
					modele.put("message", i18n.getMessage("menu" + transitions, null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
				}
			}
			// on-end : end-state
			else {
				modele.put("status", 0);
			}

		} catch(NullPointerException ex) {
			handleException(modele, i18n, language);

		} catch(Throwable th) {
			handleException(modele, i18n, language);
		}

		return modele;
	}

	public void handleServiceError(Map<String, Object> modele, MessageSource i18n, int language) {
		modele.put("status", -1);
		modele.put("message", i18n.getMessage("service.internal.error", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
	}

	public void handleNotAllowedMenu(Map<String, Object> modele, MessageSource i18n, int language) {
		modele.put("status", -1);
		modele.put("message", i18n.getMessage("menu.disabled", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
	}

	public void handleException(Map<String, Object> modele, MessageSource i18n, int language) {
		modele.put("status", -1);
		modele.put("message", i18n.getMessage("request.unavailable", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
	}

	public Map<String, Object> handleInvalidInput(String message) {
		// on crée le modèle de la vue à afficher
		Map<String, Object> modele = new HashMap<String, Object>();

		modele.put("status", -1);
		modele.put("message", message);
		return modele;
	}

	public boolean hasChildren(Element currentSate) {
		return (currentSate == null) ? false : (currentSate.getChildren().size() > 0);
	}

	public HashMap<Integer, String> getFaFNumbers(HashSet<FafInformation> fafNumbers, ProductProperties productProperties, HashSet<Integer> indexes) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();

		LinkedList<Long> fafNumbers_copy = new LinkedList<Long>();
		for(FafInformation fafInformation : fafNumbers) {
			fafNumbers_copy.add(Long.parseLong(fafInformation.getFafNumber()));
		}

		Collections.sort (fafNumbers_copy) ;
		// Collections.sort (fafNumbers_copy, Collections.reverseOrder()) ;

		int i = 0;
		for(Long fafInformation : fafNumbers_copy) {
			i++;

			if(indexes.contains(i)) {
				result.put(i, fafInformation + "");
			}
		}

		return result;
	}

	public String getFafNumbersList(HashSet<FafInformation> fafNumbers) {
		String fafNumbersList = "";

		LinkedList<Long> fafNumbers_copy = new LinkedList<Long>();
		for(FafInformation fafInformation : fafNumbers) {
			fafNumbers_copy.add(Long.parseLong(fafInformation.getFafNumber()));
		}

		Collections.sort (fafNumbers_copy) ;

		int index = 0;
		for(Long fafInformation : fafNumbers_copy) {
			index++;

			if(fafNumbersList.isEmpty()) fafNumbersList = index + ". " + fafInformation;
			else {
				fafNumbersList += "\n" + index + ". " + fafInformation;
			}
		}

		return fafNumbersList;
	}

}

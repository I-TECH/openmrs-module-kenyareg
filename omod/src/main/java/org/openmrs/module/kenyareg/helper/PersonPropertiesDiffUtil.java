package org.openmrs.module.kenyareg.helper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.kenyareg.api.PersonMergeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonIdentifier.Type;

public class PersonPropertiesDiffUtil {
	private static Logger log = LoggerFactory.getLogger(PersonMergeService.class);

	private static final String[] properties = new String[] { "lastName", "firstName", "middleName", "otherName",
			"clanName", "sex", "birthdate", "mothersFirstName", "mothersMiddleName", "mothersLastName",
			"fathersFirstName", "fathersMiddleName", "fathersLastName", "villageName", "maritalStatus" };

	public static Map<String, Object> getMatchingProperties(Person fromLpi, Person fromMpi) {
		if (fromLpi == null && fromMpi == null) {
			return Collections.emptyMap();
		}
		if (fromLpi == null && fromMpi != null) {
			fromLpi = new Person();
		} else if (fromLpi != null && fromMpi == null) {
			fromMpi = new Person();
		}
		Map<String, Object> mergedProperties = new HashMap<String, Object>();
		for (String property : properties) {
			String methodName = "get" + StringUtils.capitalize(property);
			try {
				Method method = Person.class.getMethod(methodName);
				Object lpiValue = method.invoke(fromLpi);
				Object mpiValue = method.invoke(fromMpi);
				if (lpiValue == null && mpiValue == null) {
					continue;
				}
				if (lpiValue instanceof String || mpiValue instanceof String) {
					if (lpiValue == null) {
						mergedProperties.put(property, mpiValue.toString());
					} else if (mpiValue == null) {
						mergedProperties.put(property, lpiValue.toString());
					} else if (StringUtils.equalsIgnoreCase(lpiValue.toString(), mpiValue.toString())) {
						mergedProperties.put(property, lpiValue.toString());
					}
				} else if (lpiValue instanceof Date || mpiValue instanceof Date) {
					Date mpiDate = null;
					Date lpiDate = null;
					if (lpiValue == null) {
						mpiDate = (Date) mpiValue;
						mergedProperties.put(property, mpiDate);
						continue;
					} else if (mpiValue == null) {
						lpiDate = (Date) lpiValue;
						mergedProperties.put(property, lpiDate);
						continue;
					}
					lpiDate = (Date) lpiValue;
					mpiDate = (Date) mpiValue;
					if (lpiDate.compareTo(mpiDate) == 0) {
						mergedProperties.put(property, lpiDate);
					}
				} else {
					if (lpiValue == null) {
						mergedProperties.put(property, mpiValue);
					} else if (mpiValue == null) {
						mergedProperties.put(property, lpiValue.toString());
					} else if (lpiValue == mpiValue) {
						mergedProperties.put(property, lpiValue.toString());
					}
				}
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return mergedProperties;
	}

	public static Map<String, Map<String, Object>> getConflictingProperties(Person fromLpi, Person fromMpi) {
		if (fromLpi == null && fromMpi == null) {
			return Collections.emptyMap();
		}
		Map<String, Map<String, Object>> conflictingProperties = new HashMap<String, Map<String, Object>>();
		for (String property : properties) {
			String methodName = "get" + StringUtils.capitalize(property);
			try {
				Method method = Person.class.getMethod(methodName);
				Object lpiValue = method.invoke(fromLpi);
				Object mpiValue = method.invoke(fromMpi);
				if (lpiValue == null || mpiValue == null) {
					continue;
				}
				if (lpiValue instanceof String || mpiValue instanceof String) {
					if (!StringUtils.equalsIgnoreCase(lpiValue.toString(), mpiValue.toString())) {
						Map<String, Object> conflictedProperty = new HashMap<String, Object>();
						conflictedProperty.put("lpi", lpiValue.toString());
						conflictedProperty.put("mpi", mpiValue.toString());
						conflictingProperties.put(property, conflictedProperty);
					}
				} else if (lpiValue instanceof Date || mpiValue instanceof Date) {
					Date mpiDate = (Date) lpiValue;
					Date lpiDate = (Date) mpiValue;
					if (lpiDate.compareTo(mpiDate) != 0) {
						Map<String, Object> conflictingProperty = new HashMap<String, Object>();
						conflictingProperty.put("lpi", lpiDate);
						conflictingProperty.put("mpi", mpiDate);
						conflictingProperties.put(property, conflictingProperty);
					}
				} else {
					if (lpiValue != mpiValue) {
						Map<String, Object> conflictingProperty = new HashMap<String, Object>();
						conflictingProperty.put("lpi", lpiValue.toString());
						conflictingProperty.put("mpi", mpiValue.toString());
						conflictingProperties.put(property, conflictingProperty);
					}
				}
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return conflictingProperties;
	}

	private static PersonIdentifier getPersonIdentifier(List<PersonIdentifier> personIdentifiers, Type type) {
		for (PersonIdentifier personIdentifier : personIdentifiers) {
			if (personIdentifier.getIdentifierType().equals(type)) {
				return personIdentifier;
			}
		}
		return null;
	}

	public static Map<String, String> getMatchingIdentifiers(Person fromLpi, Person fromMpi) {
		Map<String, String> lpiMpiMergedIdentifiers = new HashMap<String, String>();

		if (fromLpi != null && fromMpi != null) {
			if (fromLpi.getPersonIdentifierList() != null && fromMpi.getPersonIdentifierList() != null) {
				for (PersonIdentifier personIdFromLpi : fromLpi.getPersonIdentifierList()) {
					PersonIdentifier personIdFromMpi = getPersonIdentifier(fromMpi.getPersonIdentifierList(),
							personIdFromLpi.getIdentifierType());
					if (personIdFromMpi != null) {
						if (personIdFromLpi.equals(personIdFromMpi)) {
							lpiMpiMergedIdentifiers.put(personIdFromMpi.getIdentifierType().toString(),
									personIdFromMpi.getIdentifier());
						} else if (personIdFromLpi.getIdentifierType().equals(Type.nupi)
								|| personIdFromLpi.getIdentifierType().equals(Type.masterPatientRegistryId)) {
							lpiMpiMergedIdentifiers.put(personIdFromMpi.getIdentifierType().toString(),
									personIdFromMpi.getIdentifier());
						}
					}
				}
			} else if (fromLpi.getPersonIdentifierList() != null && fromMpi.getPersonIdentifierList() == null) {
				for (PersonIdentifier personIdFromLpi : fromLpi.getPersonIdentifierList()) {
					lpiMpiMergedIdentifiers.put(personIdFromLpi.getIdentifierType().toString(),
							personIdFromLpi.getIdentifier());
				}
			} else if (fromLpi.getPersonIdentifierList() == null && fromMpi.getPersonIdentifierList() != null) {
				for (PersonIdentifier personIdFromMpi : fromMpi.getPersonIdentifierList()) {
					lpiMpiMergedIdentifiers.put(personIdFromMpi.getIdentifierType().toString(),
							personIdFromMpi.getIdentifier());
				}
			}
		} else if (fromLpi != null && fromMpi == null) {
			if (fromLpi.getPersonIdentifierList() != null) {
				for (PersonIdentifier personIdFromLpi : fromLpi.getPersonIdentifierList()) {
					lpiMpiMergedIdentifiers.put(personIdFromLpi.getIdentifierType().toString(),
							personIdFromLpi.getIdentifier());
				}
			}
		} else if (fromLpi == null && fromMpi != null) {
			if (fromMpi.getPersonIdentifierList() != null) {
				for (PersonIdentifier personIdFromMpi : fromMpi.getPersonIdentifierList()) {
					lpiMpiMergedIdentifiers.put(personIdFromMpi.getIdentifierType().toString(),
							personIdFromMpi.getIdentifier());
				}
			}
		}
		return lpiMpiMergedIdentifiers;
	}

	public static Map<String, Map<String, String>> getConflictingIdentifiers(Person fromLpi, Person fromMpi) {
		if (fromLpi == null || fromMpi == null) {
			return Collections.emptyMap();
		} else if (CollectionUtils.isEmpty(fromLpi.getPersonIdentifierList())
				|| CollectionUtils.isEmpty(fromMpi.getPersonIdentifierList())) {
			// handle NPE thrown when accessing fields for a null
			// fromLpi/fromMpi object
			return Collections.emptyMap();
		} else {
			Map<String, Map<String, String>> conflictingIdentifiers = new HashMap<String, Map<String, String>>();
			for (PersonIdentifier lpiIdentifier : fromLpi.getPersonIdentifierList()) {
				if (lpiIdentifier.getIdentifierType().equals(Type.nupi)
						|| lpiIdentifier.getIdentifierType().equals(Type.masterPatientRegistryId)) {
					continue;
				}
				for (PersonIdentifier mpiIdentifier : fromMpi.getPersonIdentifierList()) {
					if (lpiIdentifier.getIdentifierType().equals(mpiIdentifier.getIdentifierType())
							&& !lpiIdentifier.getIdentifier().equalsIgnoreCase(mpiIdentifier.getIdentifier())) {
						Map<String, String> conflictingIdentifierPair = new HashMap<String, String>();
						conflictingIdentifierPair.put("lpi", lpiIdentifier.getIdentifier());
						conflictingIdentifierPair.put("mpi", mpiIdentifier.getIdentifier());
						conflictingIdentifiers.put(lpiIdentifier.getIdentifierType().toString(),
								conflictingIdentifierPair);
					}
				}
			}
			return conflictingIdentifiers;
		}
	}

}

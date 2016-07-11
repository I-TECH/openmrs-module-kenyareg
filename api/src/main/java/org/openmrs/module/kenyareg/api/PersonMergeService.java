package org.openmrs.module.kenyareg.api;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;

@Service("personMergeService")
public class PersonMergeService {

	private Logger log = LoggerFactory.getLogger(PersonMergeService.class);

	@Autowired(required = true)
	@Qualifier("patientService")
	PatientService patientService;
	private final String[] properties = new String[]
			{
				"lastName", "firstName", "middleName", "otherName",
				"clanName", "sex", "birthdate", "mothersFirstName",
				"mothersMiddleName", "mothersLastName", "fathersFirstName",
				"fathersMiddleName", "fathersLastName",
				"villageName", "maritalStatus"
			};

	public void mergePatientIdentifiers(Patient patient, List<PersonIdentifier> identifiers, Location location) {
		assert patient != null;
		assert identifiers != null;
		for (PersonIdentifier identifier: identifiers) {
			if (matchesIdentifierInOmrs(identifier.getIdentifierType().toString())) {
				updateOmrsIdentifier(patient, identifier.getIdentifier(), identifier.getIdentifierType().toString(), location);
			}
		}
	}

	private boolean matchesIdentifierInOmrs(String identifierName) {
		List<PatientIdentifierType> availableOmrsIdentifierTypes = patientService.getAllPatientIdentifierTypes();

		for (PatientIdentifierType identifierType : availableOmrsIdentifierTypes) {
			if (identifierType.getName().equalsIgnoreCase(identifierName)) {
				return true;
			}
		}
		return false;
	}

	private void updateOmrsIdentifier(Patient omrsPerson, String identifier, String identifierType, Location location) {
		PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByName(identifierType);
		PatientIdentifier patientIdentifier = new PatientIdentifier(identifier, patientIdentifierType, location);
		boolean identifierSet = false;
		for (PatientIdentifier existingIdentifier : omrsPerson.getIdentifiers()) {
			if (existingIdentifier.getIdentifierType().getName().equalsIgnoreCase(identifierType)
					&& !existingIdentifier.equalsContent(patientIdentifier)) {
				existingIdentifier.setIdentifier(identifier);
				identifierSet = true;
				break;
			}
		}
		if (!identifierSet) {
			omrsPerson.addIdentifier(patientIdentifier);
		}
	}

	public org.openmrs.Person mergePerson(org.openmrs.Person fromOmrs, Person fromMpi) {
		if (fromOmrs == null) {
			fromOmrs = new org.openmrs.Person();
			fromOmrs.setUuid(fromMpi.getPersonGuid());
		}
		fromOmrs.setGender(fromMpi.getSex() == null ? "M" : fromMpi.getSex().toString());
		fromOmrs.setBirthdate(fromMpi.getBirthdate());
		fromOmrs.setDeathDate(fromMpi.getDeathdate());

		PersonName personName = fromOmrs.getPersonName();
		if (personName == null) {
			personName = new PersonName();
			personName.setPerson(fromOmrs);
		}
		personName.setGivenName(fromMpi.getFirstName());
		personName.setMiddleName(fromMpi.getMiddleName());
		personName.setFamilyName(fromMpi.getLastName());
		fromOmrs.addName(personName);

		return fromOmrs;
	}

	public Map<String, Object> getLpiMpiMergedProperties(Person fromLpi, Person fromMpi) {
		if (fromLpi == null && fromMpi == null) {
			return Collections.emptyMap();
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
						mpiDate = (Date)mpiValue;
						mergedProperties.put(property, mpiDate);
						continue;
					} else if (mpiValue == null) {
						lpiDate = (Date)lpiValue;
						mergedProperties.put(property, lpiDate);
						continue;
					}
					lpiDate = (Date)lpiValue;
					mpiDate = (Date)mpiValue;
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

	public Map<String, Map<String, Object>> getLpiMpiConflictingProperties(Person fromLpi, Person fromMpi) {
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
					Date mpiDate = (Date)lpiValue;;
					Date lpiDate = (Date)mpiValue;
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

}

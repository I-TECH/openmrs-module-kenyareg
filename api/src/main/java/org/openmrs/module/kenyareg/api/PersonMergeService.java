package org.openmrs.module.kenyareg.api;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonIdentifier.Type;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.module.kenyareg.api.utils.OpenmrsPersonToOecPersonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("personMergeService")
public class PersonMergeService {

    @Autowired(required = true)
    @Qualifier("patientService")
    PatientService patientService;

    public void mergePatientIdentifiers(Patient patient, List<PersonIdentifier> identifiers, Location location) {
        assert patient != null;
        assert identifiers != null;
        for (PersonIdentifier identifier : identifiers) {
            if (identifier.getIdentifierType().equals(Type.nupi)) {
                patient.setUuid(identifier.getIdentifier());
                continue;
            }
            String typeName = identifier.getIdentifierType().name();
            OpenmrsPersonToOecPersonConverter.OpenmrsIdentifierTypeConverter openmrsIdentifierTypeConverter =
                    OpenmrsPersonToOecPersonConverter.OpenmrsIdentifierTypeConverter.valueOf(typeName);
            String typeString = openmrsIdentifierTypeConverter.getTypeString();
            if (matchesIdentifierInOmrs(typeString)) {
                updateOmrsIdentifier(patient, identifier.getIdentifier(), typeString, location);
            }
        }
    }

    private boolean matchesIdentifierInOmrs(String identifierName) {
        List<PatientIdentifierType> availableOmrsIdentifierTypes = patientService.getAllPatientIdentifierTypes();
        List<String> idString = new ArrayList<String>();
        for (PatientIdentifierType pi : availableOmrsIdentifierTypes) {
            idString.add(pi.getName());
        }
        return idString.contains(identifierName);
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

}

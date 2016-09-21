package org.openmrs.module.kenyareg.fragment.controller;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonIdentifier.Type;
import org.apache.commons.lang.StringUtils;
import org.go2itech.oecui.data.RequestResult;
import org.openmrs.Patient;
import org.openmrs.module.kenyareg.api.PersonMergeService;
import org.openmrs.module.kenyareg.api.RegistryService;
import org.openmrs.module.kenyareg.helper.PersonPropertiesDiffUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentActionRequest;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.ObjectResult;
import org.openmrs.ui.framework.session.Session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class PersonEditorFragmentController {

    public void controller(
            @SpringBean("personMergeService") PersonMergeService mergeService,
            FragmentConfiguration config,
            FragmentModel model,
            Session session
    ) {
        Person fromLpi = null;
        if (config.containsKey("lpiUid") && StringUtils.isNotBlank(config.get("lpiUid").toString())) {
            String uuid = config.get("lpiUid").toString();
            @SuppressWarnings("unchecked")
            List<Person> lpiPersonList = (List<Person>) session.getAttribute("lpiResult", RequestResult.class).getData();
            for (Person person : lpiPersonList) {
                if (person.getPersonGuid().equals(uuid)) {
                    fromLpi = person;
                    break;
                }
            }
        }

        Person fromMpi = null;
        if (config.containsKey("mpiUid") && StringUtils.isNotBlank(config.get("mpiUid").toString())) {
            String uuid = config.get("mpiUid").toString();
            @SuppressWarnings("unchecked")
            List<Person> mpiPersonList = (List<Person>) session.getAttribute("mpiResult", RequestResult.class).getData();
            for (Person person : mpiPersonList) {
                if (person.getPersonGuid().equals(uuid)) {
                    fromMpi = person;
                    break;
                }
            }
        }
        session.setAttribute("lpiMatch", fromLpi);
        session.setAttribute("mpiMatch", fromMpi);
        model.addAttribute("lpiUid", fromLpi != null ? fromLpi.getPersonGuid() : "");
        model.addAttribute("mpiUid", fromMpi != null ? fromMpi.getPersonGuid() : null);
        model.addAttribute("mergedIdentifiers", PersonPropertiesDiffUtil.getMatchingIdentifiers(fromLpi, fromMpi));
        model.addAttribute("conflictingIdentifiers", PersonPropertiesDiffUtil.getConflictingIdentifiers(fromLpi, fromMpi));
        model.addAttribute("mergedProperties", PersonPropertiesDiffUtil.getMatchingProperties(fromLpi, fromMpi));
        model.addAttribute("conflictedProperties", PersonPropertiesDiffUtil.getConflictingProperties(fromLpi, fromMpi));

    }

    public FragmentActionResult update(
            @SpringBean("personMergeService") PersonMergeService mergeService,
            @SpringBean("registryService") RegistryService registryService,
            @BindParams Person person, FragmentActionRequest request,
            Session session, UiUtils ui) {
        Person lpiMatch = session.getAttribute("lpiMatch", Person.class);
        Person mpiMatch = session.getAttribute("mpiMatch", Person.class);
        validateConflictingPersonProperty(mergeService, request, lpiMatch, mpiMatch);
        validateConflictingPersonIdentifiers(mergeService, request, lpiMatch, mpiMatch);

        if (request.hasErrors()) {
            return new FailureResult(request.getErrors());
        }

        List<PersonIdentifier> personIdentifiers = getPersonIdentifiers(request);
        person.setPersonIdentifierList(personIdentifiers);
        Patient patient = registryService.acceptPerson(person, lpiMatch, mpiMatch);
        return new ObjectResult(SimpleObject.create("patientId", patient.getId()));
    }

    private List<PersonIdentifier> getPersonIdentifiers(FragmentActionRequest request) {
        List<PersonIdentifier> personIdentifiers = new ArrayList<PersonIdentifier>();
        Enumeration keys = request.getHttpRequest().getParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (StringUtils.contains(key, "identifier") && !StringUtils.contains(key, "conflict")) {
                String identifierType = StringUtils.remove(key, "identifier_");
                String identifierValue = request.getParameter(key);
                if (StringUtils.isNotBlank(identifierValue)) {
                    PersonIdentifier personIdentifier = new PersonIdentifier();
                    personIdentifier.setIdentifierType(Type.valueOf(identifierType));
                    personIdentifier.setIdentifier(identifierValue);
                    personIdentifiers.add(personIdentifier);
                }
            }
        }
        return personIdentifiers;
    }

    private void validateConflictingPersonIdentifiers(PersonMergeService mergeService, FragmentActionRequest request,
                                                      Person lpiMatch, Person mpiMatch) {
        Map<String, Map<String, String>> conflictingIdentifiers = PersonPropertiesDiffUtil.getConflictingIdentifiers(lpiMatch, mpiMatch);
        for (Map.Entry<String, Map<String, String>> conflictingPairEntry : conflictingIdentifiers.entrySet()) {
            String identifierType = conflictingPairEntry.getKey();
            boolean found = true;
            for (Map.Entry<String, String> conflictingPair : conflictingPairEntry.getValue().entrySet()) {
                String requestParameterName = "conflict-" + conflictingPair.getKey() + "-identifier_" + identifierType;
                String value = request.getParameter(requestParameterName);
                found = found && (value != null);
            }
            if (found) {
                request.globalError("Please resolve " + identifierType + " conflict");
            }
        }
    }

    private void validateConflictingPersonProperty(PersonMergeService mergeService, FragmentActionRequest request,
                                                   Person lpiMatch, Person mpiMatch) {
        Map<String, Map<String, Object>> conflictingProperties = PersonPropertiesDiffUtil.getConflictingProperties(lpiMatch, mpiMatch);
        for (Map.Entry<String, Map<String, Object>> entry : conflictingProperties.entrySet()) {
            String propertyName = entry.getKey();
            boolean found = true;
            for (Map.Entry<String, Object> valueEntry : entry.getValue().entrySet()) {
                String requestParameterName = "conflict-" + valueEntry.getKey() + "-" + propertyName;
                Object value = request.getAttribute(requestParameterName);
                found = found && (value != null);
            }
            if (found) {
                request.globalError("Please resolve " + propertyName + " conflict");
            }
        }
    }
}


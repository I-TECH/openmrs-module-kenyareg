package org.openmrs.module.kenyareg.fragment.controller;

import java.util.List;
import java.util.Map;

import org.go2itech.oecui.data.RequestResult;
import org.openmrs.module.kenyareg.api.PersonMergeService;
import org.openmrs.module.kenyareg.api.RegistryService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentActionRequest;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.ui.framework.session.Session;

import ke.go.moh.oec.Person;

public class PersonEditorFragmentController {

	public void controller(
			@SpringBean("personMergeService") PersonMergeService mergeService,
			FragmentConfiguration config,
			FragmentModel model,
			Session session
		) {
		Person fromLpi = new Person();
		if (config.containsKey("lpiUid")) {
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

		Person fromMpi = new Person();
		if (config.containsKey("mpiUid")) {
			String uuid = config.get("mpiUid").toString();
			@SuppressWarnings("unchecked")
			List<Person> mpiPersonList = (List<Person>) session.getAttribute("lpiResult", RequestResult.class).getData();
			for (Person person : mpiPersonList) {
				if (person.getPersonGuid().equals(uuid)) {
					fromMpi = person;
					break;
				}
			}
		}
		session.setAttribute("lpiMatch", fromLpi);
		session.setAttribute("mpiMatch", fromMpi);
		model.addAttribute("mergedProperties", mergeService.getLpiMpiMergedProperties(fromLpi, fromMpi));
		model.addAttribute("conflictedProperties", mergeService.getLpiMpiConflictingProperties(fromLpi, fromMpi));

	}

	public FragmentActionResult update(
			@SpringBean("personMergeService") PersonMergeService mergeService,
			@SpringBean("registryService") RegistryService registryService,
			@BindParams Person person, FragmentActionRequest request,
			Session session, UiUtils ui) {
		Person lpiMatch = session.getAttribute("lpiMatch", Person.class);
		Person mpiMatch = session.getAttribute("mpiMatch", Person.class);
		Map<String, Map<String, Object>> conflictingProperties = mergeService.getLpiMpiConflictingProperties(lpiMatch, mpiMatch);
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

		if (request.hasErrors()) {
			return new FailureResult(request.getErrors());
		}
		registryService.acceptPerson(person);
		return new SuccessResult("Person details uptdated");
	}

	public void pollPersonIndex(Session session) {

	}

}

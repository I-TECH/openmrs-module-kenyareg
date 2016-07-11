/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyareg.fragment.controller;

import ke.go.moh.oec.Person;
import org.go2itech.oecui.data.RequestResult;
import org.go2itech.oecui.data.RequestResultPair;
import org.go2itech.oecui.data.Server;
import org.openmrs.module.kenyareg.api.RegistryService;
import org.openmrs.module.kenyaui.form.ValidatingCommandObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.session.Session;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicSearchFragmentController {

	private void setDisplayAttributes(int server, Session session) {
		switch (server) {
			case Server.MPI_LPI:
				session.setAttribute("lpiDisplayed", Boolean.FALSE);
				session.setAttribute("mpiDisplayed", Boolean.FALSE);
				break;
			case Server.MPI:
				session.setAttribute("mpiDisplayed", Boolean.FALSE);
				break;
			case Server.LPI:
				session.setAttribute("lpiDisplayed", Boolean.FALSE);
				break;
		}
	}

	public void controller(Session session) {
		session.setAttribute("lpiResult", null);
		session.setAttribute("mpiResult", null);

		session.setAttribute("lpiDisplayed", null);
		session.setAttribute("mpiDisplayed", null);

		session.setAttribute("lastResort", null);
	}

	public RequestResultPair search(@MethodParam("newBasicSearchForm") @BindParams BasicSearchForm form,
	                                @SpringBean("registryService") RegistryService registryService,
	                                @RequestParam("server")Integer serverId,
	                                Session session, UiUtils ui) {
		ui.validate(form, form, null);

		setDisplayAttributes(form.getServer(), session);

		Person query = form.getPerson();
        RequestResultPair resultPair = registryService.findPerson(serverId, query);

		if (serverId == Server.LPI) {
			session.setAttribute("lpiResult", resultPair.getLpiResult());
		}
		if (serverId == Server.MPI) {
			session.setAttribute("mpiResult", resultPair.getMpiResult());
		}
		if (serverId == Server.MPI_LPI) {
			session.setAttribute("lpiResult", resultPair.getLpiResult());
			session.setAttribute("mpiResult", resultPair.getMpiResult());
		}

		return resultPair;
	}

	public Integer accept(@RequestParam(value = "uuid", required = true) String uuid,
	                      @SpringBean("registryService") RegistryService registryService,
	                      Session session) {
		Person fromMpi = null;
		List<Person> personList = (List<Person>) session.getAttribute("lpiResult", RequestResult.class).getData();
		for (Person person : personList) {
			if (person.getPersonGuid().equals(uuid)) {
				fromMpi = person;
				break;
			}
		}
		if (fromMpi == null) {
			return null;
		}
		return registryService.acceptPerson(fromMpi).getId();
	}

	public BasicSearchForm newBasicSearchForm() {
		return new BasicSearchForm();
	}

	public class BasicSearchForm extends ValidatingCommandObject {

		private int server;
		private String surname;
		private String firstName;

		public BasicSearchForm() {
		}

		public int getServer() {
			return server;
		}

		public void setServer(int server) {
			this.server = server;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getSurname() {
			return surname;
		}

		public void setSurname(String surname) {
			this.surname = surname;
		}

		@Override
		public void validate(Object o, Errors errors) {
			//require(errors, "firstName");
		}

		public Person getPerson() {
			Person person = new Person();
			person.setFirstName(firstName);
			person.setLastName(surname);
			return person;
		}
	}
}

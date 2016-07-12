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

import java.util.Date;

public class AdvancedSearchFragmentController {

	public void controller(Session session) {
		session.setAttribute("lpiResult", null);
		session.setAttribute("mpiResult", null);
	}

	public RequestResultPair search(@MethodParam("newBasicSearchForm") @BindParams AdvancedSearchForm form,
	                                @SpringBean("registryService") RegistryService registryService,
	                                Session session, UiUtils ui) {
		ui.validate(form, form, null);

		Person query = form.getPerson();
		RequestResultPair resultPair = registryService.findPerson(form.getServer(), query);

		if (form.getServer() == Server.LPI) {
			session.setAttribute("lpiResult", resultPair.getLpiResult());
		}
		if (form.getServer() == Server.MPI) {
			session.setAttribute("mpiResult", resultPair.getMpiResult());
		}
		if (form.getServer() == Server.MPI_LPI) {
			session.setAttribute("lpiResult", resultPair.getLpiResult());
			session.setAttribute("mpiResult", resultPair.getMpiResult());
		}

		return resultPair;
	}

	public AdvancedSearchForm newBasicSearchForm() {
		return new AdvancedSearchForm();
	}

	public class AdvancedSearchForm extends ValidatingCommandObject {

		private int server;
		private String surname;
		private String firstName;
		private String middleName;
		private String otherName;
		private Date birthDate;
		private String fathersFirstName;
		private String fathersMiddleName;
		private String fathersLastName;
		private String mothersFirstName;
		private String mothersMiddleName;
		private String mothersLastName;
		private String village;

		public AdvancedSearchForm() {
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

		public String getMiddleName() {
			return middleName;
		}

		public void setMiddleName(String middleName) {
			this.middleName = middleName;
		}

		public String getOtherName() {
			return otherName;
		}

		public void setOtherName(String otherName) {
			this.otherName = otherName;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}

		public String getFathersFirstName() {
			return fathersFirstName;
		}

		public void setFathersFirstName(String fathersFirstName) {
			this.fathersFirstName = fathersFirstName;
		}

		public String getFathersMiddleName() {
			return fathersMiddleName;
		}

		public void setFathersMiddleName(String fathersMiddleName) {
			this.fathersMiddleName = fathersMiddleName;
		}

		public String getFathersLastName() {
			return fathersLastName;
		}

		public void setFathersLastName(String fathersLastName) {
			this.fathersLastName = fathersLastName;
		}

		public String getMothersFirstName() {
			return mothersFirstName;
		}

		public void setMothersFirstName(String mothersFirstName) {
			this.mothersFirstName = mothersFirstName;
		}

		public String getMothersMiddleName() {
			return mothersMiddleName;
		}

		public void setMothersMiddleName(String mothersMiddleName) {
			this.mothersMiddleName = mothersMiddleName;
		}

		public String getMothersLastName() {
			return mothersLastName;
		}

		public void setMothersLastName(String mothersLastName) {
			this.mothersLastName = mothersLastName;
		}

		public String getVillage() {
			return village;
		}

		public void setVillage(String village) {
			this.village = village;
		}

		@Override
		public void validate(Object o, Errors errors) {
			requireAny(errors, "surname", "firstName", "middleName");
		}

		public Person getPerson() {
			Person person = new Person();
			person.setFirstName(firstName);
			person.setLastName(surname);
			person.setMiddleName(middleName);
			person.setOtherName(otherName);
			person.setBirthdate(birthDate);
			person.setFathersFirstName(fathersFirstName);
			person.setFathersMiddleName(fathersMiddleName);
			person.setFathersLastName(fathersLastName);
			person.setMothersFirstName(mothersFirstName);
			person.setMothersMiddleName(mothersMiddleName);
			person.setMothersLastName(mothersLastName);
			person.setVillageName(village);
			return person;
		}
	}
}

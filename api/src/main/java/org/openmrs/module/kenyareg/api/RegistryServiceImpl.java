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

package org.openmrs.module.kenyareg.api;

import java.util.Collections;
import java.util.List;

import org.go2itech.oecui.api.RequestDispatcher;
import org.go2itech.oecui.data.RequestResult;
import org.go2itech.oecui.data.RequestResultPair;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.lib.Mediator;

@Service("registryService")
public class RegistryServiceImpl extends BaseOpenmrsService implements RegistryService {

	@Autowired(required = true)
	@Qualifier("patientService")
	PatientService patientService;

	@Autowired(required = true)
	@Qualifier("personService")
	PersonService personService;

	@Autowired(required = true)
	@Qualifier("personMergeService")
	PersonMergeService mergeService;

	@Autowired
	private KenyaEmrService emrService;

	@Override
	public RequestResultPair findPerson(int server, Person person) {
		PersonRequest request = new PersonRequest();
		request.setPerson(person);
		request.setRequestReference(Mediator.generateMessageId());

		RequestResult mpiResult = new RequestResult();
		RequestResult lpiResult = new RequestResult();

		RequestDispatcher.dispatch(request, mpiResult, lpiResult,
				RequestDispatcher.MessageType.FIND, server);

		RequestResultPair resultPair = new RequestResultPair(lpiResult, mpiResult);
		return resultPair;
	}

	@Override
	public Patient acceptPerson(Person fromMpi) {
		org.openmrs.Person fromOmrs = personService.getPersonByUuid(fromMpi.getPersonGuid());
		org.openmrs.Person merged = mergePerson(fromOmrs, fromMpi);
		Patient patient;
		if (merged.isPatient()) {
			patient = (Patient) merged;
		} else {
			patient = new Patient(merged);
		}
		return patientService.savePatient(patient);
	}

	private org.openmrs.Person mergePerson(org.openmrs.Person fromOmrs, Person fromMpi) {
		if (fromOmrs == null) {
			fromOmrs = new org.openmrs.Person();
			fromOmrs.setUuid(fromMpi.getPersonGuid());
		}
		mergeService.mergePerson(fromOmrs, fromMpi);
		Patient patient = null;
		if (fromOmrs.isPatient()) {
			patient = (Patient)fromOmrs;
		} else {
			patient = new Patient(fromOmrs);
		}
		List<PersonIdentifier> personIdentifiers = fromMpi.getPersonIdentifierList();
		if (personIdentifiers == null) {
			personIdentifiers = Collections.emptyList();
		}
		mergeService.mergePatientIdentifiers(patient, personIdentifiers, emrService.getDefaultLocation());;
		PatientIdentifierType openmrsIdType = MetadataUtils.existing(PatientIdentifierType.class, CommonMetadata._PatientIdentifierType.OPENMRS_ID);
		PatientIdentifier openmrsId = patient.getPatientIdentifier(openmrsIdType);

		if (openmrsId == null) {
			String generated = Context.getService(IdentifierSourceService.class).generateIdentifier(openmrsIdType, "Registration");
			openmrsId = new PatientIdentifier(generated, openmrsIdType, emrService.getDefaultLocation());
			patient.addIdentifier(openmrsId);

			if (!patient.getPatientIdentifier().isPreferred()) {
				openmrsId.setPreferred(true);
			}
		}
		return fromOmrs;
	}
}

package org.openmrs.module.kenyareg.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.Person.Sex;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonIdentifier.Type;

public class PersonPropertiesDiffUtilTest {

	@Test public void getMatchingProperties_shouldPreferNonEmptyProperties() {
		Person lpiPerson = new Person();
		lpiPerson.setFirstName("FirstName");
		Date birthDate = new Date();
		lpiPerson.setBirthdate(birthDate);
		Person mpiPerson = new Person();
		Map<String, Object> mergedProperties = PersonPropertiesDiffUtil.getMatchingProperties(lpiPerson, mpiPerson);

		Assert.assertThat(mergedProperties.size(), Matchers.is(2));
		Assert.assertThat(mergedProperties.get("firstName").toString(), Matchers.equalTo("FirstName"));
		Assert.assertThat((Date)mergedProperties.get("birthdate"), Matchers.equalTo(birthDate));
	}

	@Test public void getConflictingProperties_shouldConflictWhenPropertyValuesDoNotMatch() {
		Person lpiPerson = new Person();
		lpiPerson.setFirstName("FirstName");
		lpiPerson.setSex(Sex.F);
		Person mpiPerson = new Person();
		mpiPerson.setFirstName("Conflicting");
		mpiPerson.setSex(Sex.M);
		Map<String, Map<String, Object>> conflictedProperties = PersonPropertiesDiffUtil.getConflictingProperties(lpiPerson, mpiPerson);

		Assert.assertThat(conflictedProperties.size(), Matchers.is(2));
		Assert.assertThat(conflictedProperties.get("firstName").get("lpi").toString(), Matchers.equalTo("FirstName"));
		Assert.assertThat(conflictedProperties.get("firstName").get("mpi").toString(), Matchers.equalTo("Conflicting"));
		Assert.assertThat(conflictedProperties.get("sex").get("lpi").toString(), Matchers.equalTo("F"));
		Assert.assertThat(conflictedProperties.get("sex").get("mpi").toString(), Matchers.equalTo("M"));
	}

	@Test public void getConflictingProperties_shouldReturnEmptyCollectionIfBothLpiAndMpiAreNull() {
		Assert.assertThat(PersonPropertiesDiffUtil.getConflictingProperties(null, null).size(), Matchers.is(0));
	}

	@Test public void getMatchingIdentifiers_shouldMergeIdentifiersIfTheyMatch() throws ParseException {
		Person lpiPerson = getMpiPerson("1-1-1-1", Type.cccLocalId);
		Person mpiPerson = getMpiPerson("1-1-1-1", Type.cccLocalId);
		
		Map<String, String> mergedIdentifiers = PersonPropertiesDiffUtil.getMatchingIdentifiers(lpiPerson, mpiPerson);
		
		Assert.assertThat(mergedIdentifiers.size(), Matchers.is(1));
	}

	@Test public void getConflictingIdentifiers_shouldReturnEmptyCollectionIfIdentifiersMatch() throws ParseException {
		Person lpiPerson = getMpiPerson("1-1-1-1", Type.cccLocalId);
		Person mpiPerson = getMpiPerson("1-1-1-1", Type.cccLocalId);
		
		Map<String, Map<String,String>> mergedIdentifiers = PersonPropertiesDiffUtil.getConflictingIdentifiers(lpiPerson, mpiPerson);
		
		Assert.assertThat(mergedIdentifiers.size(), Matchers.is(0));
	}

	@Test public void getConflictingIdentifiers_shouldNotConflictWhenIdentifierAreOfTypeMpiOrNupi() throws ParseException {
		Person lpiPerson = getMpiPerson("1-1-1-1", Type.nupi);
		Person mpiPerson = getMpiPerson("1-1-1-2", Type.nupi);
		
		Map<String, Map<String,String>> mergedIdentifiers = PersonPropertiesDiffUtil.getConflictingIdentifiers(lpiPerson, mpiPerson);
		
		Assert.assertThat(mergedIdentifiers.size(), Matchers.is(0));
		
		lpiPerson = getMpiPerson("1-1-1-1", Type.masterPatientRegistryId);
		mpiPerson = getMpiPerson("1-1-1-2", Type.masterPatientRegistryId);
		
		mergedIdentifiers = PersonPropertiesDiffUtil.getConflictingIdentifiers(lpiPerson, mpiPerson);
		
		Assert.assertThat(mergedIdentifiers.size(), Matchers.is(0));
	}

	@Test public void getMatchingIdentifiers_shouldPreferMpiIdIfIdentifierTypeIsNupi() throws ParseException {
		Person lpiPerson = getMpiPerson("1-1-1-1", Type.nupi);
		Person mpiPerson = getMpiPerson("1-1-1-2", Type.nupi);
		
		Map<String, String> mergedIdentifiers = PersonPropertiesDiffUtil.getMatchingIdentifiers(lpiPerson, mpiPerson);
		
		Assert.assertThat(mergedIdentifiers.size(), Matchers.is(1));
		Assert.assertThat(mergedIdentifiers.get(Type.nupi.toString()), Matchers.equalTo("1-1-1-2"));
	}

	@Test public void getMatchingIdentifiers_shouldReturnEmptyIfIdentifiersInConflict() throws ParseException {
		Person lpiPerson = getMpiPerson("1-1-1-1", Type.cccLocalId);
		Person mpiPerson = getMpiPerson("1-1-1-2", Type.cccLocalId);
		
		Map<String, String> mergedIdentifiers = PersonPropertiesDiffUtil.getMatchingIdentifiers(lpiPerson, mpiPerson);
		
		Assert.assertThat(mergedIdentifiers.size(), Matchers.is(0));
	}
	
	@Test public void getMatchingIdentifiers_shouldPreferNonEmptyIdentifiers() throws ParseException {
		Person person = getMpiPerson("1-1-1-1", Type.cccLocalId);
		
		Map<String, String> lpiPreferred = PersonPropertiesDiffUtil.getMatchingIdentifiers(person, null);
		
		Assert.assertThat(lpiPreferred.size(), Matchers.is(1));
		Assert.assertThat(lpiPreferred.get(Type.cccLocalId.toString()), Matchers.equalTo("1-1-1-1"));
		
		Map<String, String> mpiPreferred = PersonPropertiesDiffUtil.getMatchingIdentifiers(null, person);
		
		Assert.assertThat(mpiPreferred.size(), Matchers.is(1));
		Assert.assertThat(mpiPreferred.get(Type.cccLocalId.toString()), Matchers.equalTo("1-1-1-1"));
	}
	
	private Person getMpiPerson(String identifier, Type identifierType) throws ParseException {
		PersonIdentifier personIdentifier = new PersonIdentifier();
		personIdentifier.setIdentifier(identifier);
		personIdentifier.setIdentifierType(identifierType);
		Person mpiPerson = new Person();
		mpiPerson.setFirstName("John");
		mpiPerson.setLastName("Junior");
		mpiPerson.setSex(Sex.F);
		mpiPerson.setPersonGuid(UUID.randomUUID().toString());
		mpiPerson.setFathersFirstName("John");
		mpiPerson.setFathersLastName("Senior");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("25/08/1999");
		mpiPerson.setBirthdate(date);
		mpiPerson.setPersonIdentifierList(Arrays.asList(personIdentifier));
		return mpiPerson;
	}
}

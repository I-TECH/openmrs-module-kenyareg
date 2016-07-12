jq(function () {

	/**
	 * Pre-fill basic search form with test data.
	 */
	jq('#surname').val("Kamau");
	jq('#firstName').val("Rose");

	/**
	 * The search result after sending a query to the MPI, LPI or both.
	 */
	var requestResult = null;

	/**
	 * The matching person selected by the user from the LPI, if any.
	 */
	var lpiMatch = null;

	/**
	 * The matching person selected by the user from the MPI, if any.
	 */
	var mpiMatch = null;

	/**
	 * Indicates whether the person index should be contacted
	 */
	var skipPersonIndex = false;

	/*
	 * Indicates current source being viewed
	 */
	var source = null;

	/**
	 * Sets up the basic search form to be submitted via AJAX.
	 */
	var skipPersonIndex = false;

	/*
	 * Indicates current source being viewed
	 */
	var source = null;

	/**
	 * Sets up the basic/advanced search form to be submitted via AJAX.
	 */
	kenyaui.setupAjaxPost('search-form', {
		onSuccess: function (data) {
			requestResult = data;
			if (!lpiMatch && data.lpiResult.successful) {
				source = "lpi";
				showResultBySource('lpi');
				showDetails(0, 'lpi');
			} else if (!mpiMatch && data.mpiResult.successful) {
				source = "mpi";
				showResultBySource('mpi');
				showDetails(0, 'mpi');
			} else if (!lpiMatch && !data.lpiResult.successful) {
				showRetryDialog("The LPI could not be contacted. Retry?", 3);
			} else if (!mpiMatch && !data.mpiResult.successful) {
				showRetryDialog("The MPI could not be contacted. Retry?", 2);
			} else {
				kenyaui.openConfirmDialog(
						{
							"message": "Neither the LPI nor the MPI could be contacted. Retry?",
							"okLabel": "Yes",
							"cancelLabel": "No",
							"okCallback": function(){
								jq("#basic-search-form").submit();
							}
						}
				);
			}
		}
	});

	jq("#search-form").on("submit", function (e) {
		e.preventDefault();
		lpiMatch = null;
		mpiMatch = null;
	})

	jq('#person-index-results-table').on('click', 'tr', function (e) {
		showDetails(this.rowIndex - 1, source);
	});

	function showResults() {
		showResultBySource('lpi');
		showResultBySource('mpi');
	}

	function showResultBySource(source) {
		var list;
		jq("div.results .ke-panel-heading").html(source.toUpperCase() + " Results")
		if (source == 'lpi') {
			list = requestResult.lpiResult;
		} else if (source == 'mpi') {
			list = requestResult.mpiResult;
		}
		jq('#person-index-results-table > tbody').html("");
		if (list.data.length == 0) {
			showEmpty();
		} else {
			for (var i = 0; i < list.data.length; i++) {
				showPerson(i, source);
			}
		}
	}

	function showEmpty() {
		var id = '#person-index-results-table';
		jq(id).append(
			'<tr>' +
				'<td colspan="6">Nothing to show</td>' +
			'</tr>');
	}

	function showPerson(i, source) {
		var person;
		var id = '#person-index-results-table';
		if (source == 'lpi') {
			person = requestResult.lpiResult.data[i];
		} else if (source == 'mpi') {
			person = requestResult.mpiResult.data[i];
		}
		jq(id).append('<tr>' +
		'<td>' + replaceNull(person.matchScore) + '</td>' +
		'<td>' + replaceNull(person.firstName) + '</td>' +
		'<td>' + replaceNull(person.middleName) + '</td>' +
		'<td>' + replaceNull(person.lastName) + '</td>' +
		'<td>' + replaceNull(person.sex) + '</td>' +
		'<td>' + replaceNull(formatDate(person.birthdate)) + '</td>' +
		'</tr>');
	}

	function replaceNull(value) {
		return value ? value : "";
	}

	function showDetails(i, source) {
		var selected;
		if (source == 'lpi') {
			lpiMatch = requestResult.lpiResult.data[i];
			selected = lpiMatch;
		} else if (source == 'mpi') {
			mpiMatch = requestResult.mpiResult.data[i];
			selected = mpiMatch;
		}
		showMatchScore(selected)
		showIdentifierDetails(selected);
		showBasicDetails(selected);
		showStatusDetails(selected);
		showParentDetails(selected);
	}

	function showMatchScore(person, source) {
		jq('#score').html(replaceNull(person.matchScore));
	}

	function showIdentifierDetails(person) {
		jq('#identifier-table > tbody').html("");
		var personIdList = person.personIdentifierList;
		if (personIdList) {
			for (var j = 0; j < personIdList.length; j++) {
				var personId = personIdList[j];
				jq(id).append('<tr>' +
				'<td class="field-label">' + replaceNull(formatIdentifierType(personId.identifierType)) + '</td>' +
				'<td>' + replaceNull(personId.identifier) + '</td>' +
				'</tr>');
			}
		} else {
			jq('#identifier-table').append('<tr><td></td></tr>');
		}
	}

	function showBasicDetails(person) {
		jq('#first-name').html(replaceNull(person.firstName));
		jq('#middle-name').html(replaceNull(person.middleName));
		jq('#last-name').html(replaceNull(person.lastName));
		jq('#birth-date').html(replaceNull(formatDate(person.birthdate)));
		jq('#sex').html(replaceNull(person.sex));
	}

	function showStatusDetails(person) {
		jq('#alive-status').html(replaceNull(person.aliveStatus));
		jq('#marital-status').html(replaceNull(formatMaritalStatus(person.maritalStatus)));
		jq('#last-visit-date').html(replaceNull(person.lastRegularVisit));
	}

	function showParentDetails(person) {
		jq('#father-first-name').html(replaceNull(person.fathersFirstName));
		jq('#father-middle-name').html(replaceNull(person.fathersMiddleName));
		jq('#father-last-name').html(replaceNull(person.fathersLastName));
		jq('#mother-first-name').html(replaceNull(person.mothersFirstName));
		jq('#mother-middle-name').html(replaceNull(person.mothersMiddleName));
		jq('#mother-last-name').html(replaceNull(person.mothersLastName));
	}

	function formatMaritalStatus(status) {
		var formatted = '';
		if (status) {
			switch (status) {
				case 'single':
					formatted = 'Single';
					break;
				case 'marriedPolygamous':
					formatted = 'Married Polygamous';
					break;
				case 'marriedMonogamous':
					formatted = 'Married Monogamous';
					break;
				case 'divorced':
					formatted = 'Divorced';
					break;
				case 'widowed':
					formatted = 'Widowed';
					break;
				case 'cohabitating':
					formatted = 'Cohabitating';
					break;
				default:
					formatted = '';
			}
		}
		return formatted;
	}

	function formatIdentifierType(type) {
		var formatted = '';
		if (type) {
			switch (type) {
				case 'patientRegistryId':
					formatted = 'Patient Registry ID';
					break;
				case 'masterPatientRegistryId':
					formatted = 'Master Patient Registry ID';
					break;
				case 'cccUniqueId':
					formatted = 'CCC Unique ID';
					break;
				case 'cccLocalId':
					formatted = 'CCC Local ID';
					break;
				case 'kisumuHdssId':
					formatted = 'Kisumu HDSS ID';
					break;
				default:
					formatted = '';
			}
		}
		return formatted;
	}

	function formatDate(date) {
		var formatted = '';
		var format = 'dd-MM-yyyy';
		if (date) {
			formatted = new Date(date).toDateString();
		}
		return formatted;
	}

	//selectedMatch was renamed/split to lpiMatch and mpiMatch
	function extractNupi(person) {
		var nupi = null;
		if (selectedMatch.personIdentifierList) {
			jq.each(selectedMatch.personIdentifierList, function (i, id) {
				if (id.identifierType === 'kisumuHdssId') {
					nupi = id.identifier;
				}
			});
		}
		if (!nupi) {
			nupi = selectedMatch.personGuid;
		}
		return nupi;
	}

	function showRetryDialog(message, server) {
		kenyaui.openConfirmDialog(
			{
				"message": message,
				"okLabel": "Yes",
				"cancelLabel": "No",
				"okCallback": function(){
					jq("input[name='server']").val(server)
					jq("#basic-search-form").submit();
				},
				"cancelCallback": function() {
					skipPersonIndex = true;
					jq("#accept-button").click();
				}
			}
		);
	}

	jq('#accept-button').click(function () {
		if (lpiMatch && !mpiMatch && requestResult.mpiResult.successful) {
			showResultBySource('mpi');
			showDetails(0, 'mpi');
			jq("html, body").animate({ scrollTop: 0 }, "slow");
		} else if(mpiMatch && !lpiMatch && requestResult.lpiResult.successful){
			showResultBySource('lpi');
			showDetails(0, 'lpi');
			jq("html, body").animate({ scrollTop: 0 }, "slow");
		} else if (!lpiMatch && !requestResult.lpiResult.successful && !skipPersonIndex) {
			showRetryDialog("The LPI could not be contacted. Retry?", 3);
		} else if (!mpiMatch && !requestResult.mpiResult.successful && !skipPersonIndex) {
			showRetryDialog("The MPI could not be contacted. Retry?", 2);
		} else if ((lpiMatch && mpiMatch) || skipPersonIndex) {
			ui.navigate('kenyareg', 'merge', {lpiUid: (lpiMatch && lpiMatch.personGuid), mpiUid: (mpiMatch && mpiMatch.personGuid)});
		}
	})

	/**
	 * This is the original implementation. It directly extracts the NUPI uses it to retrieve the patient from
	 * KenyaEMR
	 */

	/*jq('#accept-button').click(function () {
		var nupi = extractNupi(selectedMatch);
		jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/kenyareg/basicSearch/accept.action', {uuid: nupi})
			.success(function (data) {
				var patId = data;
				if (patId) {
					ui.navigate('kenyaemr', 'registration/registrationViewPatient', {patientId: patId});
				} else {
					alert('Patient not found in EMR.');
				}
			})
			.error(function (xhr, status, err) {
				alert(err);
			})
	})*/
});
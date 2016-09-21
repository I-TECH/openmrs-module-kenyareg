jq(function () {

    /**
     * Pre-fill basic search form with test data.
     */
    jq('#surname').val("ADHIAMBO");
    jq('#firstName').val("PERES");
    jq('#middleName').val("MAJIWA");


    /**
     * Initialize table to show empty row
     */
    showEmpty();

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

    /**
     * Indicates current source being viewed
     */
    var source = null;

    jq(".search").on("click", function () {
        kenyaui.openLoadingDialog({message: "Searching..."});
    });

    /**
     * Sets up the basic/advanced search form to be submitted via AJAX.
     */
    kenyaui.setupAjaxPost('search-form', {
        onSuccess: function (data) {
            requestResult = data;
            if (!lpiMatch) {
                if (data.lpiResult.successful) {
                    showResultBySource('lpi');
                } else {
                    showFailure('lpi');
                }
            } else if (!mpiMatch) {
                if (data.mpiResult.successful) {
                    showResultBySource('mpi');
                } else {
                    showFailure('mpi');
                }
            }
        },
        always: function () {
            kenyaui.closeDialog();
        }
    });

    jq("#search-form").on("submit", function (e) {
        e.preventDefault();
        lpiMatch = null;
        mpiMatch = null;
    })

    jq('#person-index-results-table').on('click', 'tr', function (e) {
        showDetails(this.rowIndex, source);
    });

    function showResults() {
        showResultBySource('lpi');
        showResultBySource('mpi');
    }

    function showResultBySource(src) {
        var list;
        source = src
        jq("div.results .ke-panel-heading").html(src.toUpperCase() + " Results")
        if (src == 'lpi') {
            list = requestResult.lpiResult;
        } else if (src == 'mpi') {
            list = requestResult.mpiResult;
        }
        jq('#person-index-results-table > tbody').html("");
        if (!list.data) {
            showFailure(src);
        } else if (list.data.length == 0) {
            showEmpty();
        } else {
            for (var i = 0; i < list.data.length; i++) {
                showPerson(i, src);
            }
            jq("#reject-button").html("Reject");
            showDetails(0, src);
        }
    }

    /**
     * Indicate that a connection failure occurred and that the LPI/MPI was not reached.
     */
    function showFailure(src) {
        source = src
        var id = '#person-index-results-table';
        jq(id).empty();
        jq(id).append(
            '<tr>' +
            '<td colspan="6" style="text-align:center; color: red">Not contacted</td>' +
            '</tr>');
    }

    /**
     * Indicate that the LPI/MPI was reached but returned empty results.
     */
    function showEmpty() {
        var id = '#person-index-results-table';
        jq(id).empty();
        jq(id).append(
            '<tr>' +
            '<td colspan="6" style="text-align:center;">No results</td>' +
            '</tr>');
        jq(".detail").hide();
        //Change button label to read 'Continue'
        jq("#reject-button").html("Continue");
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
        return value ? value : "--";
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
        jq(".continue").hide();
        jq(".detail").show('slow');
        showMatchScore(selected)
        showIdentifierDetails(selected);
        showBasicDetails(selected);
        showStatusDetails(selected);
        showParentDetails(selected);
    }

    function showMatchScore(person, source) {
        jq("div.detail .ke-panel-heading").html("Details (Match Score: " + replaceNull(person ? person.matchScore : '') + ")")
    }

    function showIdentifierDetails(person) {
        var personIdList = person ? person.personIdentifierList : null;
        jq(".unique-identifiers").empty();
        if (personIdList) {
            for (var j = 0; j < personIdList.length; j++) {
                var personId = personIdList[j];
                jq(".unique-identifiers")
                    .append('<div>' + replaceNull(formatIdentifierType(personId.identifierType)) + ': '
                    + replaceNull(personId.identifier) + '</div>'
                );
            }
        } else {
            jq('.unique-identifiers').html('No Identifiers');
        }
    }

    function showBasicDetails(person) {
        jq('.first-name').html(replaceNull(person ? person.firstName : ''));
        jq('.middle-name').html(replaceNull(person ? person.middleName : ''));
        jq('.last-name').html(replaceNull(person ? person.lastName : ''));
        jq('.birth-date').html(replaceNull(formatDate(person ? person.birthdate : '')));
        jq('.sex').html(replaceNull(person ? person.sex : ''));
    }

    function showStatusDetails(person) {
        jq('.alive-status').html(replaceNull(person ? person.aliveStatus : ''));
        jq('.marital-status').html(replaceNull(formatMaritalStatus(person ? person.maritalStatus : '')));
        jq('.last-visit-date').html(replaceNull(person ? person.lastRegularVisit : ''));
    }

    function showParentDetails(person) {
        jq('.father-first-name').html(replaceNull(person ? person.fathersFirstName : ''));
        jq('.father-middle-name').html(replaceNull(person ? person.fathersMiddleName : ''));
        jq('.father-last-name').html(replaceNull(person ? person.fathersLastName : ''));
        jq('.mother-first-name').html(replaceNull(person ? person.mothersFirstName : ''));
        jq('.mother-middle-name').html(replaceNull(person ? person.mothersMiddleName : ''));
        jq('.mother-last-name').html(replaceNull(person ? person.mothersLastName : ''));
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
                    formatted = 'MPI ID';
                    break;
                case 'cccUniqueId':
                    formatted = 'UPN';
                    break;
                case 'cccLocalId':
                    formatted = 'Clinic ID';
                    break;
                case 'kisumuHdssId':
                    formatted = 'Kisumu HDSS ID';
                    break;
                case 'nupi':
                    formatted = 'NUPI';
                    break;
                case 'telNo':
                    formatted = 'Tel No';
                    break;
                case 'parentTelNo':
                    formatted = 'Parent Tel No';
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
                "okCallback": function () {
                    jq("input[name='server']").val(server)
                    jq("#basic-search-form").submit();
                },
                "cancelCallback": function () {
                    skipPersonIndex = true;
                    jq("#accept-button").click();
                }
            }
        );
    }

    jq('#accept-button').click(function () {
        if (source == 'lpi') {
            if (!mpiMatch) {
                showResultBySource('mpi');
                jq("html, body").animate({scrollTop: 0}, "slow");
            } else {
                ui.navigate('kenyareg', 'merge', {
                    lpiUid: (lpiMatch && lpiMatch.personGuid),
                    mpiUid: (mpiMatch && mpiMatch.personGuid)
                });
            }
        } else if (source == 'mpi') {
            ui.navigate('kenyareg', 'merge', {
                lpiUid: (lpiMatch && lpiMatch.personGuid),
                mpiUid: (mpiMatch && mpiMatch.personGuid)
            });
        }
    })

    /**
     * If LPI results are rejected show MPI results. If MPI results are rejected also go create a new patient.
     */
    jq('#reject-button').on("click", function (event) {
        if (source == 'lpi') {
            lpiMatch = null;
            if (requestResult.mpiResult.successful) {
                showResultBySource('mpi');
                jq("html, body").animate({scrollTop: 0}, "slow");
            } else {
                showFailure('mpi');
            }
        } else if (source == 'mpi') {
            mpiMatch = null;
            if (lpiMatch) {
                ui.navigate('kenyareg', 'merge', {
                    lpiUid: lpiMatch.personGuid,
                    mpiUid: null
                });
            } else {
                ui.navigate('kenyareg', 'merge', {
                    lpiUid: null,
                    mpiUid: null
                });
            }
        }
    });
});
<%
    ui.decorateWith("kenyaui", "panel", [heading: "Edit Person Details"])

    ui.includeCss("kenyareg", "kenyareg.css")

    def initializeFields = { fieldCollection, mergedValues ->
        fieldCollection.each { field ->
            def property = mergedValues.find { key, value ->
                key == field.id
            }
            if (property) {
                if (field.config?.options) {
                    field.initialValue = property.value.toString()
                } else {
                    field.initialValue = property.value
                }
            }
        }
    }

    def removeFieldsInConflict = { fieldCollection, valuesInConflict ->
        def mergedFields = fieldCollection.findAll { field ->
            def isInConflict = valuesInConflict.find { key, value ->
                return field.id == key
            }
            return !isInConflict
        }
        return mergedFields
    }

    def getFormattedIdentifierType = { identifierType ->
        def formatted = '';
        switch (identifierType) {
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
        return formatted;
    }

    def identifierFieldDefinitions = [
        [ formFieldName: "cccLocalId", label : "Clinic ID", class : java.lang.String, id : "cccLocalId" ],
        [ formFieldName: "telNo", label : "Phone Number", class : java.lang.String, id : "telNo" ],
        [ formFieldName: "parentTelNo", label : "Parent Phone Number", class : java.lang.String, id : "parentTelNo" ],
        [ formFieldName: "cccUniqueId", label : "UPN", class : java.lang.String, id : "cccUniqueId" ]
    ]

    def nameFieldDefinitions = [
        [ formFieldName: "lastName", label : "Surname", class : java.lang.String, id : "lastName" ],
        [ formFieldName: "firstName", label : "First Name", class : java.lang.String, id : "firstName" ],
        [ formFieldName: "middleName", label : "Middle Name", class : java.lang.String, id : "middleName" ],
        [ formFieldName: "otherName", label : "Other Name(s)", class : java.lang.String, id : "otherName" ],
    ]
    
    def otherDemoFieldDefinitions = [
        [ formFieldName: "birthdate", label : "Birth Date", class : java.util.Date, id : "birthdate" ],
        [ formFieldName: "sex", label : "Sex", class : java.lang.String, id : "sex",
            config : [
                options : [
                    [ value: 'M', label: 'Male' ],
                    [ value: 'F', label: 'Female' ]
                ]
            ]
        ],
        [ formFieldName: "maritalStatus", label : "Marital Status", class : java.lang.String, id : "maritalStatus",
            config : [
                options  : [
                    [ value: 'single', label: 'Single' ],
                    [ value: 'marriedPolygamous', label: 'Married Polygamous' ],
                    [ value: 'marriedMonogamous', label: 'Married Monogamous' ],
                    [ value: 'divorced', label: 'Divorced' ],
                    [ value: 'widowed', label: 'Widowed' ],
                    [ value: 'cohabitating', label: 'Cohabitating' ]
                ]
            ]
        ],
    ]

    def fatherFieldDefinitions = [
        [ formFieldName: "fathersFirstName", label : "Father's First Name", class : java.lang.String, id : "fathersFirstName" ],
        [ formFieldName: "fathersMiddleName", label : "Father's Middle Name", class : java.lang.String, id : "fathersMiddleName" ],
        [ formFieldName: "fathersLastName", label : "Father's Last Name", class : java.lang.String, id : "fathersLastName" ]
    ]

    def motherFieldDefinitions = [
        [ formFieldName: "mothersFirstName", label : "Mother's First Name", class : java.lang.String, id : "mothersFirstName" ],
        [ formFieldName: "mothersMiddleName", label : "Mother's Middle Name", class : java.lang.String, id : "mothersMiddleName" ],
        [ formFieldName: "mothersLastName", label : "Mother's Last Name", class : java.lang.String, id : "mothersLastName" ]
    ]

    initializeFields(identifierFieldDefinitions, mergedIdentifiers)
    initializeFields(nameFieldDefinitions, mergedProperties)
    initializeFields(otherDemoFieldDefinitions, mergedProperties)
    initializeFields(fatherFieldDefinitions, mergedProperties)
    initializeFields(motherFieldDefinitions, mergedProperties)

    def mergedIdentifierFields = removeFieldsInConflict(identifierFieldDefinitions, conflictingIdentifiers);
    def mergedNameFields = removeFieldsInConflict(nameFieldDefinitions, conflictedProperties);
    def mergedOtherDemoFields = removeFieldsInConflict(otherDemoFieldDefinitions, conflictedProperties);
    def mergedFatherFields = removeFieldsInConflict(fatherFieldDefinitions, conflictedProperties);
    def mergedMotherFields = removeFieldsInConflict(motherFieldDefinitions, conflictedProperties);

    identifierFields = mergedIdentifierFields.each { it.formFieldName = "identifier_" + it.formFieldName }.collect { [it] }
    nameFields = [ mergedNameFields ]
    otherDemoFields = mergedOtherDemoFields.collect { [it] }
    parentFields = [ mergedFatherFields, mergedMotherFields ]

    def getConflictingFields = { fieldDefinitions, valuesInConflict ->
        def conflictingFields = []
        valuesInConflict.each { property, lpiMpiValue ->
            def conflictedPair = []
            def definition = fieldDefinitions.find { it.id == property }
            if (definition) {
                lpiMpiValue.each { source, value ->
                    def lpiMpi = [:]
                    definition.each { defKey, defValue ->
                        lpiMpi.put(defKey, defValue)
                    }
                    lpiMpi.initialValue = value
                    def resolveInputName = lpiMpi.formFieldName
                    lpiMpi.formFieldName = "conflict-${source}-${lpiMpi.formFieldName}"
                    lpiMpi.label += " (${source.toUpperCase()})" +
                      "<input name=\'resolve-${resolveInputName}\' data-field-name=\'${lpiMpi.formFieldName}\' type=\'radio\' class=\'resolve\'>"
                    conflictedPair.push(lpiMpi)
                }
                conflictingFields.push(conflictedPair)
            }
        }
        return conflictingFields;
    }

    propertiesInConflict = getConflictingFields(nameFieldDefinitions + otherDemoFieldDefinitions + fatherFieldDefinitions + motherFieldDefinitions, conflictedProperties);
    identifiersInConflict = getConflictingFields(identifierFieldDefinitions, conflictingIdentifiers)

    def personGuid = "";
    if (mpiUid) { personGuid = mpiUid }
    else if (lpiUid) { personGui = mpiUid }
%>
<% if (!propertiesInConflict.empty || !identifiersInConflict.empty) { %>
<div>
    <p>NOTE: Some conflicts occurred while trying to merge MPI and LPI person details. Please resolve by selecting the preferred property</p>
</div>
<% } %>
<form id="person-editor-form" method="post" action="${ui.actionLink("kenyareg", "personEditor", "update")}">
    <input type="hidden" name="personGuid" value="${personGuid}" >
    <div class="ke-panel-content">
        <div class="ke-form-globalerrors" style="display: none"></div>
        <fieldset>
            <legend>Identifiers</legend>
            <% identifierFields.each { %>
                ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
            <% mergedIdentifiers.each { idType, id ->
                 if (idType.equals("nupi") || idType.equals("masterPatientRegistryId")) { 
                   def label = getFormattedIdentifierType(idType) %>
                   <p>
                     ${label}: ${id}
                     <input type="hidden" name="identifier_${idType}" value="${id}" >
                   </p>
            <%   }
               } %>
        </fieldset>
        <fieldset>
            <legend>Demographics</legend>
            <% nameFields.each { %>
                ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
            <% otherDemoFields.each { %>
                ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
        </fieldset>
        <fieldset>
            <legend>Parent Information</legend>
            <% parentFields.each { %>
                ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
        </fieldset>
        <% if (!propertiesInConflict.empty || !identifiersInConflict.empty) { %>
            <fieldset>
                <legend>Conflicts</legend>
                <table>
                    <tbody>
                    <% identifiersInConflict.each { conflictingPair -> %>
                        <tr>
                        <% conflictingPair.each { field -> %>
                            <td>${ui.includeFragment("kenyaui", "widget/labeledField", field)}</td>
                        <% } %>
                        </tr>
                    <% } %>
                    <% propertiesInConflict.each { conflictingPair -> %>
                        <tr>
                        <% conflictingPair.each { field -> %>
                            <td>${ui.includeFragment("kenyaui", "widget/labeledField", field)}</td>
                        <% } %>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </fieldset>
        <% } %>
    </div>
    <div class="ke-panel-footer">
        <button id="save-button" class="button"><img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> Save</button>
        <button id="cancel-button" class="button"><img src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>
    </div>
</form>
<script>
  jQuery(function(){
    jQuery(".resolve").on("change", function(){
      var fieldName = jQuery(this).data("fieldName");
      var resolvedFieldName = fieldName.substring(fieldName.lastIndexOf("-") + 1);
      console.log(resolvedFieldName);
      console.log(jQuery("input[name=" + resolvedFieldName + "],select[name=" + resolvedFieldName + "]"));
      if (jQuery(this).is(":checked") && fieldName.indexOf("lpi") > 0) {
        var mpiFieldName = fieldName.replace(/lpi/i, "mpi");
        jQuery("input[name=" + resolvedFieldName + "],select[name=" + resolvedFieldName + "]")
          .attr("name", mpiFieldName);
        jQuery("input[name=" + fieldName + "],select[name=" + fieldName + "]")
          .attr("name", resolvedFieldName);
      }
      else if (jQuery(this).is(":checked") && fieldName.indexOf("mpi") > 0) {
        var lpiFieldName = fieldName.replace(/mpi/i, "lpi");
        jQuery("input[name=" + resolvedFieldName + "],select[name=" + resolvedFieldName + "]")
          .attr("name", lpiFieldName);
        jQuery("input[name=" + fieldName + "],select[name=" + fieldName + "]")
          .attr("name", resolvedFieldName);
      }
    });

    kenyaui.setupAjaxPost('person-editor-form', {
        onSuccess: function (data) {
            kenyaui.notifySuccess("Person details saved!");
            ui.navigate("kenyaemr/registration", "registrationViewPatient", { "patientId": data.patientId });
        }
    });
  });
</script>
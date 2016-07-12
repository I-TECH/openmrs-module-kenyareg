<%
    ui.decorateWith("kenyaui", "panel", [heading: "Advanced Search"])

    ui.includeJavascript("kenyareg", "kenyareg.js")

    def fields =
            [
                    [
                            [
                                    formFieldName: "surname",
                                    label        : "Surname",
                                    class        : java.lang.String,
                                    id           : "surname"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "firstName",
                                    label        : "First Name",
                                    class        : java.lang.String,
                                    id           : "firstName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "middleName",
                                    label        : "Middle Name",
                                    class        : java.lang.String,
                                    id           : "middleName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "otherName",
                                    label        : "Other Name",
                                    class        : java.lang.String,
                                    id           : "otherName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "birthDate",
                                    label        : "Birth Date",
                                    class        : java.util.Date,
                                    id           : "birthDate"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "fathersFirstName",
                                    label        : "Father's First Name",
                                    class        : java.lang.String,
                                    id           : "fathersFirstName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "fathersMiddleName",
                                    label        : "Father's Middle Name",
                                    class        : java.lang.String,
                                    id           : "fathersMiddleName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "fathersLastName",
                                    label        : "Father's Last Name",
                                    class        : java.lang.String,
                                    id           : "fathersLastName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "mothersFirstName",
                                    label        : "Mother's First Name",
                                    class        : java.lang.String,
                                    id           : "mothersFirstName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "mothersMiddleName",
                                    label        : "Mother's Middle Name",
                                    class        : java.lang.String,
                                    id           : "mothersMiddleName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "mothersLastName",
                                    label        : "Mother's Last Name",
                                    class        : java.lang.String,
                                    id           : "mothersLastName"
                            ]
                    ],
                    [
                            [
                                    formFieldName: "village",
                                    label        : "Village",
                                    class        : java.lang.String,
                                    id           : "village"
                            ]
                    ],
                    [
                            [
                                hiddenInputName: "server",
                                value: 1
                            ]
                    ]
            ]
%>

<form id="search-form" method="post" action="${ui.actionLink("kenyareg", "advancedSearch", "search")}">
    <div class="ke-form-globalerrors" style="display: none"></div>
    <fieldset>
        <% fields.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>
    </fieldset>
    <div class="ke-form-footer">
        <button class="search" type="submit"><img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> Search</button>
    </div>
</form>
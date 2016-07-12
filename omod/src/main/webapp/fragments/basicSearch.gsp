<%
    ui.decorateWith("kenyaui", "panel", [heading: "Basic Search"])

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
                                hiddenInputName: "server",
                                value: 1
                            ]
                    ]
            ]
%>

<form id="search-form" method="post" action="${ui.actionLink("kenyareg", "basicSearch", "search")}">
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
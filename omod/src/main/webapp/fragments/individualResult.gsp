<%
    ui.decorateWith("kenyaui", "panel", [heading: "Details"])

    ui.includeCss("kenyareg", "kenyareg.css")
%>

<script type="application/javascript">
    jq(function () {
        jq.getJSON('${ui.actionLink("kenyareg", "basicSearch", "search")}', function (result) {
            jq('#results').html(result);
        });
    });
</script>

<div id="details">

    <table>
        <tbody>
        <tr>
            <td>
                <table id="score-table" class="details-table">
                    <thead>
                    <tr>
                        <th class="centered">Match Score</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td id="score" class="centered" style="text-align: center"></td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="identifier-table" class="details-table" style="table-layout: fixed">
                    <thead>
                    <tr>
                        <th colspan="2" class="centered">Unique Identifiers</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td></td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="basic-info-table" class="details-table" style="table-layout: fixed">
                    <thead>
                    <tr>
                        <th class="centered" colspan="2">Personal Information</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="field-label">First Name:</td>
                        <td id="first-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Middle Name:</td>
                        <td id="middle-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Last Name:</td>
                        <td id="last-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Birth Date:</td>
                        <td id="birth-date"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Sex:</td>
                        <td id="sex"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Alive:</td>
                        <td id="alive-status"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Marital Status:</td>
                        <td id="marital-status"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Last Visit Date:</td>
                        <td id="last-visit-date"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Father's First Name:</td>
                        <td id="father-first-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Father's Middle Name:</td>
                        <td id="father-middle-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Father's Last Name:</td>
                        <td id="father-last-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Mother's First Name:</td>
                        <td id="mother-first-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Mother's Middle Name:</td>
                        <td id="mother-middle-name"></td>
                    </tr>
                    <tr>
                        <td class="field-label">Mother's Last Name:</td>
                        <td id="mother-last-name"></td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>

    <br>
    <button id="accept-button" class="button">Accept</button>
</div>
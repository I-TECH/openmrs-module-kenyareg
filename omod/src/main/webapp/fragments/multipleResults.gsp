<link rel="stylesheet" type="text/css" href="../resources/styles/kenyareg.css">

<%
    ui.decorateWith("kenyaui", "panel", [heading: "LPI Results"])

    ui.includeCss("kenyareg", "kenyareg.css")
%>

<div id="results2">
    <table id="person-index-results-table" class="results-table">
        <thead>
        <tr>
            <th>Match Score</th>
            <th>First Name</th>
            <th>Middle Name</th>
            <th>Last Name</th>
            <th>Sex</th>
            <th>Birth Date</th>
        </tr>
        </thead>
        <tbody>
        </tbody>
    </table>

    <br>
    <button id="reject-button" class="button">Reject</button>
</div>



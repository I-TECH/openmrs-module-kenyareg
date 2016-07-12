<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", href: ui.pageLink("kenyareg", "registryHome")]
    ]
%>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}

    ${ui.includeFragment("kenyareg", "advancedSearch")}
</div>

<div class="ke-page-content">
    <div>${ui.includeFragment("kenyareg", "status")}</div>
    <div class="results">
        ${ui.includeFragment("kenyareg", "multipleResults")}
    </div>
    <div class="detail">
        ${ui.includeFragment("kenyareg", "individualResult")}
    </div>
</div>
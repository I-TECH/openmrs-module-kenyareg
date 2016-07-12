<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Fingerprint", iconProvider: "kenyareg", icon: "buttons/fingerprint.png", href: ui.pageLink("kenyareg", "registryHome")],
            [label: "Identifier", iconProvider: "kenyareg", icon: "buttons/identifier.png", href: ui.pageLink("kenyareg", "registryHome")],
            [label: "Basic", iconProvider: "kenyareg", icon: "buttons/basic.png", href: ui.pageLink("kenyareg", "basicHome")],
            [label: "Advanced", iconProvider: "kenyareg", icon: "buttons/advanced.png", href: ui.pageLink("kenyareg", "advancedHome")]
    ]
%>
<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Search Type", items: menuItems])}
</div>
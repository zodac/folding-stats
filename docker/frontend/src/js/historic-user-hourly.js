const ROOT_URL='http://internal.axihub.ca/folding';

function populateUserDropdown() {
    var dropdown = document.getElementById('user_dropdown');
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    fetch(ROOT_URL+'/users')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        userDropdownDiv = document.getElementById("user_dropdown");

        jsonResponse.forEach(function(userItem, i){
            userButton = document.createElement("button");
            userButton.setAttribute("class", "dropdown-item");
            userButton.setAttribute("type", "button");
            userButton.setAttribute("onclick", "getUserHistoricStats("+userItem["id"]+",'"+userItem["displayName"]+"')");
            userButton.innerHTML = userItem["displayName"];

            userDropdownDiv.append(userButton);
        });
    });
}

function getUserHistoricStats(userId, userName) {
    show("loader");
    hide("historic_stats");

    var currentDate = new Date();
    var year = currentDate.getFullYear();
    var month = (currentDate.getMonth()+1);
    var monthName = currentDate.toLocaleString('default', { month: 'long' });
    var dayOfMonth = currentDate.getDate();

    fetch(ROOT_URL+'/historic/users/' + userId + '/' + year + '/' + month + '/' + dayOfMonth)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        dropDownTitle = document.getElementById("user_dropdown_root");
        dropDownTitle.innerHTML = userName;

        // Clear existing entries in div
        historicDiv = document.getElementById("historic_stats");
        while (historicDiv.firstChild) {
            historicDiv.removeChild(historicDiv.lastChild);
        }

        userTitle = document.createElement("h1");
        userTitle.setAttribute("class", "navbar-brand");
        userTitle.innerHTML = userName + " ("+ordinalSuffixOf(dayOfMonth)+" "+monthName+" "+year+")";
        historicDiv.append(userTitle);

        const headers = ["Hour", "Points", "Units"];
        historicTable = document.createElement('table');
        historicTable.setAttribute("id", "historic_table");
        historicTable.setAttribute("class", "table table-dark table-striped table-hover");

        tableHead = document.createElement('thead');
        tableHeaderRow = document.createElement('tr');
        headers.forEach(function (header, i) {
            tableHeader = document.createElement("th");
            tableHeader.setAttribute("onclick", "sortTable("+i+", 'historic_table')");
            tableHeader.setAttribute("scope", "col");
            tableHeader.innerHTML = header;

            tableHeaderRow.append(tableHeader);
        });
        tableHead.append(tableHeaderRow);
        historicTable.append(tableHead);


        tableBody = document.createElement("tbody");
        jsonResponse.forEach(function(statsEntry, i){
            tableRow = document.createElement("tr");

            dateCell = document.createElement("td");
            dateCell.innerHTML = leftPad(statsEntry["dateTime"]["time"]["hour"], 2, '0') + ":00";
            tableRow.append(dateCell);

            pointsCell = document.createElement("td");
            pointsCell.setAttribute("data-bs-toggle", "tooltip");
            pointsCell.setAttribute("data-placement", "top");
            pointsCell.setAttribute("title", "Unmultiplied: " + statsEntry["points"].toLocaleString());
            pointsCell.innerHTML = statsEntry["multipliedPoints"].toLocaleString();
            new bootstrap.Tooltip(pointsCell);
            tableRow.append(pointsCell);

            unitsCell = document.createElement("td");
            unitsCell.innerHTML = statsEntry["units"].toLocaleString();
            tableRow.append(unitsCell);
            tableBody.append(tableRow);
        });

        historicTable.append(tableBody);
        historicDiv.append(historicTable);

        hide("loader");
        show("historic_stats");
    });
}

document.addEventListener("DOMContentLoaded", function(event) {
    populateUserDropdown();
    updateTimer();
});
const ROOT_URL="%ROOT_URL%";

var selectedUserId = 0;
var selectedUser = "";
var selectedYear = new Date().getUTCFullYear();

function getUserHistoricStats(userId, userName, day, month, monthName, year) {
    if(userId != 0){
        selectedUserId = userId;
    }

    if(userName != null){
        selectedUser = userName;
        userDropdownTitle = document.getElementById("user_dropdown_root");
        userDropdownTitle.innerHTML = selectedUser;
    }

    if (year != null) {
        selectedYear = year;
        yearDropdownTitle = document.getElementById("year_dropdown_root");
        yearDropdownTitle.innerHTML = selectedYear;
    }

    if(selectedUser === "" || selectedUserId === 0){
        return;
    }

    show("loader");
    hide("historic_stats");

    fetch(ROOT_URL+"/historic/users/" + selectedUserId + "/" + selectedYear)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        // Clear existing entries in div
        historicDiv = document.getElementById("historic_stats");
        while (historicDiv.firstChild) {
            historicDiv.removeChild(historicDiv.lastChild);
        }

        const headers = ["Month", "Points", "Units"];
        historicTable = document.createElement("table");
        historicTable.setAttribute("id", "historic_table");
        historicTable.setAttribute("class", "table table-dark table-striped table-hover");

        tableHead = document.createElement("thead");
        tableHeaderRow = document.createElement("tr");
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
            dateCell.innerHTML = new Date(year, (statsEntry['dateTime']['date']['month']-1), "01").toLocaleString("default", { month: "long" });
            tableRow.append(dateCell);

            pointsCell = document.createElement("td");
            pointsCell.setAttribute("data-bs-toggle", "tooltip");
            pointsCell.setAttribute("data-placement", "top");
            pointsCell.setAttribute("title", "Unmultiplied: " + statsEntry['points'].toLocaleString());
            pointsCell.innerHTML = statsEntry['multipliedPoints'].toLocaleString();
            new bootstrap.Tooltip(pointsCell);
            tableRow.append(pointsCell);

            unitsCell = document.createElement("td");
            unitsCell.innerHTML = statsEntry['units'].toLocaleString();
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
    populateUserDropdown("user_dropdown");
    populateYearDropdown("year_dropdown", "getUserHistoricStats");
    updateTimer();
});

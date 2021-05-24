const ROOT_URL='http://internal.axihub.ca/folding';

var selectedUserId = 0;
var selectedUser = "";
var selectedYear = new Date().getUTCFullYear();

function populateTeamDropdown() {
    var dropdown = document.getElementById('team_dropdown');
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    fetch(ROOT_URL+'/teams')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        teamDropDownDiv = document.getElementById("team_dropdown");

        jsonResponse.forEach(function(teamItem, i){
            teamButton = document.createElement("button");
            teamButton.setAttribute("class", "dropdown-item");
            teamButton.setAttribute("type", "button");
            teamButton.setAttribute("onclick", "getTeamHistoricStats("+teamItem["id"]+",'"+teamItem["teamName"]+"')");
            teamButton.innerHTML = teamItem["teamName"];

            teamDropDownDiv.append(teamButton);
        });
    });
}

function getTeamHistoricStats(teamId, teamName, day, month, monthName, year) {
    if(teamId != 0){
        selectedTeamId = teamId;
    }

    if(teamName != null){
        selectedTeam = teamName;
        teamDropdownTitle = document.getElementById("team_dropdown_root");
        teamDropdownTitle.innerHTML = selectedTeam;
    }

    if (month != null){
        selectedMonth = month;
    }

    if (monthName != null) {
        selectedMonthName = monthName;
    }

    if (year != null) {
        selectedYear = year;
        yearDropdownTitle = document.getElementById("year_dropdown_root");
        yearDropdownTitle.innerHTML = selectedYear;
    }

    if(selectedTeam === "" || selectedTeamId === 0){
        return;
    }

    show("loader");
    hide("historic_stats");

    fetch(ROOT_URL+'/historic/teams/' + selectedTeamId + '/' + selectedYear)
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
            dateCell.innerHTML = new Date(year, (statsEntry["dateTime"]["date"]["month"]-1), "01").toLocaleString('default', { month: 'long' });
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
    populateTeamDropdown("team_dropdown");
    populateYearDropdown("year_dropdown", "getTeamHistoricStats");
    updateTimer();
});
const ROOT_URL='http://internal.axihub.ca/folding';

var currentDate = new Date();
var currentUtcDate = new Date(currentDate.getUTCFullYear(), currentDate.getUTCMonth(), currentDate.getUTCDate(), currentDate.getUTCHours(), currentDate.getUTCMinutes(), currentDate.getUTCSeconds());

var selectedUserId = 0;
var selectedUser = "";
var selectedMonth = (currentUtcDate.getMonth()+1);
var selectedYear = currentUtcDate.getFullYear();
var selectedMonthName = new Date(selectedYear, (selectedMonth-1), 1).toLocaleString('default', { month: 'long' });
var selectedDay = currentDate.getUTCDate();

function getUserHistoricStats(userId, userName, day, month, monthName, year) {
    if(userId != 0){
        selectedUserId = userId;
    }

    if(userName != null){
        selectedUser = userName;
        userDropdownTitle = document.getElementById("user_dropdown_root");
        userDropdownTitle.innerHTML = selectedUser;
    }

    if(day != null) {
        selectedDay = day;
        dayDropdownTitle = document.getElementById("day_dropdown_root");
        dayDropdownTitle.innerHTML = ordinalSuffixOf(selectedDay);
    }

    if (month != null){
        selectedMonth = month;
    }

    if (monthName != null) {
        selectedMonthName = monthName;
        monthDropdownTitle = document.getElementById("month_dropdown_root");
        monthDropdownTitle.innerHTML = selectedMonthName;
    }

    if (year != null) {
        selectedYear = year;
        yearDropdownTitle = document.getElementById("year_dropdown_root");
        yearDropdownTitle.innerHTML = selectedYear;
    }

    if(selectedDay != "" && selectedDay != null){
        populateDayDropdown(selectedMonth, selectedYear, "day_dropdown", "getUserHistoricStats");
    }

    if(selectedUser === "" || selectedUserId === 0){
        return;
    }

    show("loader");
    hide("historic_stats");

    fetch(ROOT_URL+'/historic/users/' + selectedUserId + '/' + selectedYear + '/' + selectedMonth + '/' + selectedDay)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        // Clear existing entries in div
        historicDiv = document.getElementById("historic_stats");
        while (historicDiv.firstChild) {
            historicDiv.removeChild(historicDiv.lastChild);
        }

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
    populateUserDropdown("user_dropdown");
    populateDayDropdown(selectedMonth, selectedYear, "day_dropdown", "getUserHistoricStats");
    populateMonthDropdown("month_dropdown", "getUserHistoricStats");
    populateYearDropdown("year_dropdown", "getUserHistoricStats");
    updateTimer();
});
// https://stackoverflow.com/a/13627586
function ordinalSuffixOf(i) {
    var j = i % 10,
        k = i % 100;
    if (j == 1 && k != 11) {
        return i + "st";
    }
    if (j == 2 && k != 12) {
        return i + "nd";
    }
    if (j == 3 && k != 13) {
        return i + "rd";
    }
    return i + "th";
}

function populateDropdown() {
    let dropdown = $('#user_dropdown');
    dropdown.empty();

    fetch(ROOT_URL+'/users')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        dropdown_div = document.getElementById("user_dropdown");

        $.each(jsonResponse, function(i, userItem) {
            userButton = document.createElement("button");
            userButton.setAttribute("class", "dropdown-item");
            userButton.setAttribute("type", "button");
            userButton.setAttribute("onclick", "getUserHistoricStats("+userItem["id"]+",'"+userItem["displayName"]+"')");
            userButton.innerHTML = userItem["displayName"];

            dropdown_div.append(userButton);
        });
    });
}

function getUserHistoricStats(userId, userName) {
    $("#loader").show();
    $("#historic_stats").hide();
    var currentDate = new Date();
    var year = currentDate.getFullYear();
    var month = (currentDate.getMonth()+1);
    var monthName = currentDate.toLocaleString('default', { month: 'long' });

    fetch(ROOT_URL+'/historic/users/' + userId + '/' + year + '/' + month)
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
        userTitle.innerHTML = userName + " ("+monthName+" "+year+")";
        historicDiv.append(userTitle);

        const headers = ["Date", "Points", "Units"];
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
            dateCell.innerHTML = ordinalSuffixOf(statsEntry["date"]["day"]);
            tableRow.append(dateCell);


            pointsCell = document.createElement("td");
            pointsCell.setAttribute("data-toggle", "tooltip");
            pointsCell.setAttribute("data-placement", "top");
            pointsCell.setAttribute("title", "Unmultiplied: " + statsEntry["points"].toLocaleString());
            pointsCell.innerHTML = statsEntry["multipliedPoints"].toLocaleString();
            tableRow.append(pointsCell);

            unitsCell = document.createElement("td");
            unitsCell.innerHTML = statsEntry["units"].toLocaleString();
            tableRow.append(unitsCell);
            tableBody.append(tableRow);
        });

        historicTable.append(tableBody);

        historicDiv.append(historicTable);

        $('[data-toggle="tooltip"]').tooltip();
        $("#loader").hide();
        $("#historic_stats").show();
    });
}

$(document).ready(function() {
    populateDropdown();
    updateTime();
});
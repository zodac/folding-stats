const ROOT_URL='http://internal.axihub.ca/folding';

// The 'toggle' functions below simply change the colour of the buttons. There must be a smarter way to do this...
function toggleMainButtonStyle(type, classList){
    var button = document.getElementById(type+"_button");

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary");
        button.classList.remove("btn-success");
    } else {
        button.classList.add("btn-success");
        button.classList.remove("btn-primary");
    }
}

function loadHardware() {
    fetch(ROOT_URL+'/hardware')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        // Build hardware
        jsonResponse.sort(sortJsonByKey("id"));
        const hardwareHeaders = ["ID", "Name", "Operating System", "Multiplier"];
        const hardwareProperties = ["id", "displayName", "operatingSystem", "multiplier"];

        hardwareDiv = document.getElementById("hardware_div");

        hardwareTitle = document.createElement('h2');
        hardwareTitle.setAttribute("class", "navbar-brand");
        hardwareTitle.innerHTML = "Hardware";
        hardwareDiv.append(hardwareTitle);

        hardwareTable = document.createElement('table');
        hardwareTable.setAttribute("id", "hardware");
        hardwareTable.setAttribute("class", "table table-dark table-striped table-hover");

        hardwareTableHead = document.createElement('thead');
        hardwareTableHeaderRow = document.createElement('tr');
        hardwareHeaders.forEach(function (header, i) {
            hardwareTableHeader = document.createElement("th");
            hardwareTableHeader.setAttribute("onclick", "sortTable("+i+", 'hardware')");
            hardwareTableHeader.setAttribute("scope", "col");
            hardwareTableHeader.innerHTML = header;

            hardwareTableHeaderRow.append(hardwareTableHeader);
        });
        hardwareTableHead.append(hardwareTableHeaderRow);
        hardwareTable.append(hardwareTableHead);


        hardwareTableBody = document.createElement('tbody');

        jsonResponse.forEach(function(hardwareItem, i) {
            hardwareTableBodyRow = document.createElement('tr');
            hardwareProperties.forEach(function (hardwareProperty, i) {
                hardwareTableBodyCell = document.createElement("td");

                if(hardwareProperty === "multiplier"){
                    hardwareTableBodyCell.innerHTML = "x" + hardwareItem[hardwareProperty].toLocaleString();
                } else {
                    hardwareTableBodyCell.innerHTML = hardwareItem[hardwareProperty].toLocaleString();
                }

                hardwareTableBodyRow.append(hardwareTableBodyCell);
            });
            hardwareTableBody.append(hardwareTableBodyRow);
        });
        hardwareTable.append(hardwareTableBody);

        hardwareDiv.append(hardwareTable);
    })
};

function loadUsers() {
    fetch(ROOT_URL+'/users')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        // Build users
        jsonResponse.sort(sortJsonByKey("id"));
        const usersHeaders = ["ID", "User", "Passkey", "Category", "Hardware ID", "Live Stats Link"];
        const usersProperties = ["id", "displayName", "passkey", "category", "hardwareId", "liveStatsLink"];

        usersDiv = document.getElementById("users_div");

        usersTitle = document.createElement('h2');
        usersTitle.setAttribute("class", "navbar-brand");
        usersTitle.innerHTML = "Users";
        usersDiv.append(usersTitle);

        usersTable = document.createElement('table');
        usersTable.setAttribute("id", "users");
        usersTable.setAttribute("class", "table table-dark table-striped table-hover");

        usersTableHead = document.createElement('thead');
        usersTableHeaderRow = document.createElement('tr');
        usersHeaders.forEach(function (header, i) {
            usersTableHeader = document.createElement("th");
            usersTableHeader.setAttribute("onclick", "sortTable("+i+", 'users')");
            usersTableHeader.setAttribute("scope", "col");
            usersTableHeader.innerHTML = header;

            usersTableHeaderRow.append(usersTableHeader);
        });
        usersTableHead.append(usersTableHeaderRow);
        usersTable.append(usersTableHead);


        usersTableBody = document.createElement('tbody');

        jsonResponse.forEach(function(usersItem, i) {
            usersTableBodyRow = document.createElement('tr');
            usersProperties.forEach(function (usersProperty, i) {
                usersTableBodyCell = document.createElement("td");

                if (usersProperty === "liveStatsLink") {
                    if (usersProperty in usersItem) {
                        liveStatsLink = document.createElement('a');
                        liveStatsLink.setAttribute("href", usersItem[usersProperty]);
                        liveStatsLink.innerHTML = usersItem[usersProperty];

                        usersTableBodyCell.append(liveStatsLink);
                    }
                } else {
                    usersTableBodyCell.innerHTML = usersItem[usersProperty].toLocaleString();
                }
                usersTableBodyRow.append(usersTableBodyCell);
            });
            usersTableBody.append(usersTableBodyRow);
        });
        usersTable.append(usersTableBody);

        usersDiv.append(usersTable);
    })
};

function loadTeams() {
    fetch(ROOT_URL+'/teams')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        // Build teams
        jsonResponse.sort(sortJsonByKey("id"));
        const teamsHeaders = ["ID", "Name", "Description", "Captain ID", "User IDs", "Retired User IDs"];
        const teamsProperties = ["id", "teamName", "teamDescription", "captainUserId", "userIds", "retiredUserIds"];

        teamsDiv = document.getElementById("teams_div");

        teamsTitle = document.createElement('h2');
        teamsTitle.setAttribute("class", "navbar-brand");
        teamsTitle.innerHTML = "Teams";
        teamsDiv.append(teamsTitle);

        teamsTable = document.createElement('table');
        teamsTable.setAttribute("id", "teams");
        teamsTable.setAttribute("class", "table table-dark table-striped table-hover");

        teamsTableHead = document.createElement('thead');
        teamsTableHeaderRow = document.createElement('tr');
        teamsHeaders.forEach(function (header, i) {
            teamsTableHeader = document.createElement("th");
            teamsTableHeader.setAttribute("onclick", "sortTable("+i+", 'teams')");
            teamsTableHeader.setAttribute("scope", "col");
            teamsTableHeader.innerHTML = header;

            teamsTableHeaderRow.append(teamsTableHeader);
        });
        teamsTableHead.append(teamsTableHeaderRow);
        teamsTable.append(teamsTableHead);

        teamsTableBody = document.createElement('tbody');

        jsonResponse.forEach(function(teamsItem, i) {
            teamsTableBodyRow = document.createElement('tr');
            teamsProperties.forEach(function (teamsProperty, i) {
                teamsTableBodyCell = document.createElement("td");

                if (teamsProperty === "liveStatsLink") {
                    if (teamsProperty in teamsItem) {
                        liveStatsLink = document.createElement('a');
                        liveStatsLink.setAttribute("href", teamsItem[teamsProperty]);
                        liveStatsLink.innerHTML = teamsItem[teamsProperty];

                        teamsTableBodyCell.append(liveStatsLink);
                    }
                } else {
                    teamsTableBodyCell.innerHTML = teamsItem[teamsProperty].toLocaleString();
                    teamsTableBodyRow.append(teamsTableBodyCell);
                }
            });
            teamsTableBody.append(teamsTableBodyRow);
        });
        teamsTable.append(teamsTableBody);

        teamsDiv.append(teamsTable);
    })
};

document.addEventListener("DOMContentLoaded", function(event) {
    loadHardware();
    loadUsers();
    loadTeams();
    updateTimer();
});
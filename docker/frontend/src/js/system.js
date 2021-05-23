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

function adminLogin(){
    var userName = document.getElementById("inputUserName").value;
    var password = document.getElementById("inputPassword").value;
    var authorizationPayload = "Basic " + encode(userName, password);

    var requestData = {
        "encodedUserNameAndPassword": authorizationPayload
    };

    show("loader");

    fetch(ROOT_URL+'/login/admin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => {
        document.getElementById("inputUserName").value = '';
        document.getElementById("inputPassword").value = '';
        hide("loader");

        if(response.status != 200){
            showToast("toast-login-failure", true);
            return;
        }

        showToast("toast-login-success", true);
        hide("login-form");
        show("admin-functions");
        sessionSet("Authorization", authorizationPayload);
    });
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
        dataListOfHardware = document.getElementById("available-hardware");

        jsonResponse.forEach(function(hardwareItem, i) {
            // Update hardware display table
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

            // Update the create/update dropdown menus for users
            hardwareOption = document.createElement("option");
            hardwareOption.setAttribute("value", hardwareItem['id']);
            hardwareOption.innerHTML = hardwareItem["displayName"];
            dataListOfHardware.append(hardwareOption);
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
        const usersHeaders = ["ID", "User", "Folding Name", "Passkey", "Category", "Hardware ID", "Profile Link", "Live Stats Link", "Retired"];
        const usersProperties = ["id", "displayName", "foldingUserName", "passkey", "category", "hardwareId", "profileLink", "liveStatsLink", "isRetired"];

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

        dataListOfUsers = document.getElementById("available-users");
        selectOfUsers = document.getElementById("create-team-users");

        jsonResponse.forEach(function(usersItem, i) {
            // Update users display table
            usersTableBodyRow = document.createElement('tr');
            usersProperties.forEach(function (usersProperty, i) {
                usersTableBodyCell = document.createElement("td");

                if (usersProperty === "liveStatsLink" || usersProperty === "profileLink") {
                    if (usersProperty in usersItem) {
                        link = document.createElement('a');
                        link.setAttribute("href", usersItem[usersProperty]);
                        link.innerHTML = usersItem[usersProperty];

                        usersTableBodyCell.append(link);
                    }
                } else {
                    if (usersProperty in usersItem) {
                        usersTableBodyCell.innerHTML = usersItem[usersProperty].toLocaleString();
                    }
                }
                usersTableBodyRow.append(usersTableBodyCell);
            });
            usersTableBody.append(usersTableBodyRow);

            // Update the create/update dropdown menus for users
            userOption = document.createElement("option");
            userOption.setAttribute("value", usersItem['id']);
            userOption.innerHTML = usersItem["displayName"];

            dataListOfUsers.append(userOption);
            selectOfUsers.append(userOption);
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
        const teamsHeaders = ["ID", "Name", "Description", "Forum Link", "Captain ID", "User IDs", "Retired User IDs"];
        const teamsProperties = ["id", "teamName", "teamDescription", "forumLink", "captainUserId", "userIds", "retiredUserIds"];

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

                if (teamsProperty === "forumLink") {
                    if (teamsProperty in teamsItem) {
                        link = document.createElement('a');
                        link.setAttribute("href", teamsItem[teamsProperty]);
                        link.setAttribute("target", "_blank");
                        link.innerHTML = teamsItem[teamsProperty];

                        teamsTableBodyCell.append(link);
                    }
                } else {
                    if (teamsProperty in teamsItem) {
                        teamsTableBodyCell.innerHTML = teamsItem[teamsProperty].toLocaleString();
                    }
                }
                teamsTableBodyRow.append(teamsTableBodyCell);
            });
            teamsTableBody.append(teamsTableBodyRow);
        });
        teamsTable.append(teamsTableBody);

        teamsDiv.append(teamsTable);
    })
};

document.addEventListener("DOMContentLoaded", function(event) {
    if(sessionContains("Authorization")) {
        hide("login-form");
        show("admin-functions");
    }

    loadHardware();
    loadUsers();
    loadTeams();
    updateTimer();
    hide("loader");
});
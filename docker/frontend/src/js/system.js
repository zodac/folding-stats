const ROOT_URL='http://internal.axihub.ca/folding';

// The 'toggle' functions below simply change the colour of the buttons. There must be a smarter way to do this...
function toggleMainButtonStyle(id, classList){
    var button = document.getElementById(id);

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary");
        button.classList.remove("btn-success");
    } else {
        button.classList.add("btn-success");
        button.classList.remove("btn-primary");
    }
}

function adminLogin(){
    var userName = document.getElementById("login_username").value;
    var password = document.getElementById("login_password").value;
    var authorizationPayload = "Basic " + encode(userName, password);

    var requestData = JSON.stringify(
        {
            "encodedUserNameAndPassword": authorizationPayload
        }
    );

    show("loader");

    fetch(ROOT_URL+'/login/admin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: requestData
    })
    .then(response => {
        document.getElementById("login_username").value = '';
        document.getElementById("login_password").value = '';
        hide("loader");

        if(response.status != 200){
            failureToast("Invalid admin credentials!");
            return;
        }

        successToast("Logged in successfully!");
        hide("login_form");
        show("admin_functions");
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
        const hardwareHeaders = ["ID", "Name", "Display Name", "Operating System", "Multiplier"];
        const hardwareProperties = ["id", "hardwareName", "displayName", "operatingSystem", "multiplier"];

        // Empty div of existing content, if any
        hardwareDiv = document.getElementById("hardware_div");
        while (hardwareDiv.firstChild) {
            hardwareDiv.removeChild(hardwareDiv.lastChild);
        }

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
        });
        hardwareTable.append(hardwareTableBody);

        hardwareDiv.append(hardwareTable);

        // Update any list that needs all hardware
        jsonResponse.sort(sortJsonByKey("hardwareName"));

        hardwareLists = document.querySelectorAll(".hardware_list");
        for (var i = 0, hardwareList; hardwareList = hardwareLists[i]; i++) {
            // Clear existing entries
            while (hardwareList.firstChild) {
                hardwareList.removeChild(hardwareList.lastChild);
            }

            // Add the default entry
            defaultHardwareOption = document.createElement("option");
            defaultHardwareOption.setAttribute("value", "");
            defaultHardwareOption.setAttribute("disabled", "");
            defaultHardwareOption.setAttribute("selected", "");
            defaultHardwareOption.innerHTML = "Choose Hardware...";
            hardwareList.append(defaultHardwareOption);

            // Add entries
            jsonResponse.forEach(function(hardwareItem, i) {
                hardwareOption = document.createElement("option");
                hardwareOption.setAttribute("value", hardwareItem['id']);

                hardwareOption.setAttribute("hardware_id", hardwareItem['id']);
                hardwareOption.setAttribute("hardware_name", hardwareItem['hardwareName']);
                hardwareOption.setAttribute("display_name", hardwareItem['displayName']);
                hardwareOption.setAttribute("operating_system", hardwareItem['operatingSystem']);
                hardwareOption.setAttribute("multiplier", hardwareItem['multiplier']);

                hardwareOption.innerHTML = hardwareItem["displayName"] + " (" +hardwareItem["operatingSystem"] + ")";
                hardwareList.append(hardwareOption);
            });
        }
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

        // Empty div of existing content, if any
        usersDiv = document.getElementById("users_div");
        while (usersDiv.firstChild) {
            usersDiv.removeChild(usersDiv.lastChild);
        }

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

        dataListOfUsers = document.getElementById("available_users");
        selectOfUsers = document.getElementById("create_team_users");

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
            var userOption = document.createElement("option");
            userOption.setAttribute("value", usersItem['id']);
            userOption.innerHTML = usersItem["displayName"];
            dataListOfUsers.append(userOption);

            var userOption = document.createElement("option");
            userOption.setAttribute("value", usersItem['id']);
            userOption.innerHTML = usersItem["displayName"];
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

        // Empty div of existing content, if any
        teamsDiv = document.getElementById("teams_div");
        while (teamsDiv.firstChild) {
            teamsDiv.removeChild(teamsDiv.lastChild);
        }

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

function populateHardwareUpdate(){
    show("loader");
    element = document.getElementById("hardware_update_selector");
    selectedElement = element.options[element.selectedIndex];

    document.getElementById("hardware_update_id").value = selectedElement.getAttribute("hardware_id");
    document.getElementById("hardware_update_name").value = selectedElement.getAttribute("hardware_name");
    document.getElementById("hardware_update_display_name").value = selectedElement.getAttribute("display_name");
    document.getElementById("hardware_update_operating_system").value = selectedElement.getAttribute("operating_system");
    document.getElementById("hardware_update_multiplier").value = selectedElement.getAttribute("multiplier");

    hardwareFields = document.querySelectorAll(".hardware_update");
    for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField);
    }
    hide("loader");
}

function populateHardwareDelete(){
    show("loader");
    element = document.getElementById("hardware_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    document.getElementById("hardware_delete_id").value = selectedElement.getAttribute("hardware_id");
    document.getElementById("hardware_delete_name").value = selectedElement.getAttribute("hardware_name");
    document.getElementById("hardware_delete_display_name").value = selectedElement.getAttribute("display_name");
    document.getElementById("hardware_delete_operating_system").value = selectedElement.getAttribute("operating_system");
    document.getElementById("hardware_delete_multiplier").value = selectedElement.getAttribute("multiplier");

    hardwareFields = document.querySelectorAll(".hardware_delete");
    for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField);
    }
    hide("loader");
}

function createHardware() {
    var hardwareName = document.getElementById("hardware_create_name").value.trim();
    var displayName = document.getElementById("hardware_create_display_name").value.trim();
    var operatingSystem = document.getElementById("hardware_create_operating_system").value.trim();
    var multiplier = document.getElementById("hardware_create_multiplier").value.trim();

    var requestData = JSON.stringify(
        {
            "hardwareName": hardwareName,
            "displayName": displayName,
            "operatingSystem": operatingSystem,
            "multiplier": multiplier
        }
    );

    show("loader");
    fetch(ROOT_URL+'/hardware', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
        },
        body: requestData
    })
    .then(response => {
        hide("loader");

        if(response.status != 201){
            failureToast("Hardware create failed with code: " + response.status);
            return;
        }

        document.getElementById("hardware_create_name").value = '';
        document.getElementById("hardware_create_display_name").value = '';
        document.getElementById("hardware_create_operating_system").value = '';
        document.getElementById("hardware_create_multiplier").value = '';
        successToast("Hardware '" + displayName + "' created");
        loadHardware();
    });
}

function updateHardware() {
    var element = document.getElementById("hardware_update_selector");
    element = document.getElementById("hardware_update_selector");
    selectedElement = element.options[element.selectedIndex];

    var hardwareId = selectedElement.getAttribute("hardware_id");

    var hardwareName = document.getElementById("hardware_update_name").value.trim();
    var displayName = document.getElementById("hardware_update_display_name").value.trim();
    var operatingSystem = document.getElementById("hardware_update_operating_system").value.trim();
    var multiplier = document.getElementById("hardware_update_multiplier").value.trim();

    var requestData = JSON.stringify(
        {
            "hardwareName": hardwareName,
            "displayName": displayName,
            "operatingSystem": operatingSystem,
            "multiplier": multiplier
        }
    );

    console.log("Updating " + hardwareId + ", payload: " + requestData);

    show("loader");
    fetch(ROOT_URL+'/hardware/' + hardwareId, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
        },
        body: requestData
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Hardware update failed with code: " + response.status);
            return;
        }

        document.getElementById("hardware_update_id").value = '';
        document.getElementById("hardware_update_name").value = '';
        document.getElementById("hardware_update_display_name").value = '';
        document.getElementById("hardware_update_operating_system").value = '';
        document.getElementById("hardware_update_multiplier").value = '';
        successToast("Hardware '" + displayName + "' updated");
        loadHardware();
    });
}

function deleteHardware() {
    var element = document.getElementById("hardware_delete_selector");
    element = document.getElementById("hardware_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    var hardwareId = selectedElement.getAttribute("hardware_id");
    var hardwareDisplayName = selectedElement.getAttribute("hardware_name");

    show("loader");
    fetch(ROOT_URL+'/hardware/' + hardwareId, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Hardware delete failed with code: " + response.status);
            return;
        }

        document.getElementById("hardware_delete_id").value = '';
        document.getElementById("hardware_delete_name").value = '';
        document.getElementById("hardware_delete_display_name").value = '';
        document.getElementById("hardware_delete_operating_system").value = '';
        document.getElementById("hardware_delete_multiplier").value = '';

        hardwareFields = document.querySelectorAll(".hardware_delete");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        successToast("Hardware '" + hardwareDisplayName + "' deleted");
        loadHardware();
    });
}


document.addEventListener("DOMContentLoaded", function(event) {
    if(sessionContains("Authorization")) {
        hide("login_form");
        show("admin_functions");
    }

    loadHardware();
    loadUsers();
    loadTeams();
    updateTimer();
    hide("loader");
});
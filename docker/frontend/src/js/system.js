const ROOT_URL="%ROOT_URL%";

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
        hide("loader");
        document.getElementById("login_username").value = '';
        document.getElementById("login_password").value = '';

        if(response.status != 200){
            failureToast("Invalid admin credentials!");
            return;
        }

        successToast("Logged in successfully!");
        hide("login_form");
        show("admin_functions");
        sessionSet("Authorization", authorizationPayload);
    })
    .catch((error) => {
        hide("loader");
        console.error('Unexpected error logging in: ', error);
        return false;
    });
}

function manualUpdate() {
    show("loader");
    fetch(ROOT_URL+'/stats/manual/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Manual update failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }
        successToast("Stats manually updated");
    })
    .catch((error) => {
        hide("loader");
        console.error('Unexpected error updating stats: ', error);
        return false;
    });
}

function manualLars() {
    show("loader");
    fetch(ROOT_URL+'/debug/lars', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Manual LARS update failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }
        successToast("LARS data manually updated");
    })
    .catch((error) => {
        hide("loader");
        console.error('Unexpected error updating LARS: ', error);
        return false;
    });
}

function printCaches() {
    show("loader");
    fetch(ROOT_URL+'/debug/caches', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Printing caches failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }
        successToast("Caches printed");
    })
    .catch((error) => {
        hide("loader");
        console.error('Unexpected error printing caches: ', error);
        return false;
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
        const hardwareHeaders = ["ID", "Name", "Display Name", "Make", "Type", "Multiplier", "Average PPD"];
        const hardwareProperties = ["id", "hardwareName", "displayName", "hardwareMake", "hardwareType", "multiplier", "averagePpd"];

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

                if (hardwareProperty === "multiplier") {
                    hardwareTableBodyCell.innerHTML = "x" + hardwareItem[hardwareProperty].toLocaleString();
                } else if (hardwareProperty === "hardwareMake") {
                    hardwareTableBodyCell.innerHTML = getHardwareMakeFrontend(hardwareItem[hardwareProperty]);
                } else if (hardwareProperty === "hardwareType") {
                    hardwareTableBodyCell.innerHTML = getHardwareTypeFrontend(hardwareItem[hardwareProperty]);
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
                hardwareOption.setAttribute("hardware_make", hardwareItem['hardwareMake']);
                hardwareOption.setAttribute("hardware_type", hardwareItem['hardwareType']);
                hardwareOption.setAttribute("multiplier", hardwareItem['multiplier']);
                hardwareOption.setAttribute("average_ppd", hardwareItem['averagePpd']);

                hardwareOption.innerHTML = hardwareItem['hardwareName'] + " (" + hardwareItem['displayName'] + ")";
                hardwareList.append(hardwareOption);
            });
        }

        hardwareDataLists = document.querySelectorAll(".hardware_datalist");
        for (var i = 0, hardwareDataList; hardwareDataList = hardwareDataLists[i]; i++) {
            // Clear existing entries
            while (hardwareDataList.firstChild) {
                hardwareDataList.removeChild(hardwareDataList.lastChild);
            }

            // Add entries
            jsonResponse.forEach(function(hardwareItem, i) {
                hardwareOption = document.createElement("option");
                hardwareOption.setAttribute("value", hardwareItem['hardwareName']);
                hardwareOption.innerHTML = hardwareItem['displayName'];
                hardwareDataList.append(hardwareOption);
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
        const usersHeaders = ["ID", "User", "Folding Name", "Passkey", "Category", "Profile Link", "Live Stats Link", "Hardware", "Team", "Is Captain"];
        const usersProperties = ["id", "displayName", "foldingUserName", "passkey", "category", "profileLink", "liveStatsLink", "hardware", "team", "userIsCaptain"];

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
                } else if (usersProperty === "hardware") {
                    usersTableBodyCell.innerHTML = usersItem["hardware"]["hardwareName"].toLocaleString();
                } else if (usersProperty === "team") {
                    usersTableBodyCell.innerHTML = usersItem["team"]["teamName"].toLocaleString();
                } else if (usersProperty === "category") {
                    usersTableBodyCell.innerHTML = getCategoryFrontend(usersItem[usersProperty]);
                } else {
                    if (usersProperty in usersItem) {
                        usersTableBodyCell.innerHTML = usersItem[usersProperty].toLocaleString();
                    }
                }
                usersTableBodyRow.append(usersTableBodyCell);
            });
            usersTableBody.append(usersTableBodyRow);
        });
        usersTable.append(usersTableBody);

        usersDiv.append(usersTable);

        // Update any list that needs all users
        jsonResponse.sort(sortJsonByKey("displayName"));

        userLists = document.querySelectorAll(".user_list");
        for (var i = 0, userList; userList = userLists[i]; i++) {
            // Clear existing entries
            while (userList.firstChild) {
                userList.removeChild(userList.lastChild);
            }

            // Add the default entry
            defaultUserOption = document.createElement("option");
            defaultUserOption.setAttribute("value", "");
            defaultUserOption.setAttribute("disabled", "");
            defaultUserOption.setAttribute("selected", "");
            defaultUserOption.innerHTML = "Choose User...";
            userList.append(defaultUserOption);

            // Add entries
            jsonResponse.forEach(function(userItem, i) {
                userOption = document.createElement("option");
                userOption.setAttribute("value", userItem['id']);

                userOption.setAttribute("user_id", userItem['id']);
                userOption.setAttribute("user_folding_name", userItem['foldingUserName']);
                userOption.setAttribute("user_display_name", userItem['displayName']);
                userOption.setAttribute("user_passkey", userItem['passkey']);
                userOption.setAttribute("user_category", getCategoryFrontend(userItem['category']));
                userOption.setAttribute("user_profile_link", userItem['profileLink']);
                userOption.setAttribute("user_live_stats_link", userItem['liveStatsLink']);
                userOption.setAttribute("user_hardware_id", userItem['hardware']['id']);
                userOption.setAttribute("user_team_id", userItem['team']['id']);
                userOption.setAttribute("user_is_captain", userItem['userIsCaptain']);

                userOption.innerHTML = userItem["displayName"] + " ("+userItem['team']['teamName']+")";
                userList.append(userOption);
            });
        }
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
        const teamsHeaders = ["ID", "Name", "Description", "Forum Link"];
        const teamsProperties = ["id", "teamName", "teamDescription", "forumLink"];

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

        teamLists = document.querySelectorAll(".team_list");
        for (var i = 0, teamList; teamList = teamLists[i]; i++) {
            // Clear existing entries
            while (teamList.firstChild) {
                teamList.removeChild(teamList.lastChild);
            }

            // Add the default entry
            defaultTeamOption = document.createElement("option");
            defaultTeamOption.setAttribute("value", "");
            defaultTeamOption.setAttribute("disabled", "");
            defaultTeamOption.setAttribute("selected", "");
            defaultTeamOption.innerHTML = "Choose Team...";
            teamList.append(defaultTeamOption);

            // Add entries
            jsonResponse.forEach(function(teamItem, i) {
                teamOption = document.createElement("option");
                teamOption.setAttribute("value", teamItem['id']);

                teamOption.setAttribute("team_id", teamItem['id']);
                teamOption.setAttribute("team_name", teamItem['teamName']);
                teamOption.setAttribute("team_description", teamItem['teamDescription']);
                teamOption.setAttribute("team_forum_link", teamItem['forumLink']);

                teamOption.innerHTML = teamItem["teamName"];
                teamList.append(teamOption);
            });
        }

        teamDataLists = document.querySelectorAll(".team_datalist");
        for (var i = 0, teamDataList; teamDataList = teamDataLists[i]; i++) {
            // Clear existing entries
            while (teamDataList.firstChild) {
                teamDataList.removeChild(teamDataList.lastChild);
            }

            // Add entries
            jsonResponse.forEach(function(teamItem, i) {
                teamOption = document.createElement("option");
                teamOption.setAttribute("value", teamItem['teamName']);
                teamDataList.append(teamOption);
            });
        }
    })
};


document.addEventListener("DOMContentLoaded", function(event) {
    if(sessionContains("Authorization")) {
        hide("login_form");
        show("admin_functions");
    }

    loadHardware();
    loadTeams();
    loadUsers();
    updateTimer();
    hide("loader");
});
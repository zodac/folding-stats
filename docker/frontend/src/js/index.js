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

function toggleTeam(teamNumber, classList) {
    var button = document.getElementById("team_"+teamNumber+"_button");

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary");
        button.classList.remove("btn-success");
    } else {
        button.classList.add("btn-success");
        button.classList.remove("btn-primary");
    }
}

function loadTcStats() {
    fetch(ROOT_URL+'/tc_stats')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        // Build competition stats
        const statsTableHeaders = ["Total Points", "Total Units"];
        const statsTableTeamProperties = ["totalMultipliedPoints", "totalUnits"];

        statsDiv = document.getElementById("stats_div");

        statsTitle = document.createElement('h2');
        statsTitle.setAttribute("class", "navbar-brand");
        statsTitle.innerHTML = "Overall Stats";
        statsDiv.append(statsTitle);

        statsTable = document.createElement('table');
        statsTable.setAttribute("id", "stats");
        statsTable.setAttribute("class", "table table-dark table-striped table-hover");

        statsTableHead = document.createElement('thead');
        statsTableHeaderRow = document.createElement('tr');
        statsTableHeaders.forEach(function (header, i) {
            statsTableHeader = document.createElement("th");
            statsTableHeader.setAttribute("onclick", "sortTable("+i+", 'stats')");
            statsTableHeader.setAttribute("scope", "col");
            statsTableHeader.innerHTML = header;

            statsTableHeaderRow.append(statsTableHeader);
        });
        statsTableHead.append(statsTableHeaderRow);
        statsTable.append(statsTableHead);

        statsTableBody = document.createElement('tbody');
        statsTableBodyRow = document.createElement('tr');
        statsTableTeamProperties.forEach(function (teamProperty, i) {
            statsTableBodyCell = document.createElement("td");

            if(teamProperty === "totalMultipliedPoints"){
                statsTableBodyCell.setAttribute("data-bs-toggle", "tooltip");
                statsTableBodyCell.setAttribute("data-placement", "top");
                statsTableBodyCell.setAttribute("title", "Unmultiplied: " + jsonResponse["totalPoints"].toLocaleString());
                new bootstrap.Tooltip(statsTableBodyCell);
            }

            statsTableBodyCell.innerHTML = jsonResponse[teamProperty].toLocaleString();
            statsTableBodyRow.append(statsTableBodyCell);
        });
        statsTableBody.append(statsTableBodyRow);
        statsTable.append(statsTableBody);

        statsDiv.append(statsTable);

        // Build team tables
        const teamTableHeaders = ["Rank", "User", "Category", "Hardware", "Points", "Units"];
        const teamTableUserProperties = ["rankInTeam", "userName", "category", "hardware", "multipliedPoints", "units"];

        var jsonResponseTeams = jsonResponse['teams'];
        jsonResponseTeams.sort(sortJsonByKey("rank"));
        jsonResponseTeams.forEach(function(team, i) {
            var teamNumber = (i+1);
            var captainName = team['captainName'];

            teamDiv = document.createElement('div');
            teamDiv.setAttribute("id", "team_"+teamNumber+"_div");

            metadataDiv = document.createElement('div');
            metadataDiv.setAttribute("id", "team_"+teamNumber+"_metadata")

            teamTitle = document.createElement('h2');
            teamTitle.innerHTML = "Rank #" + team['rank'] + ": " + team['teamName'];
            metadataDiv.append(teamTitle);

            teamStats = document.createElement('h5');
            teamStats.setAttribute("id", "team_"+teamNumber+"_stats");
            teamStats.setAttribute("data-bs-toggle", "tooltip");
            teamStats.setAttribute("data-placement", "top");
            teamStats.setAttribute("title", "Unmultiplied: " + team["teamPoints"].toLocaleString());
            teamStats.innerHTML = team['teamMultipliedPoints'].toLocaleString() + " points | " + team['teamUnits'].toLocaleString() + " units";
            new bootstrap.Tooltip(teamStats);
            metadataDiv.append(teamStats);

            teamButton = document.createElement('button');
            teamButton.setAttribute("id", "team_"+teamNumber+"_button");
            teamButton.setAttribute("class", "btn ui-btn btn-primary");
            teamButton.setAttribute("href", "#team_"+teamNumber+"_subdiv");
            teamButton.setAttribute("onclick", "toggleTeam("+teamNumber+", this.classList)");
            teamButton.setAttribute("data-bs-toggle", "collapse");
            teamButton.setAttribute("role", "button");
            teamButton.innerHTML = "Show/Hide";
            metadataDiv.append(teamButton);
            teamDiv.append(metadataDiv);

            subDiv = document.createElement('div');
            subDiv.setAttribute("id", "team_"+teamNumber+"_subdiv");
            subDiv.setAttribute("class", "collapse");

            teamTable = document.createElement('table');
            teamTable.setAttribute("id", "team_"+teamNumber);
            teamTable.setAttribute("class", "table table-dark table-striped table-hover");

            teamTableHead = document.createElement("thead");
            teamTableHeaderRow = document.createElement("tr");
            teamTableHeaders.forEach(function (header, i) {
                teamTableHeaderCell = document.createElement("th");
                teamTableHeaderCell.setAttribute("onclick", "sortTable("+i+", 'team_"+teamNumber+"')");
                teamTableHeaderCell.setAttribute("scope", "col");
                teamTableHeaderCell.innerHTML = header;

                teamTableHeaderRow.append(teamTableHeaderCell);
            });
            teamTableHead.append(teamTableHeaderRow);
            teamTable.append(teamTableHead);


            teamTableBody = document.createElement("tbody");

            activeUsers = team['activeUsers'];
            activeUsers.forEach(function (activeUser, i) {
                teamTableBodyRow = document.createElement("tr");

                teamTableUserProperties.forEach(function (userProperty, i) {
                    teamTableUserCell = document.createElement("td");

                    if(userProperty === "multipliedPoints"){
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip");
                        teamTableUserCell.setAttribute("data-placement", "left");
                        teamTableUserCell.setAttribute("title", "Unmultiplied: " + activeUser["points"].toLocaleString());
                        teamTableUserCell.innerHTML = activeUser[userProperty].toLocaleString();
                        new bootstrap.Tooltip(teamTableUserCell);
                    } else if (userProperty === "userName" && activeUser[userProperty] === captainName) {
                        teamTableUserCell.innerHTML = activeUser[userProperty].toLocaleString() + " (Captain)";
                    } else if (userProperty === "hardware") {
                    console.log("Found: " + activeUser["hardware"]);
                    console.log("Want: " + activeUser["hardware"]["multiplier"])
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip");
                        teamTableUserCell.setAttribute("data-placement", "left");
                        teamTableUserCell.setAttribute("title", "Multiplier: x" + activeUser["hardware"]["multiplier"].toLocaleString());
                        teamTableUserCell.innerHTML = activeUser[userProperty]["displayName"].toLocaleString();
                        new bootstrap.Tooltip(teamTableUserCell);
                    } else {
                        teamTableUserCell.innerHTML = activeUser[userProperty].toLocaleString();
                    }

                    teamTableBodyRow.append(teamTableUserCell);
                });
                teamTableBody.append(teamTableBodyRow);
            });

            retiredUsers = team['retiredUsers'];
            retiredUsers.forEach(function (retiredUser, i) {
                teamTableBodyRow = document.createElement("tr");

                teamTableUserProperties.forEach(function (userProperty, i) {
                    teamTableUserCell = document.createElement("td");

                    if(userProperty === "multipliedPoints"){
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip");
                        teamTableUserCell.setAttribute("data-placement", "left");
                        teamTableUserCell.setAttribute("title", "Unmultiplied: " + retiredUser["points"].toLocaleString());
                        teamTableUserCell.innerHTML = retiredUser[userProperty].toLocaleString();
                        new bootstrap.Tooltip(teamTableUserCell);
                    } else if (userProperty === "userName") {
                        teamTableUserCell.innerHTML = retiredUser[userProperty] + " (retired)";
                    } else if (userProperty === "hardware") {
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip");
                        teamTableUserCell.setAttribute("data-placement", "left");
                        teamTableUserCell.setAttribute("title", "Multiplier: x" + retiredUser["hardware"]["multiplier"].toLocaleString());
                        teamTableUserCell.innerHTML = retiredUser[userProperty]["displayName"].toLocaleString();
                        new bootstrap.Tooltip(teamTableUserCell);
                    } else {
                        teamTableUserCell.innerHTML = retiredUser[userProperty].toLocaleString();
                    }

                    teamTableBodyRow.append(teamTableUserRow);
                });
                teamTableBody.append(teamTableBodyRow);
            });
            teamTable.append(teamTableBody);
            subDiv.append(teamTable);

            teamDiv.append(subDiv);

            statsDiv.append(teamDiv);
            statsDiv.append(document.createElement('br'));
        });

        hide("loader");
    })
};

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
    loadTcStats();
    loadHardware();
    loadUsers();
    loadTeams();
    updateTimer();
});
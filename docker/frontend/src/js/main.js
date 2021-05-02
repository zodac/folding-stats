const ROOT_URL='http://teamcomp.axihub.ca';

// The 'toggle' functions below simply change the colour of the buttons. There must be a smarter way to do this...
function toggleStats() {
    if($("#stats_div").is(":visible")){
        $("#stats_button").addClass("btn-primary");
        $("#stats_button").removeClass("btn-success");
    } else {
        $("#stats_button").removeClass("btn-primary");
        $("#stats_button").addClass("btn-success");
    }
}

function toggleHardware() {
    if($("#hardware_div").is(":visible")){
        $("#hardware_button").addClass("btn-primary");
        $("#hardware_button").removeClass("btn-success");
    } else {
        $("#hardware_button").removeClass("btn-primary");
        $("#hardware_button").addClass("btn-success");
    }
}

function toggleUsers() {
    if($("#users_div").is(":visible")){
        $("#users_button").addClass("btn-primary");
        $("#users_button").removeClass("btn-success");
    } else {
        $("#users_button").removeClass("btn-primary");
        $("#users_button").addClass("btn-success");
    }
}

function toggleTeams() {
    if($("#teams_div").is(":visible")){
        $("#teams_button").addClass("btn-primary");
        $("#teams_button").removeClass("btn-success");
    } else {
        $("#teams_button").removeClass("btn-primary");
        $("#teams_button").addClass("btn-success");
    }
}

function toggleTeam(teamNumber) {
    if($("#team_"+teamNumber+"_subdiv").is(":visible")){
        $("#team_"+teamNumber+"_button").addClass("btn-primary");
        $("#team_"+teamNumber+"_button").removeClass("btn-success");
    } else {
        $("#team_"+teamNumber+"_button").removeClass("btn-primary");
        $("#team_"+teamNumber+"_button").addClass("btn-success");
    }
}

// Pull the TC stats, then manually update each of the numeric fields to be formatted with commas
function loadTcStats() {
    const prodUrl=ROOT_URL+'/tc_stats';

    fetch(prodUrl)
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
        statsTable.setAttribute("class", "table table-striped table-hover");

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
                statsTableBodyCell.setAttribute("data-toggle", "tooltip");
                statsTableBodyCell.setAttribute("data-placement", "top");
                statsTableBodyCell.setAttribute("title", "Unmultiplied: " + jsonResponse["totalPoints"].toLocaleString());
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
        $.each(jsonResponseTeams, function(i, team) {
            var teamNumber = (i+1);

            teamDiv = document.createElement('div');
            teamDiv.setAttribute("id", "team_"+teamNumber+"_div");

            metadataDiv = document.createElement('div');
            metadataDiv.setAttribute("id", "team_"+teamNumber+"_metadata")

            teamTitle = document.createElement('h2');
            teamTitle.innerHTML = "Rank #" + team['rank'] + ": " + team['teamName'] + " (Captain: " + team['captainName'] + ")";
            metadataDiv.append(teamTitle);

            teamStats = document.createElement('h5');
            teamStats.setAttribute("id", "team_"+teamNumber+"_stats");
            teamStats.setAttribute("data-toggle", "tooltip");
            teamStats.setAttribute("data-placement", "top");
            teamStats.setAttribute("title", "Unmultiplied: " + team["teamPoints"].toLocaleString());
            teamStats.innerHTML = team['teamMultipliedPoints'].toLocaleString() + " points | " + team['teamUnits'].toLocaleString() + " units";
            metadataDiv.append(teamStats);

            teamButton = document.createElement('button');
            teamButton.setAttribute("id", "team_"+teamNumber+"_button");
            teamButton.setAttribute("class", "btn ui-btn btn-primary");
            teamButton.setAttribute("href", "#team_"+teamNumber+"_subdiv");
            teamButton.setAttribute("onclick", "toggleTeam("+teamNumber+")");
            teamButton.setAttribute("data-toggle", "collapse");
            teamButton.setAttribute("role", "button");
            teamButton.innerHTML = "Show/Hide";
            metadataDiv.append(teamButton);
            teamDiv.append(metadataDiv);

            subDiv = document.createElement('div');
            subDiv.setAttribute("id", "team_"+teamNumber+"_subdiv");
            subDiv.setAttribute("class", "collapse");

            teamTable = document.createElement('table');
            teamTable.setAttribute("id", "team_"+teamNumber);
            teamTable.setAttribute("class", "table table-striped table-hover");

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
                        teamTableUserCell.setAttribute("data-toggle", "tooltip");
                        teamTableUserCell.setAttribute("data-placement", "left");
                        teamTableUserCell.setAttribute("title", "Unmultiplied: " + activeUser["points"].toLocaleString());
                    }

                    teamTableUserCell.innerHTML = activeUser[userProperty].toLocaleString();
                    teamTableBodyRow.append(teamTableUserCell);
                });
                teamTableBody.append(teamTableBodyRow);
            });

            retiredUsers = team['retiredUsers'];
            retiredUsers.forEach(function (retiredUser, i) {
                teamTableBodyRow = document.createElement("tr");

                teamTableUserProperties.forEach(function (userProperty, i) {
                    teamTableUserRow = document.createElement("td");

                    if(userProperty === "userName"){
                        teamTableUserRow.innerHTML = retiredUser[userProperty] + " (retired)";
                    } else {
                        if(userProperty === "multipliedPoints"){
                            teamTableUserCell.setAttribute("data-toggle", "tooltip");
                            teamTableUserCell.setAttribute("data-placement", "left");
                            teamTableUserCell.setAttribute("title", "Unmultiplied: " + retiredUser["points"].toLocaleString());
                        }

                        teamTableUserRow.innerHTML = retiredUser[userProperty].toLocaleString();
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
        $('[data-toggle="tooltip"]').tooltip();
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
        hardwareTable.setAttribute("class", "table table-striped table-hover");

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

        $.each(jsonResponse, function(i, hardwareItem) {
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
        usersTable.setAttribute("class", "table table-striped table-hover");

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

        $.each(jsonResponse, function(i, usersItem) {
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
        teamsTable.setAttribute("class", "table table-striped table-hover");

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

        $.each(jsonResponse, function(i, teamsItem) {
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

$(document).ready(function() {
    loadTcStats();
    loadHardware();
    loadUsers();
    loadTeams();
    updateTime();
});
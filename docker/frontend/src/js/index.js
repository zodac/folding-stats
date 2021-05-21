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
        const teamTableUserProperties = ["rankInTeam", "displayName", "category", "hardware", "multipliedPoints", "units"];

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
            teamTitle.innerHTML = "Rank #" + team['rank'] + ": ";

            if ("forumLink" in team) {
                teamTitle.innerHTML += "<a href='" + team["forumLink"] + "' target='_blank'>" + team['teamName'] + "</a>";
            } else {
                teamTitle.innerHTML += team['teamName'];
            }
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
            activeUsers.sort(sortJsonByKey("rankInTeam"));
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
                    } else if (userProperty === "displayName") {
                        teamTableUserCell.innerHTML = activeUser[userProperty];

                        if (activeUser[userProperty] === captainName){
                            teamTableUserCell.innerHTML += " (Captain)";
                        }

                        if ("profileLink" in activeUser) {
                            teamTableUserCell.innerHTML = "<a href='" + activeUser["profileLink"] + "' target='_blank'>" + teamTableUserCell.innerHTML + "</a>"
                        }

                        if ("liveStatsLink" in activeUser) {
                            teamTableUserCell.innerHTML +=
                                " <a href='" + activeUser["liveStatsLink"] + "' target='_blank'>" +
                                    "<img alt='stats' src='./res/img/live.png' width='16px' height='16px'>" +
                                "</a>";
                        }

                        if(activeUser["displayName"] != activeUser["foldingName"]){
                            teamTableUserCell.setAttribute("data-bs-toggle", "tooltip");
                            teamTableUserCell.setAttribute("data-placement", "left");
                            teamTableUserCell.setAttribute("title", "Folding Username: " + activeUser["foldingName"]);
                            new bootstrap.Tooltip(teamTableUserCell);
                        }
                    } else if (userProperty === "hardware") {
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
            retiredUsers.sort(sortJsonByKey("rankInTeam"));
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

document.addEventListener("DOMContentLoaded", function(event) {
    loadTcStats();
    startTimer();

    // Enable toasts
//    var toastElList = [].slice.call(document.querySelectorAll('.toast'))
//    var toastList = toastElList.map(function (toastEl) {
//        return new bootstrap.Toast(toastEl)
//    })
});
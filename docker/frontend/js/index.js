const REST_ENDPOINT_URL="%REST_ENDPOINT_URL%"

// The 'toggle' functions below simply change the colour of the buttons. There must be a smarter way to do this...
function toggleMainButtonStyle(id, classList){
    var button = document.getElementById(id)

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary")
        button.classList.remove("btn-success")
    } else {
        button.classList.add("btn-success")
        button.classList.remove("btn-primary")
    }
}

function toggleTeam(teamNumber, classList) {
    var button = document.getElementById("team_"+teamNumber+"_button")

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary")
        button.classList.remove("btn-success")
    } else {
        button.classList.add("btn-success")
        button.classList.remove("btn-primary")
    }
}

function loadOverallStats() {
    fetch(REST_ENDPOINT_URL+'/stats/overall')
        .then(response => {
            return response.json()
        })
        .then(function(jsonResponse) {
        // Build competition stats
        const overallTableHeaders = ["Total Points", "Total Units"]
        const overallTableProperties = ["totalMultipliedPoints", "totalUnits"]

        overallDiv = document.getElementById("overall_div")

        overallTitle = document.createElement("h2")
        overallTitle.setAttribute("class", "navbar-brand")
        overallTitle.innerHTML = "Overall Stats"
        overallDiv.append(overallTitle)

        overallTable = document.createElement("table")
        overallTable.setAttribute("id", "overall")
        overallTable.setAttribute("class", "table table-dark table-striped table-hover")

        overallTableHead = document.createElement("thead")
        overallTableHeaderRow = document.createElement("tr")
        overallTableHeaders.forEach(function (header, i) {
            overallTableHeader = document.createElement("th")
            overallTableHeader.setAttribute("onclick", "sortTable("+i+", 'overall')")
            overallTableHeader.setAttribute("scope", "col")
            overallTableHeader.innerHTML = header

            overallTableHeaderRow.append(overallTableHeader)
        })
        overallTableHead.append(overallTableHeaderRow)
        overallTable.append(overallTableHead)

        overallTableBody = document.createElement("tbody")
        overallTableBodyRow = document.createElement("tr")
        overallTableProperties.forEach(function (property, i) {
            overallTableBodyCell = document.createElement("td")

            if(property === "totalMultipliedPoints"){
                overallTableBodyCell.setAttribute("data-bs-toggle", "tooltip")
                overallTableBodyCell.setAttribute("data-placement", "top")
                overallTableBodyCell.setAttribute("title", "Unmultiplied: " + jsonResponse["totalPoints"].toLocaleString())
                new bootstrap.Tooltip(overallTableBodyCell)
            }

            overallTableBodyCell.innerHTML = jsonResponse[property].toLocaleString()
            overallTableBodyRow.append(overallTableBodyCell)
        })
        overallTableBody.append(overallTableBodyRow)
        overallTable.append(overallTableBody)

        overallDiv.append(overallTable)
    })
}

function loadTeamLeaderboard() {
    fetch(REST_ENDPOINT_URL+"/stats/leaderboard")
    .then(response => {
        return response.json()
    })
    .then(function(jsonResponse){
        const leaderboardHeaders = ["Rank", "Team", "Points", "Units", "Points To Leader", "Points To Next", "Points/WU"]
        const leaderboardProperties = [ "rank", "teamName", "teamMultipliedPoints", "teamUnits", "diffToLeader", "diffToNext", "pointsPerUnit"]

        leaderboardDiv = document.getElementById("leaderboard_div")

        leaderboardTitle = document.createElement("h2")
        leaderboardTitle.setAttribute("class", "navbar-brand")
        leaderboardTitle.innerHTML = "Team Leaderboard"

        leaderboardTable = document.createElement("table")
        leaderboardTable.setAttribute("id", "leaderboard")
        leaderboardTable.setAttribute("class", "table table-dark table-striped table-hover")

        leaderboardTableHead = document.createElement("thead")
        leaderboardTableHeaderRow = document.createElement("tr")
        leaderboardHeaders.forEach(function (header, i) {
            leaderboardTableHeader = document.createElement("th")

            if(header != "Team" && header != "Rank"){
                leaderboardTableHeader.setAttribute("style", "text-align:right")
            }

            leaderboardTableHeader.setAttribute("onclick", "sortTable("+i+", 'leaderboard')")
            leaderboardTableHeader.setAttribute("scope", "col")
            leaderboardTableHeader.innerHTML = header

            leaderboardTableHeaderRow.append(leaderboardTableHeader)
        })
        leaderboardTableHead.append(leaderboardTableHeaderRow)
        leaderboardTable.append(leaderboardTableHead)

        leaderboardTableBody = document.createElement("tbody")

        jsonResponse.forEach(function(team, i){
            leaderboardTableBodyRow = document.createElement("tr")


            leaderboardProperties.forEach(function(property){
                leaderboardCell = document.createElement("td")

                if(property != "teamName" && property != "rank"){
                    leaderboardCell.setAttribute("style", "text-align:right")
                }

                if (property === "teamMultipliedPoints") {
                    leaderboardCell.setAttribute("data-bs-toggle", "tooltip")
                    leaderboardCell.setAttribute("data-placement", "top")
                    leaderboardCell.setAttribute("title", "Unmultiplied: " + team['teamPoints'].toLocaleString())
                    new bootstrap.Tooltip(leaderboardCell)
                    leaderboardCell.innerHTML = team['teamMultipliedPoints'].toLocaleString()
                } else if (property === "teamName") {
                    leaderboardCell.innerHTML = team['team']['teamName'].toLocaleString()
                } else if (property === "pointsPerUnit"){
                    if(team['teamPoints'] === 0 || team['teamUnits'] === 0){
                      leaderboardCell.innerHTML = "0"
                    } else {
                      leaderboardCell.setAttribute("data-bs-toggle", "tooltip")
                      leaderboardCell.setAttribute("data-placement", "top")
                      leaderboardCell.setAttribute("title", "Unmultiplied: " + (team['teamPoints']/team['teamUnits']).toLocaleString(undefined, {minimumFractionDigits: 0, maximumFractionDigits: 0}))
                      new bootstrap.Tooltip(leaderboardCell)
                      leaderboardCell.innerHTML = (team['teamMultipliedPoints']/team['teamUnits']).toLocaleString(undefined, {minimumFractionDigits: 0, maximumFractionDigits: 0})
                    }
                } else {
                    leaderboardCell.innerHTML = team[property].toLocaleString()
                }

                leaderboardTableBodyRow.append(leaderboardCell)
            })
            leaderboardTableBody.append(leaderboardTableBodyRow)
        })


        leaderboardTable.append(leaderboardTableBody)

        leaderboardDiv.append(leaderboardTitle)
        leaderboardDiv.append(leaderboardTable)
        hide("loader")
    })
}

function loadCategoryLeaderboard() {
    fetch(REST_ENDPOINT_URL+"/stats/category")
    .then(response => {
        return response.json()
    })
    .then(function(jsonResponse){
        categoryDiv = document.getElementById("category_div")

        const categoryHeaders = ["Rank", "User", "Hardware", "Points", "Units", "Points to Leader", "Points to Next"]
        const categoryProperties = ["rank", "displayName", "hardware", "multipliedPoints", "units", "diffToLeader", "diffToNext"]

        categoryLeaderboardTitle = document.createElement("h2")
        categoryLeaderboardTitle.setAttribute("class", "navbar-brand")
        categoryLeaderboardTitle.innerHTML = "Category Leaderboard"
        categoryDiv.append(categoryLeaderboardTitle)

        Object.keys(jsonResponse).forEach(function(key) {
            var keyDisplay = getCategoryFrontend(key)
            categoryTitle = document.createElement("h2")
            categoryTitle.setAttribute("class", "navbar-brand")
            categoryTitle.innerHTML = keyDisplay
            categoryDiv.append(categoryTitle)

            tableId = "category_" + key.replace(/\s+/g, "_").toLowerCase()

            categoryTable = document.createElement("table")
            categoryTable.setAttribute("id", tableId)
            categoryTable.setAttribute("class", "table table-dark table-striped table-hover")

            categoryTableHead = document.createElement("thead")
            categoryTableHeaderRow = document.createElement("tr")
            categoryHeaders.forEach(function (header, i) {
                categoryTableHeader = document.createElement("th")
                categoryTableHeader.setAttribute("onclick", "sortTable("+i+", '"+tableId+"')")
                categoryTableHeader.setAttribute("scope", "col")
                categoryTableHeader.innerHTML = header

                categoryTableHeaderRow.append(categoryTableHeader)
            })
            categoryTableHead.append(categoryTableHeaderRow)
            categoryTable.append(categoryTableHead)

            categoryTableBody = document.createElement("tbody")

            users = jsonResponse[key]
            users.forEach(function(user){
                categoryTableBodyRow = document.createElement("tr")

                categoryProperties.forEach(function(property){
                    categoryCell = document.createElement("td")

                    if(property === "multipliedPoints") {
                        categoryCell.setAttribute("data-bs-toggle", "tooltip")
                        categoryCell.setAttribute("data-placement", "top")
                        categoryCell.setAttribute("title", "Unmultiplied: " + user['points'].toLocaleString())
                        new bootstrap.Tooltip(categoryCell)
                        categoryCell.innerHTML = user['multipliedPoints'].toLocaleString()
                    } else if (property === "displayName") {
                        categoryCell.innerHTML = user['user']['displayName'].toLocaleString()
                    } else if (property === "hardware") {
                        categoryCell.innerHTML = user['user']['hardware']['displayName'].toLocaleString()
                    } else {
                        categoryCell.innerHTML = user[property].toLocaleString()
                    }

                    categoryTableBodyRow.append(categoryCell)
                })
                categoryTableBody.append(categoryTableBodyRow)
            })
            categoryTable.append(categoryTableBody)

            categoryDiv.append(categoryTable)
            categoryDiv.append(document.createElement("br"))
        })
    })
}

function loadTeamStats() {
    fetch(REST_ENDPOINT_URL+'/stats')
    .then(response => {
        return response.json()
    })
    .then(function(jsonResponse) {
        statsDiv = document.getElementById("stats_div")

        statsTitle = document.createElement("h2")
        statsTitle.setAttribute("class", "navbar-brand")
        statsTitle.innerHTML = "Individual Team Stats"
        statsDiv.append(statsTitle)

        // Build team tables
        const teamTableHeaders = ["Rank", "User", "Category", "Hardware", "Points", "Units"]
        const activeUserProperties = ["rankInTeam", "displayName", "category", "hardware", "multipliedPoints", "units"]

        var jsonResponseTeams = jsonResponse['teams']
        jsonResponseTeams.sort(sortJsonByKey("rank"))
        jsonResponseTeams.forEach(function(team, i) {
            var teamNumber = (i+1)
            var captainName = team['captainName']

            teamDiv = document.createElement("div")
            teamDiv.setAttribute("id", "team_"+teamNumber+"_div")

            metadataDiv = document.createElement("div")
            metadataDiv.setAttribute("id", "team_"+teamNumber+"_metadata")

            teamTitle = document.createElement("h2")
            teamTitle.setAttribute("class", "navbar-brand")
            teamTitle.innerHTML = "Rank #" + team['rank'] + ": "

            if ("forumLink" in team['team']) {
                teamTitle.innerHTML += "<a href='" + team['team']['forumLink'] + "' target='_blank' rel='noreferrer'>" + team['team']['teamName'] + "</a>"
            } else {
                teamTitle.innerHTML += team['team']['teamName']
            }
            metadataDiv.append(teamTitle)

            teamStats = document.createElement("h5")
            teamStats.setAttribute("id", "team_"+teamNumber+"_stats")
            teamStats.setAttribute("data-bs-toggle", "tooltip")
            teamStats.setAttribute("data-placement", "top")
            teamStats.setAttribute("title", "Unmultiplied: " + team['teamPoints'].toLocaleString())
            teamStats.innerHTML = team['teamMultipliedPoints'].toLocaleString() + " points | " + team['teamUnits'].toLocaleString() + " units"
            new bootstrap.Tooltip(teamStats)
            metadataDiv.append(teamStats)

            teamButton = document.createElement('button')
            teamButton.setAttribute("id", "team_"+teamNumber+"_button")
            teamButton.setAttribute("class", "btn ui-btn btn-primary")
            teamButton.setAttribute("href", "#team_"+teamNumber+"_subdiv")
            teamButton.setAttribute("onclick", "toggleTeam("+teamNumber+", this.classList)")
            teamButton.setAttribute("data-bs-toggle", "collapse")
            teamButton.setAttribute("role", "button")
            teamButton.innerHTML = "Show/Hide"
            metadataDiv.append(teamButton)
            teamDiv.append(metadataDiv)

            subDiv = document.createElement("div")
            subDiv.setAttribute("id", "team_"+teamNumber+"_subdiv")
            subDiv.setAttribute("class", "collapse")

            teamTable = document.createElement("table")
            teamTable.setAttribute("id", "team_"+teamNumber)
            teamTable.setAttribute("class", "table table-dark table-striped table-hover")

            teamTableHead = document.createElement("thead")
            teamTableHeaderRow = document.createElement("tr")
            teamTableHeaders.forEach(function (header, i) {
                teamTableHeaderCell = document.createElement("th")
                teamTableHeaderCell.setAttribute("onclick", "sortTable("+i+", 'team_"+teamNumber+"')")
                teamTableHeaderCell.setAttribute("scope", "col")
                teamTableHeaderCell.innerHTML = header

                teamTableHeaderRow.append(teamTableHeaderCell)
            })
            teamTableHead.append(teamTableHeaderRow)
            teamTable.append(teamTableHead)

            teamTableBody = document.createElement("tbody")

            activeUsers = team['activeUsers']
            activeUsers.sort(sortJsonByKey("rankInTeam"))
            activeUsers.forEach(function (activeUser, i) {
                teamTableBodyRow = document.createElement("tr")

                activeUserProperties.forEach(function (userProperty, i) {
                    teamTableUserCell = document.createElement("td")

                    if(userProperty === "multipliedPoints"){
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip")
                        teamTableUserCell.setAttribute("data-placement", "left")
                        teamTableUserCell.setAttribute("title", "Unmultiplied: " + activeUser['points'].toLocaleString())
                        teamTableUserCell.innerHTML = activeUser['multipliedPoints'].toLocaleString()
                        new bootstrap.Tooltip(teamTableUserCell)
                    } else if (userProperty === "displayName") {
                        teamTableUserCell.innerHTML = activeUser['user']['displayName']

                        if (activeUser['user']['displayName'] === captainName){
                            teamTableUserCell.innerHTML += " (Captain)"
                        }

                        if ("profileLink" in activeUser['user']) {
                            teamTableUserCell.innerHTML = "<a href='" + activeUser['user']['profileLink'] + "' target='_blank' rel='noreferrer'>" +
                                teamTableUserCell.innerHTML + "</a>"
                        }

                        if ("liveStatsLink" in activeUser['user']) {
                            teamTableUserCell.innerHTML +=
                                " <a href='" + activeUser['user']['liveStatsLink'] + "' target='_blank' rel='noreferrer'>" +
                                    "<img alt='stats' src='./res/img/live.png' width='16px' height='16px'>" +
                                "</a>"
                        }

                        if(activeUser['user']['displayName'] != activeUser['user']['foldingUserName']){
                            teamTableUserCell.setAttribute("data-bs-toggle", "tooltip")
                            teamTableUserCell.setAttribute("data-placement", "left")
                            teamTableUserCell.setAttribute("title", "Folding Username: " + activeUser['user']['foldingUserName'])
                            new bootstrap.Tooltip(teamTableUserCell)
                        }
                    } else if (userProperty === "hardware") {
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip")
                        teamTableUserCell.setAttribute("data-placement", "left")
                        teamTableUserCell.setAttribute("title", "Multiplier: x" + activeUser['user']['hardware']['multiplier'].toLocaleString())
                        teamTableUserCell.innerHTML = activeUser['user']['hardware']['displayName'].toLocaleString()
                        new bootstrap.Tooltip(teamTableUserCell)
                    } else if (userProperty === "category") {
                        teamTableUserCell.innerHTML = getCategoryFrontend(activeUser['user']['category'])
                    } else {
                        if (userProperty in activeUser) {
                            teamTableUserCell.innerHTML = activeUser[userProperty].toLocaleString()
                        } else if (userProperty in activeUser['user']) {
                            teamTableUserCell.innerHTML = activeUser['user'][userProperty].toLocaleString()
                        }
                    }

                    teamTableBodyRow.append(teamTableUserCell)
                })
                teamTableBody.append(teamTableBodyRow)
            })

            retiredUsers = team['retiredUsers']
            retiredUsers.sort(sortJsonByKey("rankInTeam"))
            retiredUsers.forEach(function (retiredUser, i) {
                teamTableBodyRow = document.createElement("tr")

                activeUserProperties.forEach(function (userProperty, i) {
                    teamTableUserCell = document.createElement("td")

                    if (userProperty === "multipliedPoints") {
                        teamTableUserCell.setAttribute("data-bs-toggle", "tooltip")
                        teamTableUserCell.setAttribute("data-placement", "left")
                        teamTableUserCell.setAttribute("title", "Unmultiplied: " + retiredUser['points'].toLocaleString())
                        teamTableUserCell.innerHTML = retiredUser[userProperty].toLocaleString()
                        new bootstrap.Tooltip(teamTableUserCell)
                    } else if (userProperty === "category") {
                        teamTableUserCell.innerHTML = "Retired"
                    } else {
                        if (userProperty in retiredUser) {
                            teamTableUserCell.innerHTML = retiredUser[userProperty].toLocaleString()
                        }
                    }

                    teamTableBodyRow.append(teamTableUserCell)
                })
                teamTableBody.append(teamTableBodyRow)
            })
            teamTable.append(teamTableBody)
            subDiv.append(teamTable)

            teamDiv.append(subDiv)

            statsDiv.append(teamDiv)
            statsDiv.append(document.createElement("br"))
        })
    })
}

document.addEventListener("DOMContentLoaded", function(event) {
    loadOverallStats()
    loadTeamLeaderboard()
    loadCategoryLeaderboard()
    loadTeamStats()
    startTimer()
})

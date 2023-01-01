const REST_ENDPOINT_URL="%REST_ENDPOINT_URL%"

const FIRST_YEAR_WITH_RESULTS = 2021
const NUMBER_OF_MONTHS = 12
const NUMBER_OF_YEARS_TO_SHOW = (new Date().getUTCFullYear() - FIRST_YEAR_WITH_RESULTS) + 1
const YEAR_START = new Date().getUTCFullYear()

var currentDate = new Date()
var currentUtcDate = new Date(currentDate.getUTCFullYear(), currentDate.getUTCMonth(), currentDate.getUTCDate(), currentDate.getUTCHours(), currentDate.getUTCMinutes(), currentDate.getUTCSeconds())

// Load previous month by default
// December is month 0, so force it to month 12
var selectedMonth = currentUtcDate.getMonth() == 0 ? 12 : currentUtcDate.getMonth()

// If month is December, choose previous year
var selectedYear = selectedMonth == 12 ? currentUtcDate.getFullYear() - 1: currentUtcDate.getFullYear()
var selectedMonthName = new Date(selectedYear, (selectedMonth-1), 1).toLocaleString("default", { month: "long" })

function getPastResult(month, monthName, year) {
    if (month != null){
        selectedMonth = month
    }

    if (monthName != null) {
        selectedMonthName = monthName
        monthDropdownTitle = document.getElementById("month_dropdown_root")
        monthDropdownTitle.innerHTML = selectedMonthName
    }

    if (year != null) {
        selectedYear = year
        yearDropdownTitle = document.getElementById("year_dropdown_root")
        yearDropdownTitle.innerHTML = selectedYear
    }

    show("loader")
    hide("past_result")

    fetch(REST_ENDPOINT_URL+"/results/result/" + selectedYear + "/" + selectedMonth)
    .then(response => {
        return response.json()
    })
    .then(function(jsonResponse) {
        leaderboardDiv = document.getElementById("leaderboard_div")
        while (leaderboardDiv.firstChild) {
            leaderboardDiv.removeChild(leaderboardDiv.lastChild)
        }

        categoryDiv = document.getElementById("category_div")
        while (categoryDiv.firstChild) {
            categoryDiv.removeChild(categoryDiv.lastChild)
        }

        loadTeamLeaderboard(jsonResponse['teamLeaderboard'])
        loadCategoryLeaderboard(jsonResponse['userCategoryLeaderboard'])

        hide("loader")
        show("past_result")
    })
    .catch((error) => {
        hide("loader")
        console.error("Unexpected error loading result: ", error)
        return false
    })
}

function loadTeamLeaderboard(jsonResponse) {
    const leaderboardHeaders = ["Rank", "Team", "Points", "Units", "Points To Leader", "Points To Next"]
    const leaderboardProperties = [ "rank", "teamName", "teamMultipliedPoints", "teamUnits", "diffToLeader", "diffToNext"]

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

            if (property === "teamMultipliedPoints") {
                leaderboardCell.setAttribute("data-bs-toggle", "tooltip")
                leaderboardCell.setAttribute("data-placement", "top")
                leaderboardCell.setAttribute("title", "Unmultiplied: " + team['teamPoints'].toLocaleString())
                new bootstrap.Tooltip(leaderboardCell)
                leaderboardCell.innerHTML = team['teamMultipliedPoints'].toLocaleString()
            } else if (property === "teamName") {
                leaderboardCell.innerHTML = team['team']['teamName'].toLocaleString()
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
}

function loadCategoryLeaderboard(jsonResponse) {
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

        tableId = "category_" + key.replace(/\s+/g,"_").toLowerCase()

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
}

function populateMonthDropdown() {
    var dropdownId = "month_dropdown"
    var dropdown = document.getElementById(dropdownId)
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild)
    }

    monthDropdownDiv = document.getElementById(dropdownId)

    for (i = 0; i < NUMBER_OF_MONTHS; i++) {
        var loopMonthName = new Date(YEAR_START, i, 1).toLocaleString("default", { month: "long" })

        monthButton = document.createElement("button")
        monthButton.setAttribute("class", "dropdown-item")
        monthButton.setAttribute("type", "button")
        monthButton.setAttribute("onclick", "getPastResult("+(i+1)+",'"+loopMonthName+"',null)")
        monthButton.innerHTML = loopMonthName

        monthDropdownDiv.append(monthButton)
    }

    monthDropdownTitle = document.getElementById(dropdownId + "_root")
    monthDropdownTitle.innerHTML = selectedMonthName
}

function populateYearDropdown() {
    var dropdownId = "year_dropdown"
    var dropdown = document.getElementById(dropdownId)
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild)
    }

    yearDropdownDiv = document.getElementById(dropdownId)

    for (i = 0; i < NUMBER_OF_YEARS_TO_SHOW; i++) {
        var loopYear = (YEAR_START - i)

        yearButton = document.createElement("button")
        yearButton.setAttribute("class", "dropdown-item")
        yearButton.setAttribute("type", "button")
        yearButton.setAttribute("onclick", "getPastResult(null,null,"+loopYear+")")
        yearButton.innerHTML = loopYear

        yearDropdownDiv.append(yearButton)
    }

    yearDropdownTitle = document.getElementById(dropdownId + "_root")
    yearDropdownTitle.innerHTML = selectedYear
}

document.addEventListener("DOMContentLoaded", function(event) {
    populateMonthDropdown()
    populateYearDropdown()
    updateTimer()
    getPastResult()
})

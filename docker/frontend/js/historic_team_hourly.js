/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
const REST_ENDPOINT_URL="%REST_ENDPOINT_URL%"

var currentDate = new Date()
var currentUtcDate = new Date(currentDate.getUTCFullYear(), currentDate.getUTCMonth(), currentDate.getUTCDate(), currentDate.getUTCHours(), currentDate.getUTCMinutes(), currentDate.getUTCSeconds())

var selectedTeamId = 0
var selectedTeam = ""
var selectedMonth = (currentUtcDate.getMonth()+1)
var selectedYear = currentUtcDate.getFullYear()
var selectedMonthName = new Date(selectedYear, (selectedMonth-1), 1).toLocaleString("default", { month: "long" })
var selectedDay = currentDate.getUTCDate()

function getTeamHistoricStats(teamId, teamName, day, month, monthName, year) {
    if(teamId != 0){
        selectedTeamId = teamId
    }

    if(teamName != null){
        selectedTeam = teamName
        teamDropdownTitle = document.getElementById("team_dropdown_root")
        teamDropdownTitle.innerHTML = selectedTeam
    }

    if(day != null) {
        selectedDay = day
        dayDropdownTitle = document.getElementById("day_dropdown_root")
        dayDropdownTitle.innerHTML = ordinalSuffixOf(selectedDay)
    }

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

    if(selectedTeam === "" || selectedTeamId === 0){
        return
    }

    show("loader")
    hide("historic_stats")

    fetch(REST_ENDPOINT_URL+"/historic/teams/" + selectedTeamId + "/" + selectedYear + "/" + selectedMonth + "/" + selectedDay)
    .then(response => {
        return response.json()
    })
    .then(function(jsonResponse) {
        // Clear existing entries in div
        historicDiv = document.getElementById("historic_stats")
        while (historicDiv.firstChild) {
            historicDiv.removeChild(historicDiv.lastChild)
        }

        const headers = ["Hour", "Points", "Units"]
        historicTable = document.createElement("table")
        historicTable.setAttribute("id", "historic_table")
        historicTable.setAttribute("class", "table table-dark table-striped table-hover")

        tableHead = document.createElement("thead")
        tableHeaderRow = document.createElement("tr")
        headers.forEach(function (header, i) {
            tableHeader = document.createElement("th")
            tableHeader.setAttribute("onclick", "sortTable(" + i + ", 'historic_table')")
            tableHeader.setAttribute("scope", "col")
            tableHeader.innerHTML = header

            tableHeaderRow.append(tableHeader)
        })
        tableHead.append(tableHeaderRow)
        historicTable.append(tableHead)


        tableBody = document.createElement("tbody")
        jsonResponse.forEach(function(statsEntry, i){
            tableRow = document.createElement("tr")

            dateCell = document.createElement("td")
            dateCell.innerHTML = leftPad(statsEntry['dateTime']['time']['hour'], 2, "0") + ":00"
            tableRow.append(dateCell)

            pointsCell = document.createElement("td")
            pointsCell.setAttribute("data-bs-toggle", "tooltip")
            pointsCell.setAttribute("data-placement", "top")
            pointsCell.setAttribute("title", "Unmultiplied: " + statsEntry['points'].toLocaleString())
            pointsCell.innerHTML = statsEntry['multipliedPoints'].toLocaleString()
            new bootstrap.Tooltip(pointsCell)
            tableRow.append(pointsCell)

            unitsCell = document.createElement("td")
            unitsCell.innerHTML = statsEntry['units'].toLocaleString()
            tableRow.append(unitsCell)
            tableBody.append(tableRow)
        })

        historicTable.append(tableBody)
        historicDiv.append(historicTable)

        hide("loader")
        show("historic_stats")
    })
}

document.addEventListener("DOMContentLoaded", function(event) {
    populateTeamDropdown()
    populateDayDropdown(selectedMonth, selectedYear, "day_dropdown", "getTeamHistoricStats")
    populateMonthDropdown("month_dropdown", "getTeamHistoricStats")
    populateYearDropdown("year_dropdown", "getTeamHistoricStats")
    updateTimer()
})

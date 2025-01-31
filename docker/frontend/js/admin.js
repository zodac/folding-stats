/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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
const REST_ENDPOINT_URL = "%REST_ENDPOINT_URL%"

String.prototype.format = function () {
    var formatted = this
    for (var i = 0; i < arguments.length; i++) {
        var regexp = new RegExp('\\{' + i + '\\}', 'gi')
        formatted = formatted.replace(regexp, arguments[i])
    }
    return formatted
}

function adminLogin() {
    var userName = document.getElementById("login_username").value
    var password = document.getElementById("login_password").value
    var authorizationPayload = "Basic " + btoa(userName + ":" + password)

    var requestData = JSON.stringify(
        {
            "encodedUserNameAndPassword": authorizationPayload
        }
    )

    show("loader")

    fetch(REST_ENDPOINT_URL + "/login/admin", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: requestData
    })
        .then(response => {
            document.getElementById("login_username").value = ""
            document.getElementById("login_password").value = ""

            if (response.status != HTTP_OK) {
                failureToast("Invalid admin credentials!")
                hide("loader")
                return
            }

            successToast("Logged in successfully!")
            hide("login_form")

            sessionSet("Authorization", authorizationPayload)
            loadHardware()
            loadTeams()
            loadUsersAdmin()
            loadPendingUserChangesAdmin()
            loadCompletedUserChangesAdmin()

            hide("loader")
            show("admin_functions")
        })
        .catch((error) => {
            hide("loader")
            console.error("Unexpected error logging in: ", error)
            return false
        })
}

function manualUpdate() {
    show("loader")
    fetch(REST_ENDPOINT_URL + "/stats/manual/update", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
        .then(response => {
            hide("loader")

            if (response.status != HTTP_OK) {
                failureToast("Manual update failed with code: " + response.status)
                response.json()
                    .then(response => {
                        console.error(JSON.stringify(response, null, 2))
                    })
                return
            }
            successToast("Stats manually updated")
        })
        .catch((error) => {
            hide("loader")
            console.error("Unexpected error updating stats: ", error)
            return false
        })
}

function manualLars() {
    show("loader")
    fetch(REST_ENDPOINT_URL + "/debug/lars", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
        .then(response => {
            hide("loader")

            if (response.status != HTTP_OK) {
                failureToast("Manual LARS update failed with code: " + response.status)
                response.json()
                    .then(response => {
                        console.error(JSON.stringify(response, null, 2))
                    })
                return
            }

            successToast("LARS data manually updated")
            loadHardware()
        })
        .catch((error) => {
            hide("loader")
            console.error("Unexpected error updating LARS: ", error)
            return false
        })
}

function manualResultSave() {
    show("loader")
    fetch(REST_ENDPOINT_URL + "/results/manual/save", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
        .then(response => {
            hide("loader")

            if (response.status != HTTP_OK) {
                failureToast("Manual result save failed with code: " + response.status)
                response.json()
                    .then(response => {
                        console.error(JSON.stringify(response, null, 2))
                    })
                return
            }
            successToast("Result manually saved")
        })
        .catch((error) => {
            hide("loader")
            console.error("Unexpected error saving result: ", error)
            return false
        })
}

function printCaches() {
    show("loader")
    fetch(REST_ENDPOINT_URL + "/debug/caches", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
        .then(response => {
            hide("loader")

            if (response.status != HTTP_OK) {
                failureToast("Printing caches failed with code: " + response.status)
                response.json()
                    .then(response => {
                        console.error(JSON.stringify(response, null, 2))
                    })
                return
            }
            successToast("Caches printed")
        })
        .catch((error) => {
            hide("loader")
            console.error("Unexpected error printing caches: ", error)
            return false
        })
}

function loadHardware() {
    fetch(REST_ENDPOINT_URL + "/hardware")
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            // Build hardware
            jsonResponse.sort(sortJsonByKey("id"))
            const hardwareHeaders = ["ID", "Name", "Display Name", "Make", "Type", "Multiplier", "Average PPD"]
            const hardwareProperties = ["id", "hardwareName", "displayName", "hardwareMake", "hardwareType", "multiplier", "averagePpd"]

            // Empty div of existing content, if any
            hardwareDiv = document.getElementById("hardware_div")
            while (hardwareDiv.firstChild) {
                hardwareDiv.removeChild(hardwareDiv.lastChild)
            }

            hardwareTitle = document.createElement("h2")
            hardwareTitle.setAttribute("class", "navbar-brand")
            hardwareTitle.innerHTML = "Hardware"
            hardwareDiv.append(hardwareTitle)

            hardwareTableDiv = document.createElement("div")
            hardwareTableDiv.setAttribute("class", "scrollable-table")
            hardwareTable = document.createElement("table")
            hardwareTable.setAttribute("id", "hardware")
            hardwareTable.setAttribute("class", "table table-dark table-striped table-hover")

            hardwareTableHead = document.createElement("thead")
            hardwareTableHeaderRow = document.createElement("tr")
            hardwareHeaders.forEach(function (header, i) {
                hardwareTableHeader = document.createElement("th")
                hardwareTableHeader.setAttribute("onclick", "sortTable(" + i + ", 'hardware')")
                hardwareTableHeader.setAttribute("scope", "col")
                hardwareTableHeader.innerHTML = header

                hardwareTableHeaderRow.append(hardwareTableHeader)
            })
            hardwareTableHead.append(hardwareTableHeaderRow)
            hardwareTable.append(hardwareTableHead)


            hardwareTableBody = document.createElement("tbody")

            jsonResponse.forEach(function (hardwareItem) {
                // Update hardware display table
                hardwareTableBodyRow = document.createElement("tr")
                hardwareProperties.forEach(function (hardwareProperty) {
                    hardwareTableBodyCell = document.createElement("td")

                    if (hardwareProperty === "multiplier") {
                        hardwareTableBodyCell.innerHTML = "x" + hardwareItem['multiplier'].toLocaleString()
                    } else if (hardwareProperty === "hardwareMake") {
                        hardwareTableBodyCell.innerHTML = getHardwareMakeFrontend(hardwareItem['hardwareMake'])
                    } else if (hardwareProperty === "hardwareType") {
                        hardwareTableBodyCell.innerHTML = getHardwareTypeFrontend(hardwareItem['hardwareType'])
                    } else {
                        hardwareTableBodyCell.innerHTML = hardwareItem[hardwareProperty].toLocaleString()
                    }

                    hardwareTableBodyRow.append(hardwareTableBodyCell)
                })
                hardwareTableBody.append(hardwareTableBodyRow)
            })
            hardwareTable.append(hardwareTableBody)

            hardwareTableDiv.append(hardwareTable)
            hardwareDiv.append(hardwareTableDiv)

            // Update any list that needs all hardware
            jsonResponse.sort(sortJsonByKey("hardwareName"))

            hardwareDataLists = document.querySelectorAll(".hardware_datalist")
            for (var i = 0, hardwareDataList; hardwareDataList = hardwareDataLists[i]; i++) {
                // Clear existing entries
                while (hardwareDataList.firstChild) {
                    hardwareDataList.removeChild(hardwareDataList.lastChild)
                }

                // Add entries
                jsonResponse.forEach(function (hardwareItem) {
                    hardwareOption = document.createElement("option")
                    hardwareOption.setAttribute("value", hardwareItem['hardwareName'])
                    hardwareOption.innerHTML = hardwareItem['displayName']
                    hardwareDataList.append(hardwareOption)
                })
            }
        })
}

function loadUsersAdmin() {
    var url = encodeURI(REST_ENDPOINT_URL + "/users/all/passkey")
    fetch(url, {
        headers: {
            "Authorization": sessionGet("Authorization")
        }
    })
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            // Build users
            jsonResponse.sort(sortJsonByKey("id"))
            const usersHeaders = ["ID", "User", "Folding Name", "Passkey", "Category", "Profile Link", "Live Stats Link", "Hardware", "Team", "Role"]
            const usersProperties = ["id", "displayName", "foldingUserName", "passkey", "category", "profileLink", "liveStatsLink", "hardware", "team", "role"]

            // Empty div of existing content, if any
            usersDiv = document.getElementById("users_div")
            while (usersDiv.firstChild) {
                usersDiv.removeChild(usersDiv.lastChild)
            }

            usersTitle = document.createElement("h2")
            usersTitle.setAttribute("class", "navbar-brand")
            usersTitle.innerHTML = "Users"
            usersDiv.append(usersTitle)

            usersTableDiv = document.createElement("div")
            usersTableDiv.setAttribute("class", "scrollable-table")
            usersTable = document.createElement('table')
            usersTable.setAttribute("id", "users")
            usersTable.setAttribute("class", "table table-dark table-striped table-hover")

            usersTableHead = document.createElement("thead")
            usersTableHeaderRow = document.createElement("tr")
            usersHeaders.forEach(function (header, i) {
                usersTableHeader = document.createElement("th")
                usersTableHeader.setAttribute("onclick", "sortTable(" + i + ", 'users')")
                usersTableHeader.setAttribute("scope", "col")
                usersTableHeader.innerHTML = header

                usersTableHeaderRow.append(usersTableHeader)
            })
            usersTableHead.append(usersTableHeaderRow)
            usersTable.append(usersTableHead)

            usersTableBody = document.createElement("tbody")

            jsonResponse.forEach(function (usersItem) {
                // Update users display table
                usersTableBodyRow = document.createElement("tr")
                usersProperties.forEach(function (usersProperty) {
                    usersTableBodyCell = document.createElement("td")

                    if (usersProperty === "liveStatsLink" || usersProperty === "profileLink") {
                        if (usersProperty in usersItem) {
                            link = document.createElement('a')
                            link.setAttribute("href", usersItem[usersProperty])
                            link.innerHTML = usersItem[usersProperty]

                            usersTableBodyCell.append(link)
                        }
                    } else if (usersProperty === "hardware") {
                        usersTableBodyCell.innerHTML = usersItem['hardware']['hardwareName'].toLocaleString()
                    } else if (usersProperty === "team") {
                        usersTableBodyCell.innerHTML = usersItem['team']['teamName'].toLocaleString()
                    } else if (usersProperty === "category") {
                        usersTableBodyCell.innerHTML = getCategoryFrontend(usersItem[usersProperty])
                    } else if (usersProperty === "role") {
                        usersTableBodyCell.innerHTML = getRoleFrontend(usersItem[usersProperty])
                    } else {
                        if (usersProperty in usersItem) {
                            usersTableBodyCell.innerHTML = usersItem[usersProperty].toLocaleString()
                        }
                    }
                    usersTableBodyRow.append(usersTableBodyCell)
                })
                usersTableBody.append(usersTableBodyRow)
            })
            usersTable.append(usersTableBody)

            usersTableDiv.append(usersTable)
            usersDiv.append(usersTableDiv)

            // Update any list that needs all users
            jsonResponse.sort(sortJsonByKey("displayName"))

            userDataLists = document.querySelectorAll(".user_datalist")
            for (var i = 0, userDataList; userDataList = userDataLists[i]; i++) {
                // Clear existing entries
                while (userDataList.firstChild) {
                    userDataList.removeChild(userDataList.lastChild)
                }

                // Add entries
                jsonResponse.forEach(function (userItem) {
                    userOption = document.createElement("option")
                    userOption.setAttribute("value", userItem['id'] + ": " + userItem['displayName'])
                    userOption.innerHTML = userItem['foldingUserName'] + " (" + getCategoryFrontend(userItem['category']) + ", " + userItem['team']['teamName'] + ")"
                    userDataList.append(userOption)
                })
            }
        })
}

function loadTeams() {
    fetch(REST_ENDPOINT_URL + "/teams")
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            // Build teams
            jsonResponse.sort(sortJsonByKey("id"))
            const teamsHeaders = ["ID", "Name", "Description", "Forum Link"]
            const teamsProperties = ["id", "teamName", "teamDescription", "forumLink"]

            // Empty div of existing content, if any
            teamsDiv = document.getElementById("teams_div")
            while (teamsDiv.firstChild) {
                teamsDiv.removeChild(teamsDiv.lastChild)
            }

            teamsTitle = document.createElement("h2")
            teamsTitle.setAttribute("class", "navbar-brand")
            teamsTitle.innerHTML = "Teams"
            teamsDiv.append(teamsTitle)

            teamsTableDiv = document.createElement("div")
            teamsTableDiv.setAttribute("class", "scrollable-table")
            teamsTable = document.createElement("table")
            teamsTable.setAttribute("id", "teams")
            teamsTable.setAttribute("class", "table table-dark table-striped table-hover")

            teamsTableHead = document.createElement("thead")
            teamsTableHeaderRow = document.createElement("tr")
            teamsHeaders.forEach(function (header, i) {
                teamsTableHeader = document.createElement("th")
                teamsTableHeader.setAttribute("onclick", "sortTable(" + i + ", 'teams')")
                teamsTableHeader.setAttribute("scope", "col")
                teamsTableHeader.innerHTML = header

                teamsTableHeaderRow.append(teamsTableHeader)
            })
            teamsTableHead.append(teamsTableHeaderRow)
            teamsTable.append(teamsTableHead)

            teamsTableBody = document.createElement("tbody")

            jsonResponse.forEach(function (teamsItem) {
                teamsTableBodyRow = document.createElement("tr")
                teamsProperties.forEach(function (teamsProperty) {
                    teamsTableBodyCell = document.createElement("td")

                    if (teamsProperty === "forumLink") {
                        if (teamsProperty in teamsItem) {
                            link = document.createElement('a')
                            link.setAttribute("href", teamsItem[teamsProperty])
                            link.setAttribute("target", "_blank")
                            link.setAttribute("rel", "noreferrer")
                            link.innerHTML = teamsItem[teamsProperty]

                            teamsTableBodyCell.append(link)
                        }
                    } else {
                        if (teamsProperty in teamsItem) {
                            teamsTableBodyCell.innerHTML = teamsItem[teamsProperty].toLocaleString()
                        }
                    }
                    teamsTableBodyRow.append(teamsTableBodyCell)
                })
                teamsTableBody.append(teamsTableBodyRow)
            })
            teamsTable.append(teamsTableBody)

            teamsTableDiv.append(teamsTable)
            teamsDiv.append(teamsTableDiv)

            // Update any list that needs all teams
            jsonResponse.sort(sortJsonByKey("teamName"))
            teamDataLists = document.querySelectorAll(".team_datalist")
            for (var i = 0, teamDataList; teamDataList = teamDataLists[i]; i++) {
                // Clear existing entries
                while (teamDataList.firstChild) {
                    teamDataList.removeChild(teamDataList.lastChild)
                }

                // Add entries
                jsonResponse.forEach(function (teamItem) {
                    teamOption = document.createElement("option")
                    teamOption.setAttribute("value", teamItem['teamName'])
                    teamDataList.append(teamOption)
                })
            }
        })
}

function loadUserChangesAdmin(states, idPrefix, title) {
    var url = encodeURI(REST_ENDPOINT_URL + "/changes/passkey?state=" + states)
    fetch(url, {
        headers: {
            "Authorization": sessionGet("Authorization")
        }
    })
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            // Build users
            jsonResponse.sort(sortJsonByKey("id"))

            if (idPrefix === "pending_user_changes") {
                usersHeaders = ["Folding Name", "Passkey", "Live Stats Link", "Hardware", "Created", "Updated", "State", "Approve/Reject"]
                usersProperties = ["foldingUserName", "passkey", "liveStatsLink", "hardware", "createdUtcTimestamp", "updatedUtcTimestamp", "state", "approve"]
            } else {
                usersHeaders = ["Folding Name", "Passkey", "Live Stats Link", "Hardware", "Created", "Updated", "State"]
                usersProperties = ["foldingUserName", "passkey", "liveStatsLink", "hardware", "createdUtcTimestamp", "updatedUtcTimestamp", "state"]
            }

            // Empty div of existing content, if any
            usersDiv = document.getElementById(idPrefix + "_div")
            while (usersDiv.firstChild) {
                usersDiv.removeChild(usersDiv.lastChild)
            }

            usersTitle = document.createElement("h2")
            usersTitle.setAttribute("class", "navbar-brand")
            usersTitle.innerHTML = title
            usersDiv.append(usersTitle)

            usersTableDiv = document.createElement("div")
            usersTableDiv.setAttribute("class", "scrollable-table")
            usersTable = document.createElement('table')
            usersTable.setAttribute("id", idPrefix + "_table")
            usersTable.setAttribute("class", "table table-dark table-striped table-hover")

            usersTableHead = document.createElement("thead")
            usersTableHeaderRow = document.createElement("tr")
            usersHeaders.forEach(function (header, i) {
                usersTableHeader = document.createElement("th")
                usersTableHeader.setAttribute("onclick", "sortTable(" + i + ", '" + idPrefix + "_table')")
                usersTableHeader.setAttribute("scope", "col")
                usersTableHeader.innerHTML = header

                usersTableHeaderRow.append(usersTableHeader)
            })
            usersTableHead.append(usersTableHeaderRow)
            usersTable.append(usersTableHead)

            usersTableBody = document.createElement("tbody")

            jsonResponse.forEach(function (usersItem) {
                // Update users display table
                usersTableBodyRow = document.createElement("tr")
                usersProperties.forEach(function (usersProperty) {
                    usersTableBodyCell = document.createElement("td")

                    if (usersProperty === "liveStatsLink") {
                        if (usersProperty in usersItem['previousUser']) {
                            if (usersProperty in usersItem['newUser']) {

                                if (usersItem['previousUser']['liveStatsLink'] != usersItem['newUser']['liveStatsLink']) {
                                    oldSpan = document.createElement('span')
                                    oldSpan.setAttribute("class", "old-value")
                                    oldSpan.innerHTML = usersItem['previousUser']['liveStatsLink']

                                    spacerSpan = document.createElement('span')
                                    spacerSpan.innerHTML = " -> "

                                    newSpan = document.createElement('span')
                                    newSpan.setAttribute("class", "new-value")
                                    newSpan.innerHTML = usersItem['newUser']['liveStatsLink']

                                    usersTableBodyCell.append(oldSpan)
                                    usersTableBodyCell.append(spacerSpan)
                                    usersTableBodyCell.append(newSpan)
                                }
                            } else {
                                oldSpan = document.createElement('span')
                                oldSpan.setAttribute("class", "old-value")
                                oldSpan.innerHTML = usersItem['previousUser']['liveStatsLink']
                                usersTableBodyCell.append(oldSpan)
                            }
                        } else {
                            if (usersProperty in usersItem['newUser']) {
                                newSpan = document.createElement('span')
                                newSpan.setAttribute("class", "new-value")
                                newSpan.innerHTML = usersItem['newUser']['liveStatsLink']

                                usersTableBodyCell.append(newSpan)
                            }
                        }
                    } else if (usersProperty === "foldingUserName") {
                        previousFoldingName = usersItem['previousUser']['foldingUserName']
                        newFoldingName = usersItem['newUser']['foldingUserName']

                        if (previousFoldingName != newFoldingName) {
                            oldSpan = document.createElement('span')
                            oldSpan.setAttribute("class", "old-value")
                            oldSpan.innerHTML = previousFoldingName

                            spacerSpan = document.createElement('span')
                            spacerSpan.innerHTML = " -> "

                            newSpan = document.createElement('span')
                            newSpan.setAttribute("class", "new-value")
                            newSpan.innerHTML = newFoldingName

                            usersTableBodyCell.append(oldSpan)
                            usersTableBodyCell.append(spacerSpan)
                            usersTableBodyCell.append(newSpan)
                        } else {
                            usersTableBodyCell.innerHTML = usersItem['newUser']['foldingUserName']
                        }
                    } else if (usersProperty === "passkey") {
                        previousPasskey = usersItem['previousUser']['passkey']
                        newPasskey = usersItem['newUser']['passkey']

                        if (previousPasskey != newPasskey) {
                            oldSpan = document.createElement('span')
                            oldSpan.setAttribute("class", "old-value")
                            oldSpan.innerHTML = previousPasskey

                            spacerSpan = document.createElement('span')
                            spacerSpan.innerHTML = " -> "

                            newSpan = document.createElement('span')
                            newSpan.setAttribute("class", "new-value")
                            newSpan.innerHTML = newPasskey

                            usersTableBodyCell.append(oldSpan)
                            usersTableBodyCell.append(spacerSpan)
                            usersTableBodyCell.append(newSpan)
                        }
                    } else if (usersProperty === "state") {
                        usersTableBodyCell.innerHTML = getUserChangeStateFrontend(usersItem['state'])
                    } else if (usersProperty === "hardware") {
                        previousHardwareId = usersItem['previousUser']['hardware']['id']
                        newHardwareId = usersItem['newUser']['hardware']['id']

                        if (previousHardwareId != newHardwareId) {
                            oldSpan = document.createElement('span')
                            oldSpan.setAttribute("class", "old-value")
                            oldSpan.innerHTML = usersItem['previousUser']['hardware']['displayName']

                            spacerSpan = document.createElement('span')
                            spacerSpan.innerHTML = " -> "

                            newSpan = document.createElement('span')
                            newSpan.setAttribute("class", "new-value")
                            newSpan.innerHTML = usersItem['newUser']['hardware']['displayName']

                            usersTableBodyCell.append(oldSpan)
                            usersTableBodyCell.append(spacerSpan)
                            usersTableBodyCell.append(newSpan)
                        }
                    } else if (usersProperty === "createdUtcTimestamp") {
                        var timestamp = usersItem['createdUtcTimestamp']
                        var tsDate = timestamp['date']
                        var tsTime = timestamp['time']

                        usersTableBodyCell.innerHTML = "{0}/{1}/{2} {3}:{4}".format(tsDate['year'], tsDate['month'], tsDate['day'], tsTime['hour'], tsTime['minute'])
                    } else if (usersProperty === "updatedUtcTimestamp") {
                        var timestamp = usersItem['updatedUtcTimestamp']
                        var tsDate = timestamp['date']
                        var tsTime = timestamp['time']

                        usersTableBodyCell.innerHTML = "{0}/{1}/{2} {3}:{4}".format(tsDate['year'], tsDate['month'], tsDate['day'], tsTime['hour'], tsTime['minute'])
                    } else if (usersProperty === "approve") {
                        approveNowButton = document.createElement("button")
                        approveNowButton.setAttribute("class", "btn btn-success")
                        approveNowButton.setAttribute("onclick", "approveUserChangeNow(" + usersItem['id'] + "); return false")
                        approveNowButton.innerHTML = "Approve Now"
                        usersTableBodyCell.append(approveNowButton)

                        approveNextMonthButton = document.createElement("button")
                        approveNextMonthButton.setAttribute("class", "btn btn-success")
                        approveNextMonthButton.setAttribute("onclick", "approveUserChangeNextMonth(" + usersItem['id'] + "); return false")
                        approveNextMonthButton.innerHTML = "Approve Next Month"
                        usersTableBodyCell.append(approveNextMonthButton)

                        rejectButton = document.createElement("button")
                        rejectButton.setAttribute("class", "btn btn-danger")
                        rejectButton.setAttribute("onclick", "rejectUserChange(" + usersItem['id'] + "); return false")
                        rejectButton.innerHTML = "Reject"
                        usersTableBodyCell.append(rejectButton)
                    }

                    usersTableBodyRow.append(usersTableBodyCell)
                })
                usersTableBody.append(usersTableBodyRow)
            })
            usersTable.append(usersTableBody)

            usersTableDiv.append(usersTable)
            usersDiv.append(usersTableDiv)
        })
}

function loadPendingUserChangesAdmin() {
    loadUserChangesAdmin("REQUESTED_NOW,REQUESTED_NEXT_MONTH", "pending_user_changes", "Pending Changes")
}

function loadCompletedUserChangesAdmin() {
    loadUserChangesAdmin("REJECTED,COMPLETED", "completed_user_changes", "Completed Changes")
}

document.addEventListener("DOMContentLoaded", function () {
    if (sessionContains("Authorization")) {
        hide("login_form")
        show("admin_functions")

        loadHardware()
        loadTeams()
        loadUsersAdmin()
        loadPendingUserChangesAdmin()
        loadCompletedUserChangesAdmin()
    }

    updateTimer()
    hide("loader")
})

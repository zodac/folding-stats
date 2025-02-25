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
function populateHardwareUpdate() {
    var inputElement = document.getElementById("hardware_update_selector_input")
    var hardwareName = inputElement.value

    if (hardwareName == "") {
        hardwareFields = document.querySelectorAll(".hardware_update")
        for (let i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField)
        }

        return
    }

    show("loader")

    var url = encodeURI(REST_ENDPOINT_URL + "/hardware/fields?hardwareName=" + hardwareName)
    fetch(url)
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("hardware_update_id").value = jsonResponse['id']
            document.getElementById("hardware_update_name").value = jsonResponse['hardwareName']
            document.getElementById("hardware_update_display_name").value = jsonResponse['displayName']
            document.getElementById("hardware_update_hardware_make_input").value = jsonResponse['hardwareMake']
            document.getElementById("hardware_update_hardware_type_input").value = jsonResponse['hardwareType']
            document.getElementById("hardware_update_multiplier").value = jsonResponse['multiplier']
            document.getElementById("hardware_update_average_ppd").value = jsonResponse['averagePpd'].toLocaleString()
        })

    hardwareFields = document.querySelectorAll(".hardware_update")
    for (let i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField)
    }
    hide("loader")
}

function populateHardwareDelete() {
    var inputElement = document.getElementById("hardware_delete_selector_input")
    var hardwareName = inputElement.value

    if (hardwareName == "") {
        hardwareFields = document.querySelectorAll(".hardware_delete")
        for (let i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField)
        }

        return
    }

    show("loader")

    var url = encodeURI(REST_ENDPOINT_URL + "/hardware/fields?hardwareName=" + hardwareName)
    fetch(url)
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("hardware_delete_id").value = jsonResponse['id']
            document.getElementById("hardware_delete_name").value = jsonResponse['hardwareName']
            document.getElementById("hardware_delete_display_name").value = jsonResponse['displayName']
            document.getElementById("hardware_delete_hardware_make_input").value = jsonResponse['hardwareMake']
            document.getElementById("hardware_delete_hardware_type_input").value = jsonResponse['hardwareType']
            document.getElementById("hardware_delete_multiplier").value = jsonResponse['multiplier']
            document.getElementById("hardware_delete_average_ppd").value = jsonResponse['averagePpd'].toLocaleString()
        })

    hardwareFields = document.querySelectorAll(".hardware_delete")
    for (let i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField)
    }
    hide("loader")
}

function populateTeamUpdate() {
    var inputElement = document.getElementById("team_update_selector_input")
    var teamName = inputElement.value

    if (teamName == "") {
        teamFields = document.querySelectorAll(".team_update")
        for (let i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField)
        }

        return
    }

    show("loader")

    var url = encodeURI(REST_ENDPOINT_URL + "/teams/fields?teamName=" + teamName)
    fetch(url)
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("team_update_id").value = jsonResponse['id']
            document.getElementById("team_update_name").value = jsonResponse['teamName']
            document.getElementById("team_update_forum_link").value = jsonResponse['forumLink']

            if ("teamDescription" in jsonResponse) {
                document.getElementById("team_update_description").value = jsonResponse['teamDescription']
            } else {
                document.getElementById("team_update_description").value = ""
            }
        })

    teamFields = document.querySelectorAll(".team_update")
    for (let i = 0, teamField; teamField = teamFields[i]; i++) {
        showElement(teamField)
    }
    hide("loader")
}

function populateTeamDelete() {
    var inputElement = document.getElementById("team_delete_selector_input")
    var teamName = inputElement.value

    if (teamName == "") {
        teamFields = document.querySelectorAll(".team_delete")
        for (let i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField)
        }

        return
    }

    show("loader")

    var url = encodeURI(REST_ENDPOINT_URL + "/teams/fields?teamName=" + teamName)
    fetch(url)
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("team_delete_id").value = jsonResponse['id']
            document.getElementById("team_delete_name").value = jsonResponse['teamName']
            document.getElementById("team_delete_forum_link").value = jsonResponse['forumLink']

            if ("teamDescription" in jsonResponse) {
                document.getElementById("team_delete_description").value = jsonResponse['teamDescription']
            } else {
                document.getElementById("team_delete_description").value = ""
            }
        })

    teamFields = document.querySelectorAll(".team_delete")
    for (let i = 0, teamField; teamField = teamFields[i]; i++) {
        showElement(teamField)
    }
    hide("loader")
}

function populateUserUpdate() {
    var inputElement = document.getElementById("user_update_selector_input")
    var input = inputElement.value

    if (input == "") {
        userFields = document.querySelectorAll(".user_update")
        for (let i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField)
        }

        return
    }

    show("loader")

    var userId = input.split(":")[0]
    var url = encodeURI(REST_ENDPOINT_URL + "/users/" + userId + "/passkey")
    fetch(url, {
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        },
    })
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("user_update_id").value = userId
            document.getElementById("user_update_folding_name").value = jsonResponse['foldingUserName']
            document.getElementById("user_update_display_name").value = jsonResponse['displayName']
            document.getElementById("user_update_passkey").value = jsonResponse['passkey']
            document.getElementById("user_update_category_input").value = jsonResponse['category']
            document.getElementById("user_update_is_captain").checked = jsonResponse["role"] === "CAPTAIN"

            if ("profileLink" in jsonResponse) {
                document.getElementById("user_update_profile_link").value = jsonResponse['profileLink']
            } else {
                document.getElementById("user_update_profile_link").value = ""
            }

            if ("liveStatsLink" in jsonResponse) {
                document.getElementById("user_update_live_stats_link").value = jsonResponse['liveStatsLink']
            } else {
                document.getElementById("user_update_live_stats_link").value = ""
            }

            var hardwareId = jsonResponse['hardware']['id']
            var teamId = jsonResponse['team']['id']

            var hardwareUrl = encodeURI(REST_ENDPOINT_URL + "/hardware/" + hardwareId)
            fetch(hardwareUrl)
                .then(response => {
                    return response.json()
                })
                .then(function (jsonResponse) {
                    document.getElementById("user_update_hardware_selector_input").value = jsonResponse['hardwareName']

                    var teamUrl = encodeURI(REST_ENDPOINT_URL + "/teams/" + teamId)
                    fetch(teamUrl)
                        .then(response => {
                            return response.json()
                        })
                        .then(function (jsonResponse) {
                            document.getElementById("user_update_team_selector_input").value = jsonResponse['teamName']

                            userFields = document.querySelectorAll(".user_update")
                            for (let i = 0, userField; userField = userFields[i]; i++) {
                                showElement(userField)
                            }
                            hide("loader")
                        })
                })
        })
}

function populateUserDelete() {
    var inputElement = document.getElementById("user_delete_selector_input")
    var input = inputElement.value

    if (input == "") {
        userFields = document.querySelectorAll(".user_delete")
        for (let i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField)
        }

        return
    }

    show("loader")

    var userId = input.split(":")[0]
    var url = encodeURI(REST_ENDPOINT_URL + "/users/" + userId + "/passkey")
    fetch(url, {
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        },
    })
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("user_delete_id").value = userId
            document.getElementById("user_delete_folding_name").value = jsonResponse['foldingUserName']
            document.getElementById("user_delete_display_name").value = jsonResponse['displayName']
            document.getElementById("user_delete_passkey").value = jsonResponse['passkey']
            document.getElementById("user_delete_category_input").value = jsonResponse['category']
            document.getElementById("user_delete_is_captain").checked = jsonResponse["role"] === "CAPTAIN"

            if ("profileLink" in jsonResponse) {
                document.getElementById("user_delete_profile_link").value = jsonResponse['profileLink']
            } else {
                document.getElementById("user_delete_profile_link").value = ""
            }

            if ("liveStatsLink" in jsonResponse) {
                document.getElementById("user_delete_live_stats_link").value = jsonResponse['liveStatsLink']
            } else {
                document.getElementById("user_delete_live_stats_link").value = ""
            }

            var hardwareId = jsonResponse['hardware']['id']
            var teamId = jsonResponse['team']['id']

            var hardwareUrl = encodeURI(REST_ENDPOINT_URL + "/hardware/" + hardwareId)
            fetch(hardwareUrl)
                .then(response => {
                    return response.json()
                })
                .then(function (jsonResponse) {
                    document.getElementById("user_delete_hardware_selector_input").value = jsonResponse['hardwareName']

                    var teamUrl = encodeURI(REST_ENDPOINT_URL + "/teams/" + teamId)
                    fetch(teamUrl)
                        .then(response => {
                            return response.json()
                        })
                        .then(function (jsonResponse) {
                            document.getElementById("user_delete_team_selector_input").value = jsonResponse['teamName']

                            userFields = document.querySelectorAll(".user_delete")
                            for (let i = 0, userField; userField = userFields[i]; i++) {
                                showElement(userField)
                            }
                            hide("loader")
                        })
                })
        })
}

function populateUserOffset() {
    var inputElement = document.getElementById("user_offset_selector_input")
    var input = inputElement.value

    if (input == "") {
        userFields = document.querySelectorAll(".user_offset")
        for (let i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField)
        }

        return
    }

    show("loader")

    var userId = input.split(":")[0]
    var url = encodeURI(REST_ENDPOINT_URL + "/users/" + userId)
    fetch(url)
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("user_offset_id").value = userId
            document.getElementById("user_offset_folding_name").value = jsonResponse['foldingUserName']
            document.getElementById("user_offset_display_name").value = jsonResponse['displayName']
            document.getElementById("user_offset_category_input").value = jsonResponse['category']

            var hardwareId = jsonResponse['hardware']['id']
            var teamId = jsonResponse['team']['id']

            var statsUrl = encodeURI(REST_ENDPOINT_URL + "/stats/users/" + userId)
            fetch(statsUrl)
                .then(response => {
                    return response.json()
                })
                .then(function (jsonResponse) {
                    document.getElementById("user_offset_points").value = jsonResponse['multipliedPoints'].toLocaleString()
                    document.getElementById("user_offset_units").value = jsonResponse['units'].toLocaleString()

                    var hardwareUrl = encodeURI(REST_ENDPOINT_URL + "/hardware/" + hardwareId)
                    fetch(hardwareUrl)
                        .then(response => {
                            return response.json()
                        })
                        .then(function (jsonResponse) {
                            document.getElementById("user_offset_hardware_selector_input").value = jsonResponse['hardwareName']

                            var teamUrl = encodeURI(REST_ENDPOINT_URL + "/teams/" + teamId)
                            fetch(teamUrl)
                                .then(response => {
                                    return response.json()
                                })
                                .then(function (jsonResponse) {
                                    document.getElementById("user_offset_team_selector_input").value = jsonResponse['teamName']

                                    userFields = document.querySelectorAll(".user_offset")
                                    for (let i = 0, userField; userField = userFields[i]; i++) {
                                        showElement(userField)
                                    }
                                    hide("loader")
                                })
                        })
                })
        })
}

function populateUserChangeCreate() {
    var inputElement = document.getElementById("user_change_user_selector_input")
    var input = inputElement.value

    if (input == "") {
        userChangeFields = document.querySelectorAll(".user_change_create")
        for (let i = 0, userChangeField; userChangeField = userChangeFields[i]; i++) {
            hideElement(userChangeField)
        }

        return
    }

    show("loader")

    var userId = input.split(":")[0]
    var url = encodeURI(REST_ENDPOINT_URL + "/users/" + userId)
    fetch(url)
        .then(response => {
            return response.json()
        })
        .then(function (jsonResponse) {
            document.getElementById("user_change_create_id").value = userId
            document.getElementById("user_change_create_existing_passkey").value = jsonResponse['passkey']
            document.getElementById("user_change_create_folding_name").value = jsonResponse['foldingUserName']
            document.getElementById("user_change_create_passkey").value = jsonResponse['passkey']

            if ("liveStatsLink" in jsonResponse) {
                document.getElementById("user_change_create_live_stats_link").value = jsonResponse['liveStatsLink']
            } else {
                document.getElementById("user_change_create_live_stats_link").value = ""
            }

            var hardwareId = jsonResponse['hardware']['id']

            var hardwareUrl = encodeURI(REST_ENDPOINT_URL + "/hardware/" + hardwareId)
            fetch(hardwareUrl)
                .then(response => {
                    return response.json()
                })
                .then(function (jsonResponse) {
                    document.getElementById("user_change_create_hardware_selector_input").value = jsonResponse['hardwareName']

                    userChangeFields = document.querySelectorAll(".user_change_create")
                    for (let i = 0, userChangeField; userChangeField = userChangeFields[i]; i++) {
                        showElement(userChangeField)
                    }
                    hide("loader")
                })
        })
}

function populateHardwareUpdate(){
    var inputElement = document.getElementById("hardware_update_selector_input");
    var hardwareName = inputElement.value

    if (hardwareName == "") {
        hardwareFields = document.querySelectorAll(".hardware_update");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+"/hardware/fields?hardwareName=" + hardwareName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("hardware_update_id").value = jsonResponse['id'];
        document.getElementById("hardware_update_name").value = jsonResponse['hardwareName'];
        document.getElementById("hardware_update_display_name").value = jsonResponse['displayName'];
        document.getElementById("hardware_update_hardware_make_input").value = jsonResponse['hardwareMake'];
        document.getElementById("hardware_update_hardware_type_input").value = jsonResponse['hardwareType'];
        document.getElementById("hardware_update_multiplier").value = jsonResponse['multiplier'];
        document.getElementById("hardware_update_average_ppd").value = jsonResponse['averagePpd'];
    })

    hardwareFields = document.querySelectorAll(".hardware_update");
    for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField);
    }
    hide("loader");
}

function populateHardwareDelete(){
    var inputElement = document.getElementById("hardware_delete_selector_input");
    var hardwareName = inputElement.value

    if (hardwareName == "") {
        hardwareFields = document.querySelectorAll(".hardware_delete");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+"/hardware/fields?hardwareName=" + hardwareName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("hardware_delete_id").value = jsonResponse['id'];
        document.getElementById("hardware_delete_name").value = jsonResponse['hardwareName'];
        document.getElementById("hardware_delete_display_name").value = jsonResponse['displayName'];
        document.getElementById("hardware_delete_hardware_make_input").value = jsonResponse['hardwareMake'];
        document.getElementById("hardware_delete_hardware_type_input").value = jsonResponse['hardwareType'];
        document.getElementById("hardware_delete_multiplier").value = jsonResponse['multiplier'];
        document.getElementById("hardware_delete_average_ppd").value = jsonResponse['averagePpd'];
    })

    hardwareFields = document.querySelectorAll(".hardware_delete");
    for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField);
    }
    hide("loader");
}

function populateTeamUpdate(){
    var inputElement = document.getElementById("team_update_selector_input");
    var teamName = inputElement.value

    if (teamName == "") {
        teamFields = document.querySelectorAll(".team_update");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+"/teams/fields?teamName=" + teamName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("team_update_id").value = jsonResponse['id'];
        document.getElementById("team_update_name").value = jsonResponse['teamName'];
        document.getElementById("team_update_forum_link").value = jsonResponse['forumLink'];

        if ("teamDescription" in jsonResponse) {
            document.getElementById("team_update_description").value = jsonResponse['teamDescription'];
        } else {
            document.getElementById("team_update_description").value = "";
        }
    })

    teamFields = document.querySelectorAll(".team_update");
    for (var i = 0, teamField; teamField = teamFields[i]; i++) {
        showElement(teamField);
    }
    hide("loader");
}

function populateTeamDelete(){
    var inputElement = document.getElementById("team_delete_selector_input");
    var teamName = inputElement.value

    if (teamName == "") {
        teamFields = document.querySelectorAll(".team_delete");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+"/teams/fields?teamName=" + teamName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("team_delete_id").value = jsonResponse['id'];
        document.getElementById("team_delete_name").value = jsonResponse['teamName'];
        document.getElementById("team_delete_forum_link").value = jsonResponse['forumLink'];

        if ("teamDescription" in jsonResponse) {
            document.getElementById("team_delete_description").value = jsonResponse['teamDescription'];
        } else {
            document.getElementById("team_delete_description").value = "";
        }
    })

    teamFields = document.querySelectorAll(".team_delete");
    for (var i = 0, teamField; teamField = teamFields[i]; i++) {
        showElement(teamField);
    }
    hide("loader");
}

function populateUserUpdate(){
    var inputElement = document.getElementById("user_update_selector_input");
    var input = inputElement.value

    if (input == "") {
        userFields = document.querySelectorAll(".user_update");
        for (var i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField);
        }

        return;
    }

    show("loader");

    var userId = input.split(":")[0];
    fetch(ROOT_URL+"/users/" + userId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_update_id").value = userId;
        document.getElementById("user_update_folding_name").value = jsonResponse['foldingUserName'];
        document.getElementById("user_update_display_name").value = jsonResponse['displayName'];
        document.getElementById("user_update_passkey").value = jsonResponse['passkey'];
        document.getElementById("user_update_category_input").value = jsonResponse['category'];
        document.getElementById("user_update_is_captain").checked = jsonResponse["userIsCaptain"];

        if ("profileLink" in jsonResponse) {
            document.getElementById("user_update_profile_link").value = jsonResponse['profileLink'];
        } else {
            document.getElementById("user_update_profile_link").value = "";
        }

        if ("liveStatsLink" in jsonResponse) {
            document.getElementById("user_update_live_stats_link").value = jsonResponse['liveStatsLink'];
        } else {
            document.getElementById("user_update_live_stats_link").value = "";
        }

        var hardwareId = jsonResponse['hardware']['id'];
        var teamId = jsonResponse['team']['id'];

        fetch(ROOT_URL+"/hardware/" + hardwareId)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            document.getElementById("user_update_hardware_selector_input").value = jsonResponse['hardwareName'];

            fetch(ROOT_URL+"/teams/" + teamId)
            .then(response => {
                return response.json();
            })
            .then(function(jsonResponse) {
                document.getElementById("user_update_team_selector_input").value = jsonResponse['teamName'];

                userFields = document.querySelectorAll(".user_update");
                for (var i = 0, userField; userField = userFields[i]; i++) {
                    showElement(userField);
                }
                hide("loader");
            })
        });
    });
}

function populateUserDelete(){
    var inputElement = document.getElementById("user_delete_selector_input");
    var input = inputElement.value

    if (input == "") {
        userFields = document.querySelectorAll(".user_delete");
        for (var i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField);
        }

        return;
    }

    show("loader");

    var userId = input.split(":")[0];
    fetch(ROOT_URL+"/users/" + userId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_delete_id").value = userId;
        document.getElementById("user_delete_folding_name").value = jsonResponse['foldingUserName'];
        document.getElementById("user_delete_display_name").value = jsonResponse['displayName'];
        document.getElementById("user_delete_passkey").value = jsonResponse['passkey'];
        document.getElementById("user_delete_category_input").value = jsonResponse['category'];
        document.getElementById("user_delete_is_captain").checked = jsonResponse["userIsCaptain"];

        if ("profileLink" in jsonResponse) {
            document.getElementById("user_delete_profile_link").value = jsonResponse['profileLink'];
        } else {
            document.getElementById("user_delete_profile_link").value = "";
        }

        if ("liveStatsLink" in jsonResponse) {
            document.getElementById("user_delete_live_stats_link").value = jsonResponse['liveStatsLink'];
        } else {
            document.getElementById("user_delete_live_stats_link").value = "";
        }

        var hardwareId = jsonResponse['hardware']['id'];
        var teamId = jsonResponse['team']['id'];

        fetch(ROOT_URL+"/hardware/" + hardwareId)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            document.getElementById("user_delete_hardware_selector_input").value = jsonResponse['hardwareName'];

            fetch(ROOT_URL+"/teams/" + teamId)
            .then(response => {
                return response.json();
            })
            .then(function(jsonResponse) {
                document.getElementById("user_delete_team_selector_input").value = jsonResponse['teamName'];

                userFields = document.querySelectorAll(".user_delete");
                for (var i = 0, userField; userField = userFields[i]; i++) {
                    showElement(userField);
                }
                hide("loader");
            })
        });
    });
}

function populateUserOffset(){
    var inputElement = document.getElementById("user_offset_selector_input");
    var input = inputElement.value

    if (input == "") {
        userFields = document.querySelectorAll(".user_offset");
        for (var i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField);
        }

        return;
    }

    show("loader");

    var userId = input.split(":")[0];
    fetch(ROOT_URL+"/users/" + userId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_offset_id").value = userId;
        document.getElementById("user_offset_folding_name").value = jsonResponse['foldingUserName'];
        document.getElementById("user_offset_display_name").value = jsonResponse['displayName'];
        document.getElementById("user_offset_category_input").value = jsonResponse['category'];

        var hardwareId = jsonResponse['hardware']['id'];
        var teamId = jsonResponse['team']['id'];

        fetch(ROOT_URL+"/stats/users/" + userId)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            document.getElementById("user_offset_points").value = jsonResponse['multipliedPoints'].toLocaleString();
            document.getElementById("user_offset_units").value = jsonResponse['units'].toLocaleString();

            fetch(ROOT_URL+"/hardware/" + hardwareId)
            .then(response => {
                return response.json();
            })
            .then(function(jsonResponse) {
                document.getElementById("user_offset_hardware_selector_input").value = jsonResponse['hardwareName'];

                fetch(ROOT_URL+"/teams/" + teamId)
                .then(response => {
                    return response.json();
                })
                .then(function(jsonResponse) {
                    document.getElementById("user_offset_team_selector_input").value = jsonResponse['teamName'];

                    userFields = document.querySelectorAll(".user_offset");
                    for (var i = 0, userField; userField = userFields[i]; i++) {
                        showElement(userField);
                    }
                    hide("loader");
                })
            });
        });
    });
}

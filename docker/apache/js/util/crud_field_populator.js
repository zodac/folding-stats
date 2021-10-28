function populateHardwareUpdate(){
    var inputElement = document.getElementById("hardware_update_selector_input");
    var hardwareName = inputElement.value

    if (hardwareName == '') {
        hardwareFields = document.querySelectorAll(".hardware_update");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+'/hardware/fields?hardwareName=' + hardwareName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("hardware_update_id").value = jsonResponse["id"];
        document.getElementById("hardware_update_name").value = jsonResponse["hardwareName"];
        document.getElementById("hardware_update_display_name").value = jsonResponse["displayName"];
        document.getElementById("hardware_update_hardware_make_input").value = jsonResponse["hardwareMake"];
        document.getElementById("hardware_update_hardware_type_input").value = jsonResponse["hardwareType"];
        document.getElementById("hardware_update_multiplier").value = jsonResponse["multiplier"];
        document.getElementById("hardware_update_average_ppd").value = jsonResponse["averagePpd"];
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

    if (hardwareName == '') {
        hardwareFields = document.querySelectorAll(".hardware_delete");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+'/hardware/fields?hardwareName=' + hardwareName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("hardware_delete_id").value = jsonResponse["id"];
        document.getElementById("hardware_delete_name").value = jsonResponse["hardwareName"];
        document.getElementById("hardware_delete_display_name").value = jsonResponse["displayName"];
        document.getElementById("hardware_delete_hardware_make_input").value = jsonResponse["hardwareMake"];
        document.getElementById("hardware_delete_hardware_type_input").value = jsonResponse["hardwareType"];
        document.getElementById("hardware_delete_multiplier").value = jsonResponse["multiplier"];
        document.getElementById("hardware_delete_average_ppd").value = jsonResponse["averagePpd"];
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

    if (teamName == '') {
        teamFields = document.querySelectorAll(".team_update");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+'/teams/fields?teamName=' + teamName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("team_update_id").value = jsonResponse["id"];
        document.getElementById("team_update_name").value = jsonResponse["teamName"];
        document.getElementById("team_update_forum_link").value = jsonResponse["forumLink"];

        teamDescription = jsonResponse["teamDescription"];
        if (teamDescription !== undefined) {
            document.getElementById("team_update_description").value = teamDescription;
        } else {
            document.getElementById("team_update_description").value = '';
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

    if (teamName == '') {
        teamFields = document.querySelectorAll(".team_delete");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        return;
    }

    show("loader");

    fetch(ROOT_URL+'/teams/fields?teamName=' + teamName)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("team_delete_id").value = jsonResponse["id"];
        document.getElementById("team_delete_name").value = jsonResponse["teamName"];
        document.getElementById("team_delete_forum_link").value = jsonResponse["forumLink"];

        teamDescription = jsonResponse["teamDescription"];
        if (teamDescription !== undefined) {
            document.getElementById("team_delete_description").value = teamDescription;
        } else {
            document.getElementById("team_delete_description").value = '';
        }
    })

    teamFields = document.querySelectorAll(".team_delete");
    for (var i = 0, teamField; teamField = teamFields[i]; i++) {
        showElement(teamField);
    }
    hide("loader");
}

function populateUserUpdate(){
    show("loader");
    element = document.getElementById("user_update_selector");
    selectedElement = element.options[element.selectedIndex];

    document.getElementById("user_update_id").value = selectedElement.getAttribute("user_id");
    document.getElementById("user_update_folding_name").value = selectedElement.getAttribute("user_folding_name");
    document.getElementById("user_update_display_name").value = selectedElement.getAttribute("user_display_name");
    document.getElementById("user_update_passkey").value = selectedElement.getAttribute("user_passkey");
    document.getElementById("user_update_category_input").value = selectedElement.getAttribute("user_category");

    if (selectedElement.getAttribute("user_is_captain") === "true") {
        document.getElementById("user_update_is_captain").checked = true;
    } else {
        document.getElementById("user_update_is_captain").checked = false;
    }

    profileLink = selectedElement.getAttribute("user_profile_link");
    if (profileLink !== 'undefined') {
        document.getElementById("user_update_profile_link").value = profileLink;
    } else {
        document.getElementById("user_update_profile_link").value = '';
    }

    liveStatsLink = selectedElement.getAttribute("user_live_stats_link");
    if (liveStatsLink !== 'undefined') {
        document.getElementById("user_update_live_stats_link").value = liveStatsLink;
    } else {
        document.getElementById("user_update_live_stats_link").value = '';
    }

    var hardwareId = selectedElement.getAttribute("user_hardware_id");
    fetch(ROOT_URL+'/hardware/' + hardwareId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_update_hardware_selector_input").value = jsonResponse["hardwareName"];

        var teamId = selectedElement.getAttribute("user_team_id");
        fetch(ROOT_URL+'/teams/' + teamId)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            document.getElementById("user_update_team_selector_input").value = jsonResponse["teamName"];

            userFields = document.querySelectorAll(".user_update");
            for (var i = 0, userField; userField = userFields[i]; i++) {
                showElement(userField);
            }
            hide("loader");
        })
    });
}

function populateUserDelete(){
    show("loader");
    element = document.getElementById("user_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    document.getElementById("user_delete_id").value = selectedElement.getAttribute("user_id");
    document.getElementById("user_delete_folding_name").value = selectedElement.getAttribute("user_folding_name");
    document.getElementById("user_delete_display_name").value = selectedElement.getAttribute("user_display_name");
    document.getElementById("user_delete_passkey").value = selectedElement.getAttribute("user_passkey");
    document.getElementById("user_delete_category_input").value = selectedElement.getAttribute("user_category");

    if (selectedElement.getAttribute("user_is_captain") === "true") {
        document.getElementById("user_delete_is_captain").checked = true;
    } else {
        document.getElementById("user_delete_is_captain").checked = false;
    }

    profileLink = selectedElement.getAttribute("user_profile_link");
    if (profileLink !== 'undefined') {
        document.getElementById("user_delete_profile_link").value = profileLink;
    } else {
        document.getElementById("user_delete_profile_link").value = '';
    }

    liveStatsLink = selectedElement.getAttribute("user_live_stats_link");
    if (liveStatsLink !== 'undefined'){
        document.getElementById("user_delete_live_stats_link").value = liveStatsLink;
    } else {
        document.getElementById("user_delete_live_stats_link").value = '';
    }

    var hardwareId = selectedElement.getAttribute("user_hardware_id");
    fetch(ROOT_URL+'/hardware/' + hardwareId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_delete_hardware_selector_input").value = jsonResponse["hardwareName"];

        var teamId = selectedElement.getAttribute("user_team_id");
        fetch(ROOT_URL+'/teams/' + teamId)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            document.getElementById("user_delete_team_selector_input").value = jsonResponse["teamName"];

            userFields = document.querySelectorAll(".user_delete");
            for (var i = 0, userField; userField = userFields[i]; i++) {
                showElement(userField);
            }
            hide("loader");
        })
    });
}

function populateUserOffset(){
    show("loader");
    element = document.getElementById("user_offset_selector");
    selectedElement = element.options[element.selectedIndex];

    userId = selectedElement.getAttribute("user_id");

    document.getElementById("user_offset_id").value = userId;
    document.getElementById("user_offset_folding_name").value = selectedElement.getAttribute("user_folding_name");
    document.getElementById("user_offset_display_name").value = selectedElement.getAttribute("user_display_name");
    document.getElementById("user_offset_category_input").value = selectedElement.getAttribute("user_category");

    fetch(ROOT_URL+'/stats/users/' + userId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_offset_points").value = jsonResponse['multipliedPoints'];
        document.getElementById("user_offset_units").value = jsonResponse['units'];

        var hardwareId = selectedElement.getAttribute("user_hardware_id");
        fetch(ROOT_URL+'/hardware/' + hardwareId)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            document.getElementById("user_offset_hardware_selector_input").value = jsonResponse["hardwareName"];

            var teamId = selectedElement.getAttribute("user_team_id");
            fetch(ROOT_URL+'/teams/' + teamId)
            .then(response => {
                return response.json();
            })
            .then(function(jsonResponse) {
                document.getElementById("user_offset_team_selector_input").value = jsonResponse["teamName"];

                userFields = document.querySelectorAll(".user_offset");
                for (var i = 0, userField; userField = userFields[i]; i++) {
                    showElement(userField);
                }
                hide("loader");
            })
        });
    });
}
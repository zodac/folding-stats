function createHardware() {
    var hardwareName = document.getElementById("hardware_create_name").value.trim();
    var displayName = document.getElementById("hardware_create_display_name").value.trim();
    var hardwareMake = document.getElementById("hardware_create_hardware_make_input").value.trim();
    var hardwareType = document.getElementById("hardware_create_hardware_type_input").value.trim();
    var multiplier = document.getElementById("hardware_create_multiplier").value.trim();
    var averagePpd = document.getElementById("hardware_create_average_ppd").value.trim().replaceAll(',', "");

    var requestData = JSON.stringify(
        {
            "hardwareName": escape(hardwareName),
            "displayName": escape(displayName),
            "hardwareMake": getHardwareMakeBackend(escape(hardwareMake)),
            "hardwareType": getHardwareTypeBackend(escape(hardwareType)),
            "multiplier": escape(multiplier),
            "averagePpd": escape(averagePpd),
        }
    );

    show("loader");
    fetch(REST_ENDPOINT_URL+"/hardware", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        },
        body: requestData
    })
    .then(response => {
        hide("loader");

        if(response.status != 201){
            failureToast("Hardware create failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("hardware_create_name").value = "";
        document.getElementById("hardware_create_display_name").value = "";
        document.getElementById("hardware_create_hardware_make_input").value = "";
        document.getElementById("hardware_create_hardware_type_input").value = "";
        document.getElementById("hardware_create_multiplier").value = "";
        document.getElementById("hardware_create_average_ppd").value = "";
        successToast("Hardware '" + displayName + "' created");
        loadHardware();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error creating hardware: ", error);
        return false;
    });
}

function updateHardware() {
    var hardwareId = document.getElementById("hardware_update_id").value.trim();
    var hardwareName = document.getElementById("hardware_update_name").value.trim();
    var displayName = document.getElementById("hardware_update_display_name").value.trim();
    var hardwareMake = document.getElementById("hardware_update_hardware_make_input").value.trim();
    var hardwareType = document.getElementById("hardware_update_hardware_type_input").value.trim();
    var multiplier = document.getElementById("hardware_update_multiplier").value.trim();
    var averagePpd = document.getElementById("hardware_update_average_ppd").value.trim().replaceAll(',', "");

    var requestData = JSON.stringify(
        {
            "hardwareName": escape(hardwareName),
            "displayName": escape(displayName),
            "hardwareMake": getHardwareMakeBackend(escape(hardwareMake)),
            "hardwareType": getHardwareTypeBackend(escape(hardwareType)),
            "multiplier": escape(multiplier),
            "averagePpd": escape(averagePpd),
        }
    );

    show("loader");
    fetch(REST_ENDPOINT_URL+"/hardware/" + hardwareId, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        },
        body: requestData
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Hardware update failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("hardware_update_selector_input").value = "";
        document.getElementById("hardware_update_id").value = "";
        document.getElementById("hardware_update_name").value = "";
        document.getElementById("hardware_update_display_name").value = "";
        document.getElementById("hardware_update_hardware_make_input").value = "";
        document.getElementById("hardware_update_hardware_type_input").value = "";
        document.getElementById("hardware_update_multiplier").value = "";
        document.getElementById("hardware_update_average_ppd").value = "";

        hardwareFields = document.querySelectorAll(".hardware_update");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        successToast("Hardware '" + displayName + "' updated");
        loadHardware();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error updating hardware: ", error);
        return false;
    });
}

function deleteHardware() {
    var hardwareId = document.getElementById("hardware_delete_id").value.trim();
    var displayName = document.getElementById("hardware_delete_display_name").value.trim();

    show("loader");
    fetch(REST_ENDPOINT_URL+"/hardware/" + hardwareId, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Hardware delete failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("hardware_delete_selector_input").value = "";
        document.getElementById("hardware_delete_id").value = "";
        document.getElementById("hardware_delete_name").value = "";
        document.getElementById("hardware_delete_display_name").value = "";
        document.getElementById("hardware_delete_hardware_make_input").value = "";
        document.getElementById("hardware_delete_hardware_type_input").value = "";
        document.getElementById("hardware_delete_multiplier").value = "";
        document.getElementById("hardware_delete_average_ppd").value = "";

        hardwareFields = document.querySelectorAll(".hardware_delete");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        successToast("Hardware '" + displayName + "' deleted");
        loadHardware();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error deleting hardware: ", error);
        return false;
    });
}

function createTeam() {
    var teamName = document.getElementById("team_create_name").value.trim();
    var teamDescription = document.getElementById("team_create_description").value.trim();
    var forumLink = document.getElementById("team_create_forum_link").value.trim();

    var requestData = JSON.stringify(
        {
            "teamName": escape(teamName),
            "teamDescription": escape(teamDescription),
            "forumLink": escape(forumLink)
        }
    );

    show("loader");
    fetch(REST_ENDPOINT_URL+"/teams", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        },
        body: requestData
    })
    .then(response => {
        hide("loader");

        if(response.status != 201){
            failureToast("Team create failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("team_create_name").value = "";
        document.getElementById("team_create_description").value = "";
        document.getElementById("team_create_forum_link").value = "";
        successToast("Team '" + teamName + "' created");
        loadTeams();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error creating team: ", error);
        return false;
    });
}

function updateTeam() {
    var teamId = document.getElementById("team_update_id").value.trim();
    var teamName = document.getElementById("team_update_name").value.trim();
    var teamDescription = document.getElementById("team_update_description").value.trim();
    var forumLink = document.getElementById("team_update_forum_link").value.trim();

    var requestData = JSON.stringify(
        {
            "teamName": escape(teamName),
            "teamDescription": escape(teamDescription),
            "forumLink": escape(forumLink)
        }
    );

    show("loader");
    fetch(REST_ENDPOINT_URL+"/teams/" + teamId, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        },
        body: requestData
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Team update failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("team_update_selector_input").value = "";
        document.getElementById("team_update_id").value = "";
        document.getElementById("team_update_name").value = "";
        document.getElementById("team_update_description").value = "";
        document.getElementById("team_update_forum_link").value = "";

        teamFields = document.querySelectorAll(".team_update");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        successToast("Team '" + teamName + "' updated");
        loadTeams();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error updating team: ", error);
        return false;
    });
}

function deleteTeam() {
    var teamId = document.getElementById("team_delete_id").value.trim();
    var teamName = document.getElementById("team_delete_name").value.trim();

    show("loader");
    fetch(REST_ENDPOINT_URL+"/teams/" + teamId, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Team delete failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("team_delete_selector_input").value = "";
        document.getElementById("team_delete_id").value = "";
        document.getElementById("team_delete_name").value = "";
        document.getElementById("team_delete_description").value = "";
        document.getElementById("team_delete_forum_link").value = "";

        teamFields = document.querySelectorAll(".team_delete");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        successToast("Team '" + teamName + "' deleted");
        loadTeams();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error deleting team: ", error);
        return false;
    });
}

function createUser() {
    var foldingUserName = document.getElementById("user_create_folding_name").value.trim();
    var displayName = document.getElementById("user_create_display_name").value.trim();
    var passkey = document.getElementById("user_create_passkey").value.trim();
    var category = document.getElementById("user_create_category_input").value.trim();
    var profileLink = document.getElementById("user_create_profile_link").value.trim();
    var liveStatsLink = document.getElementById("user_create_live_stats_link").value.trim();
    var hardwareName = document.getElementById("user_create_hardware_selector_input").value.trim();
    var teamName = document.getElementById("user_create_team_selector_input").value.trim();
    var isCaptain = document.getElementById("user_create_is_captain").checked;

    var hardwareUrl = encodeURI(REST_ENDPOINT_URL+"/hardware/fields?hardwareName=" + hardwareName)
    fetch(hardwareUrl)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        var hardwareId = jsonResponse['id'];

        var teamUrl = encodeURI(REST_ENDPOINT_URL+"/teams/fields?teamName=" + teamName)
        fetch(teamUrl)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            var teamId = jsonResponse['id'];

            var requestData = JSON.stringify(
                {
                    "foldingUserName": escape(foldingUserName),
                    "displayName": escape(displayName),
                    "passkey": escape(passkey),
                    "category": getCategoryBackend(escape(category)),
                    "profileLink": escape(profileLink),
                    "liveStatsLink": escape(liveStatsLink),
                    "hardwareId": escape(hardwareId),
                    "teamId": escape(teamId),
                    "userIsCaptain": escape(isCaptain)
                }
            );

            show("loader");
            var url = encodeURI(REST_ENDPOINT_URL+"/users")
            fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": sessionGet("Authorization")
                },
                body: requestData
            })
            .then(response => {
                hide("loader");

                if(response.status != 201){
                    failureToast("User create failed with code: " + response.status);
                    response.json()
                    .then(response => {
                        console.error(JSON.stringify(response, null, 2));
                    });
                    return;
                }

                document.getElementById("user_create_folding_name").value = "";
                document.getElementById("user_create_display_name").value = "";
                document.getElementById("user_create_passkey").value = "";
                document.getElementById("user_create_category_input").value = "";
                document.getElementById("user_create_profile_link").value = "";
                document.getElementById("user_create_live_stats_link").value = "";
                document.getElementById("user_create_hardware_selector_input").value = "";
                document.getElementById("user_create_team_selector_input").value = "";
                document.getElementById("user_create_is_captain").checked = false;

                successToast("User '" + displayName + "' created");
                loadUsersAdmin();
            })
            .catch((error) => {
                hide("loader");
                console.error("Unexpected error creating user: ", error);
                return false;
            });
        })
    });
}

function updateUser() {
    var userId = document.getElementById("user_update_id").value
    var foldingUserName = document.getElementById("user_update_folding_name").value.trim();
    var displayName = document.getElementById("user_update_display_name").value.trim();
    var passkey = document.getElementById("user_update_passkey").value.trim();
    var category = document.getElementById("user_update_category_input").value.trim();
    var profileLink = document.getElementById("user_update_profile_link").value.trim();
    var liveStatsLink = document.getElementById("user_update_live_stats_link").value.trim();
    var hardwareName = document.getElementById("user_update_hardware_selector_input").value.trim();
    var teamName = document.getElementById("user_update_team_selector_input").value.trim();
    var isCaptain = document.getElementById("user_update_is_captain").checked;

    var hardwareUrl = encodeURI(REST_ENDPOINT_URL+"/hardware/fields?hardwareName=" + hardwareName)
    fetch(hardwareUrl)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        var hardwareId = jsonResponse['id'];

        var teamUrl = encodeURI(REST_ENDPOINT_URL+"/teams/fields?teamName=" + teamName)
        fetch(teamUrl)
        .then(response => {
            return response.json();
        })
        .then(function(jsonResponse) {
            var teamId = jsonResponse['id'];

            var requestData = JSON.stringify(
                {
                    "foldingUserName": escape(foldingUserName),
                    "displayName": escape(displayName),
                    "passkey": escape(passkey),
                    "category": getCategoryBackend(escape(category)),
                    "profileLink": escape(profileLink),
                    "liveStatsLink": escape(liveStatsLink),
                    "hardwareId": escape(hardwareId),
                    "teamId": escape(teamId),
                    "userIsCaptain": escape(isCaptain)
                }
            );

            show("loader");
            var url = encodeURI(REST_ENDPOINT_URL+"/users/" + userId)
            fetch(url, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": sessionGet("Authorization")
                },
                body: requestData
            })
            .then(response => {
                hide("loader");

                if(response.status != 200){
                    failureToast("User update failed with code: " + response.status)
                    response.json()
                    .then(response => {
                        console.error(JSON.stringify(response, null, 2));
                    });
                    return;
                }

                document.getElementById("user_update_selector_input").value = "";
                document.getElementById("user_update_id").value = "";
                document.getElementById("user_update_folding_name").value = "";
                document.getElementById("user_update_display_name").value = "";
                document.getElementById("user_update_passkey").value = "";
                document.getElementById("user_update_category_input").value = "";
                document.getElementById("user_update_profile_link").value = "";
                document.getElementById("user_update_live_stats_link").value = "";
                document.getElementById("user_update_hardware_selector_input").value = "";
                document.getElementById("user_update_team_selector_input").value = "";
                document.getElementById("user_update_is_captain").checked = false;

                userFields = document.querySelectorAll(".user_update");
                for (var i = 0, userField; userField = userFields[i]; i++) {
                    hideElement(userField);
                }

                successToast("User '" + displayName + "' updated");
                loadUsersAdmin();
            })
            .catch((error) => {
                hide("loader");
                console.error("Unexpected error updating user: ", error);
                return false;
            });
        })
    });
}

function deleteUser() {
    var userId = document.getElementById("user_delete_id").value
    var displayName = document.getElementById("user_delete_display_name").value

    show("loader");
    var url = encodeURI(REST_ENDPOINT_URL+"/users/" + userId)
    fetch(url, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("User delete failed with code: " + response.status)
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        document.getElementById("user_delete_selector_input").value = "";
        document.getElementById("user_delete_id").value = "";
        document.getElementById("user_delete_folding_name").value = "";
        document.getElementById("user_delete_display_name").value = "";
        document.getElementById("user_delete_passkey").value = "";
        document.getElementById("user_delete_category_input").value = "";
        document.getElementById("user_delete_profile_link").value = "";
        document.getElementById("user_delete_live_stats_link").value = "";
        document.getElementById("user_delete_hardware_selector_input").value = "";
        document.getElementById("user_delete_team_selector_input").value = "";
        document.getElementById("user_delete_is_captain").checked = false;

        userFields = document.querySelectorAll(".user_delete");
        for (var i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField);
        }

        successToast("User '" + displayName + "' deleted");
        loadUsersAdmin();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error deleting user: ", error);
        return false;
    });
}

function offsetUser() {
    var userId = document.getElementById("user_offset_id").value
    var displayName = document.getElementById("user_offset_display_name").value

    show("loader");
    var url = encodeURI(REST_ENDPOINT_URL+"/stats/users/" + userId)
    fetch(url)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        var startPoints = jsonResponse['multipliedPoints']
        var startUnits = jsonResponse['units']
        var endPoints = document.getElementById("user_offset_points").value.replaceAll(',', "")
        var endUnits = document.getElementById("user_offset_units").value.replaceAll(',', "")

        var offsetPoints = (endPoints - startPoints);
        var offsetUnits = (endUnits - startUnits);

        var requestData = JSON.stringify(
            {
                "multipliedPointsOffset": escape(offsetPoints),
                "unitsOffset": escape(offsetUnits)
            }
        );

        var statsUrl = encodeURI(REST_ENDPOINT_URL+"/stats/users/" + userId)
        fetch(statsUrl, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Authorization": sessionGet("Authorization")
            },
            body: requestData
        })
        .then(response => {
            hide("loader");

            if(response.status != 200){
                failureToast("User stats offset failed with code: " + response.status)
                response.json()
                .then(response => {
                    console.error(JSON.stringify(response, null, 2));
                });
                return;
            }

            document.getElementById("user_offset_selector_input").value = "";
            document.getElementById("user_offset_id").value = "";
            document.getElementById("user_offset_folding_name").value = "";
            document.getElementById("user_offset_display_name").value = "";
            document.getElementById("user_offset_category_input").value = "";
            document.getElementById("user_offset_hardware_selector_input").value = "";
            document.getElementById("user_offset_team_selector_input").value = "";
            document.getElementById("user_offset_points").value = "";
            document.getElementById("user_offset_units").value = "";

            userFields = document.querySelectorAll(".user_offset");
            for (var i = 0, userField; userField = userFields[i]; i++) {
                hideElement(userField);
            }

            successToast("User '" + displayName + "' stats updated: " + offsetPoints + " points, " + offsetUnits + " units");
            loadUsersAdmin();
        })
        .catch((error) => {
            hide("loader");
            console.error("Unexpected error offsetting user stats: ", error);
            return false;
        });
    });
}

function createUserChange() {
    var userId = document.getElementById("user_change_create_id").value
    var existingPasskey = document.getElementById("user_change_create_existing_passkey").value.trim();
    var foldingUserName = document.getElementById("user_change_create_folding_name").value.trim();
    var passkey = document.getElementById("user_change_create_passkey").value.trim();
    var liveStatsLink = document.getElementById("user_change_create_live_stats_link").value.trim();
    var hardwareName = document.getElementById("user_change_create_hardware_selector_input").value.trim();
    var when = document.getElementById("user_change_create_when_input").value.trim();

    var hardwareUrl = encodeURI(REST_ENDPOINT_URL+"/hardware/fields?hardwareName=" + hardwareName)
    fetch(hardwareUrl)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        var hardwareId = jsonResponse['id'];
        var immediate = when === "Immediately";

        var requestData = JSON.stringify(
            {
                "userId": escape(userId),
                "existingPasskey": escape(existingPasskey),
                "foldingUserName": escape(foldingUserName),
                "passkey": escape(passkey),
                "liveStatsLink": escape(liveStatsLink),
                "hardwareId": escape(hardwareId),
                "immediate": immediate
            }
        );

        show("loader");
        var url = encodeURI(REST_ENDPOINT_URL+"/changes")
        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: requestData
        })
        .then(response => {
            hide("loader");

            if(response.status == 409){
                response.json()
                .then(response => {
                    failureToastPermanent("Request already exists!");
                    console.error(JSON.stringify(response, null, 2));
                });
                return;
            } else if(response.status != 201){
                response.json()
                .then(response => {
                    failureToastPermanent("Failure: " + response["errors"]);
                    console.error(JSON.stringify(response, null, 2));
                });
                return;
            }

            document.getElementById("user_change_user_selector_input").value = "";
            document.getElementById("user_change_create_id").value = "";
            document.getElementById("user_change_create_existing_passkey").value = "";
            document.getElementById("user_change_create_folding_name").value = "";
            document.getElementById("user_change_create_passkey").value = "";
            document.getElementById("user_change_create_live_stats_link").value = "";
            document.getElementById("user_change_create_hardware_selector_input").value = "";
            document.getElementById("user_change_create_when_input").value = "";

            userCreateFields = document.querySelectorAll(".user_change_create");
            for (var i = 0, userCreateField; userCreateField = userCreateFields[i]; i++) {
                hideElement(userCreateField);
            }

            successToast("Request created");
            loadPendingUserChanges();
        })
        .catch((error) => {
            hide("loader");
            console.error("Unexpected error creating request: ", error);
            return false;
        });
    });
}

function approveUserChangeNow(id){
    var url = encodeURI(REST_ENDPOINT_URL+"/changes/" + id + "/approve/immediate")
    fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");
        successToast("Request approved (now)");

        loadPendingUserChangesAdmin();
        loadCompletedUserChangesAdmin();
        loadUsersAdmin();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error approving request (now): ", error);
        return false;
    });
}

function approveUserChangeNextMonth(id){
    var url = encodeURI(REST_ENDPOINT_URL+"/changes/" + id + "/approve/next")
    fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");
        successToast("Request approved (next month)");

        loadPendingUserChangesAdmin();
        loadCompletedUserChangesAdmin();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error approving request (next month): ", error);
        return false;
    });
}

function rejectUserChange(id){
    var url = encodeURI(REST_ENDPOINT_URL+"/changes/" + id + "/reject")
    fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");
        successToast("Request rejected");

        loadPendingUserChangesAdmin();
        loadCompletedUserChangesAdmin();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error rejecting request: ", error);
        return false;
    });
}

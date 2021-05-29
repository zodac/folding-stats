function createHardware() {
    var hardwareName = document.getElementById("hardware_create_name").value.trim();
    var displayName = document.getElementById("hardware_create_display_name").value.trim();
    var operatingSystem = document.getElementById("hardware_create_operating_system").value.trim();
    var multiplier = document.getElementById("hardware_create_multiplier").value.trim();

    var requestData = JSON.stringify(
        {
            "hardwareName": hardwareName,
            "displayName": displayName,
            "operatingSystem": operatingSystem,
            "multiplier": multiplier
        }
    );

    show("loader");
    fetch(ROOT_URL+'/hardware', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("hardware_create_name").value = '';
        document.getElementById("hardware_create_display_name").value = '';
        document.getElementById("hardware_create_operating_system").value = '';
        document.getElementById("hardware_create_multiplier").value = '';
        successToast("Hardware '" + displayName + "' created");
        loadHardware();
    })
    .catch((error) => {
        console.error('Unexpected error creating hardware: ', error);
        return false;
    });
}

function updateHardware() {
    var element = document.getElementById("hardware_update_selector");
    element = document.getElementById("hardware_update_selector");
    selectedElement = element.options[element.selectedIndex];
    var hardwareId = selectedElement.getAttribute("hardware_id");

    var hardwareName = document.getElementById("hardware_update_name").value.trim();
    var displayName = document.getElementById("hardware_update_display_name").value.trim();
    var operatingSystem = document.getElementById("hardware_update_operating_system").value.trim();
    var multiplier = document.getElementById("hardware_update_multiplier").value.trim();

    var requestData = JSON.stringify(
        {
            "hardwareName": hardwareName,
            "displayName": displayName,
            "operatingSystem": operatingSystem,
            "multiplier": multiplier
        }
    );

    show("loader");
    fetch(ROOT_URL+'/hardware/' + hardwareId, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("hardware_update_id").value = '';
        document.getElementById("hardware_update_name").value = '';
        document.getElementById("hardware_update_display_name").value = '';
        document.getElementById("hardware_update_operating_system").value = '';
        document.getElementById("hardware_update_multiplier").value = '';

        hardwareFields = document.querySelectorAll(".hardware_update");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        successToast("Hardware '" + displayName + "' updated");
        loadHardware();
    })
    .catch((error) => {
        console.error('Unexpected error updating hardware: ', error);
        return false;
    });
}

function deleteHardware() {
    var element = document.getElementById("hardware_delete_selector");
    element = document.getElementById("hardware_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    var hardwareId = selectedElement.getAttribute("hardware_id");
    var hardwareDisplayName = selectedElement.getAttribute("hardware_name");

    show("loader");
    fetch(ROOT_URL+'/hardware/' + hardwareId, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("hardware_delete_id").value = '';
        document.getElementById("hardware_delete_name").value = '';
        document.getElementById("hardware_delete_display_name").value = '';
        document.getElementById("hardware_delete_operating_system").value = '';
        document.getElementById("hardware_delete_multiplier").value = '';

        hardwareFields = document.querySelectorAll(".hardware_delete");
        for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
            hideElement(hardwareField);
        }

        successToast("Hardware '" + hardwareDisplayName + "' deleted");
        loadHardware();
    })
    .catch((error) => {
        console.error('Unexpected error deleting hardware: ', error);
        return false;
    });
}

function createTeam() {
    var teamName = document.getElementById("team_create_name").value.trim();
    var teamDescription = document.getElementById("team_create_description").value.trim();
    var forumLink = document.getElementById("team_create_forum_link").value.trim();

    var requestData = JSON.stringify(
        {
            "teamName": teamName,
            "teamDescription": teamDescription,
            "forumLink": forumLink
        }
    );

    show("loader");
    fetch(ROOT_URL+'/teams', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("team_create_name").value = '';
        document.getElementById("team_create_description").value = '';
        document.getElementById("team_create_forum_link").value = '';
        successToast("Team '" + teamName + "' created");
        loadTeams();
    })
    .catch((error) => {
        console.error('Unexpected error creating team: ', error);
        return false;
    });
}

function updateTeam() {
    var element = document.getElementById("team_update_selector");
    element = document.getElementById("team_update_selector");
    selectedElement = element.options[element.selectedIndex];
    var teamId = selectedElement.getAttribute("team_id");

    var teamName = document.getElementById("team_update_name").value.trim();
    var teamDescription = document.getElementById("team_update_description").value.trim();
    var forumLink = document.getElementById("team_update_forum_link").value.trim();

    var requestData = JSON.stringify(
        {
            "teamName": teamName,
            "teamDescription": teamDescription,
            "forumLink": forumLink
        }
    );

    show("loader");
    fetch(ROOT_URL+'/teams/' + teamId, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("team_update_id").value = '';
        document.getElementById("team_update_name").value = '';
        document.getElementById("team_update_description").value = '';
        document.getElementById("team_update_forum_link").value = '';

        teamFields = document.querySelectorAll(".team_update");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        successToast("Team '" + teamName + "' updated");
        loadTeams();
    })
    .catch((error) => {
        console.error('Unexpected error updating team: ', error);
        return false;
    });
}

function deleteTeam() {
    var element = document.getElementById("team_delete_selector");
    element = document.getElementById("team_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    var teamId = selectedElement.getAttribute("team_id");
    var teamName = selectedElement.getAttribute("team_name");

    show("loader");
    fetch(ROOT_URL+'/teams/' + teamId, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("team_delete_id").value = '';
        document.getElementById("team_delete_name").value = '';
        document.getElementById("team_delete_description").value = '';
        document.getElementById("team_delete_forum_link").value = '';

        teamFields = document.querySelectorAll(".team_delete");
        for (var i = 0, teamField; teamField = teamFields[i]; i++) {
            hideElement(teamField);
        }

        successToast("Team '" + teamName + "' deleted");
        loadTeams();
    })
    .catch((error) => {
        console.error('Unexpected error deleting team: ', error);
        return false;
    });
}

function createUser() {
    var foldingUserName = document.getElementById("user_create_folding_name").value.trim();
    var displayName = document.getElementById("user_create_display_name").value.trim();
    var passkey = document.getElementById("user_create_passkey").value.trim();
    var category = document.getElementById("user_create_category").value.trim();
    var profileLink = document.getElementById("user_create_profile_link").value.trim();
    var liveStatsLink = document.getElementById("user_create_live_stats_link").value.trim();
    var hardwareId = document.getElementById("user_create_hardware_selector").value.trim();
    var teamId = document.getElementById("user_create_team_selector").value.trim();
    var isCaptain = document.getElementById("user_create_is_captain").checked;

    var requestData = JSON.stringify(
        {
            "foldingUserName": foldingUserName,
            "displayName": displayName,
            "passkey": passkey,
            "category": category,
            "profileLink": profileLink,
            "liveStatsLink": liveStatsLink,
            "hardwareId": hardwareId,
            "teamId": teamId,
            "userIsCaptain": isCaptain
        }
    );

    show("loader");
    fetch(ROOT_URL+'/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("user_create_folding_name").value = '';
        document.getElementById("user_create_display_name").value = '';
        document.getElementById("user_create_passkey").value = '';
        document.getElementById("user_create_category").value = '';
        document.getElementById("user_create_profile_link").value = '';
        document.getElementById("user_create_live_stats_link").value = '';
        document.getElementById("user_create_hardware_selector").value = '';
        document.getElementById("user_create_team_selector").value = '';
        document.getElementById("user_create_is_captain").checked = false;
        successToast("User '" + displayName + "' created");
        loadUsers();
    })
    .catch((error) => {
        console.error('Unexpected error creating user: ', error);
        return false;
    });
}

function updateUser() {
    var element = document.getElementById("user_update_selector");
    element = document.getElementById("user_update_selector");
    selectedElement = element.options[element.selectedIndex];

    var userId = selectedElement.getAttribute("user_id");

    var foldingUserName = document.getElementById("user_update_folding_name").value.trim();
    var displayName = document.getElementById("user_update_display_name").value.trim();
    var passkey = document.getElementById("user_update_passkey").value.trim();
    var category = document.getElementById("user_update_category").value.trim();
    var profileLink = document.getElementById("user_update_profile_link").value.trim();
    var liveStatsLink = document.getElementById("user_update_live_stats_link").value.trim();
    var hardwareId = document.getElementById("user_update_hardware_selector").value.trim();
    var teamId = document.getElementById("user_update_team_selector").value.trim();
    var isCaptain = selectedElement.getAttribute("user_is_captain").checked;

    var requestData = JSON.stringify(
        {
            "foldingUserName": foldingUserName,
            "displayName": displayName,
            "passkey": passkey,
            "category": category,
            "profileLink": profileLink,
            "liveStatsLink": liveStatsLink,
            "hardwareId": hardwareId,
            "teamId": teamId,
            "userIsCaptain": isCaptain,
        }
    );

    show("loader");
    fetch(ROOT_URL+'/users/' + userId, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("user_update_id").value = '';
        document.getElementById("user_update_folding_name").value = '';
        document.getElementById("user_update_display_name").value = '';
        document.getElementById("user_update_passkey").value = '';
        document.getElementById("user_update_category").value = '';
        document.getElementById("user_update_profile_link").value = '';
        document.getElementById("user_update_live_stats_link").value = '';
        document.getElementById("user_update_hardware_selector").value = '';
        document.getElementById("user_update_team_selector").value = '';
        document.getElementById("user_update_is_captain").checked = false;

        userFields = document.querySelectorAll(".user_delete");
        for (var i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField);
        }

        successToast("User '" + displayName + "' updated");
        loadUsers();
    })
    .catch((error) => {
        console.error('Unexpected error updating user: ', error);
        return false;
    });
}

function deleteUser() {
    var element = document.getElementById("user_delete_selector");
    element = document.getElementById("user_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    var userId = selectedElement.getAttribute("user_id");
    var userDisplayName = selectedElement.getAttribute("user_display_name");

    show("loader");
    fetch(ROOT_URL+'/users/' + userId, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': sessionGet("Authorization")
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

        document.getElementById("user_delete_id").value = '';
        document.getElementById("user_delete_folding_name").value = '';
        document.getElementById("user_delete_display_name").value = '';
        document.getElementById("user_delete_passkey").value = '';
        document.getElementById("user_delete_category").value = '';
        document.getElementById("user_delete_profile_link").value = '';
        document.getElementById("user_delete_live_stats_link").value = '';
        document.getElementById("user_delete_hardware_selector").value = '';
        document.getElementById("user_delete_team_selector").value = '';
        document.getElementById("user_delete_is_captain").checked = false;

        userFields = document.querySelectorAll(".user_delete");
        for (var i = 0, userField; userField = userFields[i]; i++) {
            hideElement(userField);
        }

        successToast("User '" + userDisplayName + "' deleted");
        loadUsers();
    })
    .catch((error) => {
        console.error('Unexpected error deleting user: ', error);
        return false;
    });
}

function offsetUser() {
    var element = document.getElementById("user_offset_selector");
    element = document.getElementById("user_offset_selector");
    selectedElement = element.options[element.selectedIndex];

    var userId = selectedElement.getAttribute("user_id");
    var displayName = selectedElement.getAttribute("user_display_name");

    show("loader");
    fetch(ROOT_URL+'/tc_stats/users/' + userId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        var startPoints = jsonResponse['multipliedPoints'];
        var startUnits = jsonResponse['units'];
        var endPoints = document.getElementById("user_offset_points").value;
        var endUnits = document.getElementById("user_offset_units").value

        var offsetPoints = (endPoints - startPoints);
        var offsetUnits = (endUnits - startUnits);

        var requestData = JSON.stringify(
            {
                "multipliedPointsOffset": offsetPoints,
                "unitsOffset": offsetUnits
            }
        );

        fetch(ROOT_URL+'/users/' + userId, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': sessionGet("Authorization")
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

            document.getElementById("user_offset_points").value = '';
            document.getElementById("user_offset_units").value = '';
            successToast("User '" + displayName + "' stats updated: " + offsetPoints + " points, " + offsetUnits + " units");
            loadUsers();
        })
        .catch((error) => {
            console.error('Unexpected error offsetting user stats: ', error);
            return false;
        });
    });
}
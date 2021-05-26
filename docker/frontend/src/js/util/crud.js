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

function createUser() {
    var foldingUserName = document.getElementById("user_create_folding_name").value.trim();
    var displayName = document.getElementById("user_create_display_name").value.trim();
    var passkey = document.getElementById("user_create_passkey").value.trim();
    var category = document.getElementById("user_create_category").value.trim();
    var hardwareId = document.getElementById("user_create_hardware_selector").value.trim();
    var profileLink = document.getElementById("user_create_profile_link").value.trim();
    var liveStatsLink = document.getElementById("user_create_live_stats_link").value.trim();
    var isRetired = false;

    var requestData = JSON.stringify(
        {
            "foldingUserName": foldingUserName,
            "displayName": displayName,
            "passkey": passkey,
            "category": category,
            "hardwareId": hardwareId,
            "profileLink": profileLink,
            "liveStatsLink": liveStatsLink,
            "isRetired": isRetired
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

        document.getElementById("user_create_id").value = '';
        document.getElementById("user_create_folding_name").value = '';
        document.getElementById("user_create_display_name").value = '';
        document.getElementById("user_create_passkey").value = '';
        document.getElementById("user_create_category").value = '';
        document.getElementById("user_create_hardware_selector").value = '';
        document.getElementById("user_create_profile_link").value = '';
        document.getElementById("user_create_live_stats_link").value = '';
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
    var hardwareId = document.getElementById("user_update_hardware_selector").value.trim();
    var profileLink = document.getElementById("user_update_profile_link").value.trim();
    var liveStatsLink = document.getElementById("user_update_live_stats_link").value.trim();
    var isRetired = selectedElement.getAttribute("user_is_retired");

    var requestData = JSON.stringify(
        {
            "foldingUserName": foldingUserName,
            "displayName": displayName,
            "passkey": passkey,
            "category": category,
            "hardwareId": hardwareId,
            "profileLink": profileLink,
            "liveStatsLink": liveStatsLink,
            "isRetired": isRetired
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
        document.getElementById("user_update_hardware_selector").value = '';
        document.getElementById("user_update_profile_link").value = '';
        document.getElementById("user_update_live_stats_link").value = '';
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
        document.getElementById("user_delete_hardware_selector").value = '';
        document.getElementById("user_delete_profile_link").value = '';
        document.getElementById("user_delete_live_stats_link").value = '';

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
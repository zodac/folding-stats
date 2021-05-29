function populateHardwareUpdate(){
    show("loader");
    element = document.getElementById("hardware_update_selector");
    selectedElement = element.options[element.selectedIndex];

    document.getElementById("hardware_update_id").value = selectedElement.getAttribute("hardware_id");
    document.getElementById("hardware_update_name").value = selectedElement.getAttribute("hardware_name");
    document.getElementById("hardware_update_display_name").value = selectedElement.getAttribute("display_name");
    document.getElementById("hardware_update_operating_system").value = selectedElement.getAttribute("operating_system");
    document.getElementById("hardware_update_multiplier").value = selectedElement.getAttribute("multiplier");

    hardwareFields = document.querySelectorAll(".hardware_update");
    for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField);
    }
    hide("loader");
}

function populateHardwareDelete(){
    show("loader");
    element = document.getElementById("hardware_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    document.getElementById("hardware_delete_id").value = selectedElement.getAttribute("hardware_id");
    document.getElementById("hardware_delete_name").value = selectedElement.getAttribute("hardware_name");
    document.getElementById("hardware_delete_display_name").value = selectedElement.getAttribute("display_name");
    document.getElementById("hardware_delete_operating_system").value = selectedElement.getAttribute("operating_system");
    document.getElementById("hardware_delete_multiplier").value = selectedElement.getAttribute("multiplier");

    hardwareFields = document.querySelectorAll(".hardware_delete");
    for (var i = 0, hardwareField; hardwareField = hardwareFields[i]; i++) {
        showElement(hardwareField);
    }
    hide("loader");
}

function populateUserUpdate(){
    show("loader");
    element = document.getElementById("user_update_selector");
    selectedElement = element.options[element.selectedIndex];

    profileLink = selectedElement.getAttribute("user_profile_link");
    liveStatsLink = selectedElement.getAttribute("user_live_stats_link");

    document.getElementById("user_update_id").value = selectedElement.getAttribute("user_id");
    document.getElementById("user_update_folding_name").value = selectedElement.getAttribute("user_folding_name");
    document.getElementById("user_update_display_name").value = selectedElement.getAttribute("user_display_name");
    document.getElementById("user_update_passkey").value = selectedElement.getAttribute("user_passkey");
    document.getElementById("user_update_category").value = selectedElement.getAttribute("user_category");
    document.getElementById("user_update_hardware_selector").value = selectedElement.getAttribute("user_hardware_id");
    document.getElementById("user_update_team_selector").value = selectedElement.getAttribute("user_team_id");

    if (selectedElement.getAttribute("user_is_captain") === "true") {
        document.getElementById("user_update_is_captain").checked = true;
    } else {
        document.getElementById("user_update_is_captain").checked = false;
    }

    if (profileLink !== 'undefined') {
        document.getElementById("user_update_profile_link").value = profileLink;
    } else {
        document.getElementById("user_update_profile_link").value = '';
    }

    if(liveStatsLink !== 'undefined'){
        document.getElementById("user_update_live_stats_link").value = liveStatsLink;
    } else {
        document.getElementById("user_update_live_stats_link").value = '';
    }

    userFields = document.querySelectorAll(".user_update");
    for (var i = 0, userField; userField = userFields[i]; i++) {
        showElement(userField);
    }
    hide("loader");
}

function populateUserDelete(){
    show("loader");
    element = document.getElementById("user_delete_selector");
    selectedElement = element.options[element.selectedIndex];

    profileLink = selectedElement.getAttribute("user_profile_link");
    liveStatsLink = selectedElement.getAttribute("user_live_stats_link");

    document.getElementById("user_delete_id").value = selectedElement.getAttribute("user_id");
    document.getElementById("user_delete_folding_name").value = selectedElement.getAttribute("user_folding_name");
    document.getElementById("user_delete_display_name").value = selectedElement.getAttribute("user_display_name");
    document.getElementById("user_delete_passkey").value = selectedElement.getAttribute("user_passkey");
    document.getElementById("user_delete_category").value = selectedElement.getAttribute("user_category");
    document.getElementById("user_delete_hardware_selector").value = selectedElement.getAttribute("user_hardware_id");
    document.getElementById("user_delete_team_selector").value = selectedElement.getAttribute("user_team_id");

    if (selectedElement.getAttribute("user_is_captain") === "true") {
        document.getElementById("user_delete_is_captain").checked = true;
    } else {
        document.getElementById("user_delete_is_captain").checked = false;
    }

    if (profileLink !== 'undefined') {
        document.getElementById("user_delete_profile_link").value = profileLink;
    } else {
        document.getElementById("user_delete_profile_link").value = '';
    }

    if (liveStatsLink !== 'undefined'){
        document.getElementById("user_delete_live_stats_link").value = liveStatsLink;
    } else {
        document.getElementById("user_delete_live_stats_link").value = '';
    }

    userFields = document.querySelectorAll(".user_delete");
    for (var i = 0, userField; userField = userFields[i]; i++) {
        showElement(userField);
    }
    hide("loader");
}

function populateUserOffset(){
    show("loader");
    element = document.getElementById("user_offset_selector");
    selectedElement = element.options[element.selectedIndex];

    userId = selectedElement.getAttribute("user_id");

    document.getElementById("user_offset_id").value = userId;
    document.getElementById("user_offset_folding_name").value = selectedElement.getAttribute("user_folding_name");
    document.getElementById("user_offset_display_name").value = selectedElement.getAttribute("user_display_name");

    fetch(ROOT_URL+'/tc_stats/users/' + userId)
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        document.getElementById("user_offset_points").value = jsonResponse['multipliedPoints'];
        document.getElementById("user_offset_units").value = jsonResponse['units'];
    });

    userFields = document.querySelectorAll(".user_offset");
    for (var i = 0, userField; userField = userFields[i]; i++) {
        showElement(userField);
    }
    hide("loader");
}
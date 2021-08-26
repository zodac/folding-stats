const NUMBER_OF_MONTHS = 12;
const NUMBER_OF_YEARS_TO_SHOW = 2;
const YEAR_START = new Date().getUTCFullYear();

function populateUserDropdown(dropdownId) {
    var dropdown = document.getElementById(dropdownId);
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    fetch(ROOT_URL+'/users')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        userDropdownDiv = document.getElementById(dropdownId);

        jsonResponse.forEach(function(userItem, i){
            userButton = document.createElement("button");
            userButton.setAttribute("class", "dropdown-item");
            userButton.setAttribute("type", "button");
            userButton.setAttribute("onclick", "getUserHistoricStats("+userItem["id"]+",'"+JSON.stringify(userItem["displayName"])+"',null,null,null,null)");
            userButton.innerHTML = userItem["displayName"];

            userDropdownDiv.append(userButton);
        });
    });
}

function populateTeamDropdown(dropdownId) {
    var dropdown = document.getElementById(dropdownId);
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    fetch(ROOT_URL+'/teams')
    .then(response => {
        return response.json();
    })
    .then(function(jsonResponse) {
        teamDropDownDiv = document.getElementById(dropdownId);

        jsonResponse.forEach(function(teamItem, i){
            teamButton = document.createElement("button");
            teamButton.setAttribute("class", "dropdown-item");
            teamButton.setAttribute("type", "button");
            teamButton.setAttribute("onclick", "getTeamHistoricStats("+teamItem["id"]+",'"+JSON.stringify(teamItem["teamName"])+"',null,null,null,null)");
            teamButton.innerHTML = teamItem["teamName"];

            teamDropDownDiv.append(teamButton);
        });
    });
}

function populateDayDropdown(month, year, dropdownId, historicFunctionName) {
    var dropdown = document.getElementById(dropdownId);
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    dayDropdownDiv = document.getElementById(dropdownId);
    var numberOfDaysInMonth = new Date(year, month, 0).getDate();

    if(selectedDay > numberOfDaysInMonth){
        selectedDay = numberOfDaysInMonth;
    }

    for (i = 0; i < numberOfDaysInMonth; i++) {
        var loopDay = ordinalSuffixOf((i+1));

        dayButton = document.createElement("button");
        dayButton.setAttribute("class", "dropdown-item");
        dayButton.setAttribute("type", "button");
        dayButton.setAttribute("onclick", historicFunctionName + "(0,null,'"+(i+1)+"',null,null,null)");
        dayButton.innerHTML = loopDay;

        dayDropdownDiv.append(dayButton);
    }

    dayDropdownTitle = document.getElementById(dropdownId + "_root");
    dayDropdownTitle.innerHTML = ordinalSuffixOf(selectedDay);
}


function populateMonthDropdown(dropdownId, historicFunctionName) {
    var dropdown = document.getElementById(dropdownId);
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    monthDropdownDiv = document.getElementById(dropdownId);

    for (i = 0; i < NUMBER_OF_MONTHS; i++) {
        var loopMonthName = new Date(YEAR_START, i, 1).toLocaleString('default', { month: 'long' });

        monthButton = document.createElement("button");
        monthButton.setAttribute("class", "dropdown-item");
        monthButton.setAttribute("type", "button");
        monthButton.setAttribute("onclick", historicFunctionName + "(0,null,null,"+(i+1)+",'"+loopMonthName+"',null)");
        monthButton.innerHTML = loopMonthName;

        monthDropdownDiv.append(monthButton);
    }

    monthDropdownTitle = document.getElementById(dropdownId + "_root");
    monthDropdownTitle.innerHTML = selectedMonthName;
}

function populateYearDropdown(dropdownId, historicFunctionName) {
    var dropdown = document.getElementById(dropdownId);
    while (dropdown.firstChild) {
        dropdown.removeChild(dropdown.lastChild);
    }

    yearDropdownDiv = document.getElementById(dropdownId);

    for (i = 0; i < NUMBER_OF_YEARS_TO_SHOW; i++) {
        var loopYear = (YEAR_START - i);

        yearButton = document.createElement("button");
        yearButton.setAttribute("class", "dropdown-item");
        yearButton.setAttribute("type", "button");
        yearButton.setAttribute("onclick", historicFunctionName + "(0,null,null,null,null,"+loopYear+")");
        yearButton.innerHTML = loopYear;

        yearDropdownDiv.append(yearButton);
    }

    yearDropdownTitle = document.getElementById(dropdownId + "_root");
    yearDropdownTitle.innerHTML = selectedYear;
}
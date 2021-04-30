// Lifted/stole this code from W3 Schools, then cleaned it:
// https://www.w3schools.com/howto/howto_js_sort_table.asp
function sortTable(columnIndex, tableId) {
    var table, rows, switching, i, first, second, shouldSwitch, dir, switchCount = 0;
    table = document.getElementById(tableId);
    switching = true;
    dir = "asc";

    // Make a loop that will continue until no switching has been done
    while (switching) {
        // Start by saying no switching is done
        switching = false;
        rows = table.rows;
        // Loop through all table rows (except the first, which contains table headers)
        for (i = 1; i < (rows.length - 1); i++) {
            // Start by saying there should be no switching:
            shouldSwitch = false;
          
            // Get the two elements you want to compare, one from current row and one from the next
            first = rows[i].getElementsByTagName("TD")[columnIndex];
            second = rows[i + 1].getElementsByTagName("TD")[columnIndex];
         
            // Check if the two rows should switch place, based on the direction, asc or desc
            if (dir == "asc") {
                if (isNaN(first.innerHTML) || isNaN(second.innerHTML)){
                    if (first.innerHTML.toLowerCase() > second.innerHTML.toLowerCase()) {
                        shouldSwitch = true;
                        break;
                    }
                } else {
                    if (parseInt(first.innerHTML) > parseInt(second.innerHTML)) {
                        shouldSwitch = true;
                        break;
                    }
                }
            } else if (dir == "desc") {
                if (isNaN(first.innerHTML) || isNaN(second.innerHTML)){
                    if (first.innerHTML.toLowerCase() < second.innerHTML.toLowerCase()) {
                        shouldSwitch = true;
                        break;
                    }
                } else {
                    if (parseInt(first.innerHTML) < parseInt(second.innerHTML)) {
                        shouldSwitch = true;
                        break;
                    }
                }
            }
        }
        
        if (shouldSwitch) {
            // If a switch has been marked, make the switch and mark that a switch has been done
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
            
            // Each time a switch is done, increase this count by 1:
            switchCount++;
        } else {
            // If no switching has been done AND the direction is "asc", set the direction to "desc" and run the while loop again
            if (switchCount == 0 && dir == "asc") {
                dir = "desc";
                switching = true;
            }
        }
    }
}
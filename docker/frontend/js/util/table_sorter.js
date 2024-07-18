/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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
// Lifted/stole this code from W3 Schools, then cleaned it:
// https://www.w3schools.com/howto/howto_js_sort_table.asp
function sortTable(columnIndex, tableId) {
    var table, rows, switching, i, first, second, shouldSwitch, dir, switchCount = 0
    table = document.getElementById(tableId)
    switching = true
    dir = "asc"

    // Make a loop that will continue until no switching has been done
    while (switching) {
        // Start by saying no switching is done
        switching = false
        rows = table.rows
        // Loop through all table rows (except the first, which contains table headers)
        for (i = 1; i < (rows.length - 1); i++) {
            // Start by saying there should be no switching:
            shouldSwitch = false
          
            // Get the two elements you want to compare, one from current row and one from the next
            firstElement = rows[i].getElementsByTagName("td")[columnIndex]
            secondElement = rows[i + 1].getElementsByTagName("td")[columnIndex]

            // Removing commas to check formatted numbers
            // Making lowercase so sort is case-insensitive
            first = firstElement.innerHTML.replaceAll(",", "").toLowerCase()
            second = secondElement.innerHTML.replaceAll(",", "").toLowerCase()
         
            // Check if the two rows should switch place, based on the direction, asc or desc
            if (dir == "asc") {
                if (isNaN(first) || isNaN(second)){
                    if (first > second) {
                        shouldSwitch = true
                        break
                    }
                } else {
                    if (parseInt(first) > parseInt(second)) {
                        shouldSwitch = true
                        break
                    }
                }
            } else if (dir == "desc") {
                if (isNaN(first) || isNaN(second)){
                    if (first < second) {
                        shouldSwitch = true
                        break
                    }
                } else {
                    if (parseInt(first) < parseInt(second)) {
                        shouldSwitch = true
                        break
                    }
                }
            }
        }
        
        if (shouldSwitch) {
            // If a switch has been marked, make the switch and mark that a switch has been done
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i])
            switching = true
            
            // Each time a switch is done, increase this count by 1:
            switchCount++
        } else {
            // If no switching has been done AND the direction is "asc", set the direction to "desc" and run the while loop again
            if (switchCount == 0 && dir == "asc") {
                dir = "desc"
                switching = true
            }
        }
    }
}

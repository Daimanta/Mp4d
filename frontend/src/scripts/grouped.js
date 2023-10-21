let contents = [];
function group_by_artist() {
    populate_table("/api/v1/grouped/artist", "string");
}

function group_by_album() {
    populate_table("/api/v1/grouped/album", "string");
}

function group_by_year() {
    populate_table("/api/v1/grouped/year", "integer");
}

function group_by_genre() {
    populate_table("/api/v1/grouped/genre", "string");
}

function populate_table(url, type) {
    fetch(url).then(
        async (response) => {
            let data = await response.json();;
            contents.length = 0;
            for (let group of data) {
                contents.push(group);
            }

            const elem = document.getElementById("content_id");
            elem.replaceChildren();
            for (let group of contents) {
                const row = document.createElement("tr");
                const name_td = document.createElement("td");
                name_td.colSpan = 2;
                name_td.align = 'left';
                const link = document.createElement("a");
                link.addEventListener("click", () => {
                    alert("Not finished atm!");
                });
                if (type === "string") {
                    link.textContent = group.string;
                } else if (type === "integer") {
                    link.textContent = group.integer;
                }

                link.href = "#";
                name_td.appendChild(link);

                row.appendChild(name_td);

                const count_td = document.createElement("td");
                count_td.textContent = group.count + " songs";
                row.appendChild(count_td);

                elem.appendChild(row);
            }
        }
    )
}
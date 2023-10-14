let contents = [];
function group_by_artist() {
    fetch('/api/v1/grouped/artist').then(
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
                link.textContent = group.string;
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
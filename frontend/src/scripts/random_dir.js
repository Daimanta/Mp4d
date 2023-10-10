let breadCrumbs = [];

function init_breadcrumbs(id) {
    fetch('/api/v1/folderpath/'+(id || 0)).then(
        async (response) => {
            const data = await response.json();
            let i = 0;
            breadCrumbs = [];
            while (i < data.length) {
                breadCrumbs.push({"name": data[i].name, "id": data[i].id, "index": i});
                i++;
            }
            update_breadcrumbs();
        }
    )
}

function update_breadcrumbs() {
    update_breadcrumb_element(document.getElementById("upper_breadcrumb_id"));
    update_breadcrumb_element(document.getElementById("lower_breadcrumb_id"));
}

function update_breadcrumb_element(parent) {
    parent.replaceChildren();
    let first = true;
    for (let crumb of breadCrumbs) {
        if (first) {
            first = false;
        } else {
            parent.appendChild(document.createTextNode(" · "));
        }
        const link = document.createElement("a");
        link.href = "#";
        link.textContent = crumb.name === "/" ? "Home" : crumb.name;
        link.addEventListener('click', () => {
            navigate_breadcrumb(crumb.id, crumb.index);
        });
        parent.appendChild(link);
    }
}

function generate_random_directory() {
    fetch('/api/v1/randomfolder').then(
        async (response) => {
            const data = await response.json();
            init_breadcrumbs(data.id);
            [document.getElementById("upper_play_id"), document.getElementById("lower_play_id")].forEach((x) => {
               x.addEventListener('click', () => {
                    fetch('/api/v1/folderplaylist/' + data.id).then(
                        res => res.blob()
                    ).then(blob => {
                        const file = window.URL.createObjectURL(blob);
                        let fileLink = document.createElement('a');
                        fileLink.href = file;
                        fileLink.download = data.id+`.m3u8`;
                        fileLink.click()
                    });
                });
            });

            const table = document.getElementById("random_list_id");
            for (let song of data.songs) {
                const tr = document.createElement("tr");

                const empty = document.createElement("td");
                empty.width = "10%";
                empty.textContent = " ";
                tr.appendChild(empty);

                const name = document.createElement("td");
                const name_link = document.createElement("a");
                name_link.href = song.uuid;
                name_link.textContent = song.name;
                name.appendChild(name_link);
                tr.appendChild(name);

                const download = document.createElement("td");
                download.align = "right";
                download.appendChild(document.createTextNode("["));
                download.appendChild(getLink("my_link", "Info"));
                download.appendChild(document.createTextNode("] ["));
                download.appendChild(getLink("my_link_2", "Download"));
                download.appendChild(document.createTextNode("]"));
                tr.appendChild(download);

                table.appendChild(tr);
            }
        }
    )
}
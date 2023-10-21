function store_search() {
    const filter_type_node = get_first_checked("type");

    const result = {
        "all": filter_type_node.value === "all",
        "artist": document.getElementById("artist_input_id").value || null,
        "album": document.getElementById("album_input_id").value || null,
        "song": document.getElementById("song_input_id").value || null,
        "year": document.getElementById("year_input_id").value || null,
        "start_folder": null,
    };
    sessionStorage.setItem("search", JSON.stringify(result));
    window.location = "search_results.html";
}

let search_result_songs = [];

function do_search() {
    fetch('/api/v1/search', {
        method: 'POST',
        body: sessionStorage.getItem("search"),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(
        async (response) => {
            const data = await response.json();
            const element = document.getElementById("list_id");
            search_result_songs.length = 0;
            for (let song of data) {
                const tr = document.createElement("tr");
                const song_td = document.createElement("td");
                song_td.align = "left";
                song_td.width = "50%";
                song_td.appendChild(get_link("/api/v1/songplaylist/" + song.uuid, song.name));
                tr.appendChild(song_td);


                const folder_td = document.createElement("td");
                folder_td.align = "left";
                folder_td.appendChild(get_link("/index.html?id="+song.folder, song.folderName));
                tr.appendChild(folder_td);

                element.appendChild(tr);
                search_result_songs.push(song.uuid);
            }
        }
    )
}

function play_search_results() {
    rest_post_with_name("/api/v1/songplaylist/",
        {
            method: 'POST',
            body: JSON.stringify(search_result_songs),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
    }, "multi.m3u8");
}
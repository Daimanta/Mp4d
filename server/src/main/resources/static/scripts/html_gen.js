
function generate_tabmenu(this_url) {
    const tabs = [
        ["/index.html", "Music"],
        ["/tag_browse.html", "Browse by tag"],
        ["/playlist.html", "Custom Playlist"],
        ["/random.html", "Random Selection"],
        ["/random_directory.html", "Random Directory"],
        ["/preferences.html", "Preferences"],
        ["/search.html", "Search"],
        ["/statistics.html", "Statistics"],
        ["/extras.html", "Extras"]
    ];
    const top = document.getElementById("tabmenu");
    for (let tab of tabs) {
        const li = document.createElement("li");
        const link = document.createElement("a");
        if (this_url === tab[0]) {
            link.classList.add("active");
        }
        link.href = tab[0];
        link.textContent = tab[1];
        li.appendChild(link);
        top.appendChild(li);
    }
}

function generate_subdirectories() {

    const data = [
        [
            "/Folder1Ex/",
            "Music volume 1",
            "20 songs"
        ],
        [
            "/Folder2Ex/",
            "Musical album 3",
            "23 songs"
        ]
    ];
    const table = document.getElementById("dir_list");
    for (let directory of data) {
        const tr = document.createElement("tr");

        const nbsp = document.createElement("td");
        nbsp.width = "10%";
        nbsp.textContent = " ";
        tr.appendChild(nbsp);

        const name = document.createElement("td");
        const name_link = document.createElement("a");
        name_link.href = directory[0];
        name_link.textContent = directory[1];
        name.appendChild(name_link);
        tr.appendChild(name);

        const content = document.createElement("td")
        content.textContent = directory[2];
        tr.appendChild(content);

        tr.appendChild(document.createElement("td")); // Empty td for lining out columns

        const play = document.createElement("td");
        play.appendChild(document.createTextNode("["));
        const play_link = document.createElement("a");
        play_link.textContent = "Play";
        play_link.href = directory[0];
        play.appendChild(play_link);
        play.appendChild(document.createTextNode("]"));
        tr.appendChild(play);

        table.appendChild(tr);
    }
}

function generate_songs_list() {
    const songs = [
        ["/my/link/foo.mp3", "02. Example Song"]
    ];

    const table = document.getElementById("song_list");
    for (let song of songs) {
        const tr = document.createElement("tr");

        const empty = document.createElement("td");
        empty.width = "10%";
        empty.textContent = " ";
        tr.appendChild(empty);

        const name = document.createElement("td");
        const name_link = document.createElement("a");
        name_link.href = song[0];
        name_link.textContent = song[1];
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

function generate_random_list() {
    //<li>[<a href="/Classical%20TOP%20100%20Volume%202/" title="Visit the directory containing this track.">+</a>] &middot; <a href="/Classical%20TOP%20100%20Volume%202/02.%20Charles%20Gounod%20-%20Faust%20Waltz.mp3.m3u">02. Charles Gounod - Faust Waltz</a></li>
    const songs = [
        ["/my/link/foo.mp3", "02. Example Song", "Folder link"]
    ];
    const table = document.getElementById("random_list");
    for (let song of songs) {
        const li = document.createElement("li");
        appendText(li, "[");
        li.appendChild(getLink(song[0], "+"));
        appendText(li, "] · ");
        li.appendChild(getLink(song[2], song[1]));
        table.appendChild(li);
    }
}

function getLink(link, text) {
    const result = document.createElement("a");
    result.href = link;
    result.textContent = text;
    return result;
}

function appendText(element, text) {
    element.appendChild(document.createTextNode(text));
}

class DirectoryTree {
    id;
    name;
    directoryTrees;
    constructor(id, name, directoryTrees) {
        this.id = id;
        this.name = name;
        this.directoryTrees = directoryTrees;
    }

    isLeaf() {
        return this.directoryTrees == null || this.directoryTrees.length === 0;
    }
}

function generate_tabmenu(this_url) {
    const tabs = [
        ["/index.html", "Music"],
        ["/tag_browse/tag_browse.html", "Browse by tag"],
        ["/playlist.html", "Custom Playlist"],
        ["/random.html", "Random Selection"],
        ["/random_directory.html", "Random Directory"],
        ["/preferences.html", "Preferences"],
        ["/search.html", "Search"],
        ["/statistics/statistics.html", "Statistics"],
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

function visit_subfolder(id) {
    const fetch_string = id ? 'api/v1/folder?id=' + id : 'api/v1/folder'
    fetch(fetch_string).then(
        async (response)=> {
            const data = await response.json();
            const songs = data.songs;
            const dirs = data.subFolders;
            process_songs_list(songs);
            process_dir_lists(dirs);
            if (id) {
                add_breadcrumb({"name": data.name, "id": data.id});
            }
        }
    )
}

function navigate_breadcrumb(id, index) {
    const fetch_string = id ? 'api/v1/folder?id=' + id : 'api/v1/folder'
    fetch(fetch_string).then(
        async (response)=> {
            const data = await response.json();
            const songs = data.songs;
            const dirs = data.subFolders;
            process_songs_list(songs);
            process_dir_lists(dirs);
            trim_breadcrumbs(index);
            setPlaylistUrl();
        }
    )
}

function setPlaylistUrl() {
    const playlistRef = document.getElementById("current_playlist_ref");
    const lowerPlaylistRef = document.getElementById("lower_current_playlist_ref");
    playlistRef.addEventListener("click", () => {
        const fetchId = breadCrumbs[breadCrumbs.length - 1].id || '0';
        window.location = '/api/v1/folderplaylist/'+fetchId;
    });
    lowerPlaylistRef.addEventListener("click", () => {
        const fetchId = breadCrumbs[breadCrumbs.length - 1].id || '0';
        window.location = '/api/v1/folderplaylist/'+fetchId;
    });
}

function process_songs_list(songs) {
    const table = document.getElementById("song_list");
    table.replaceChildren();
    for (let song of songs) {
        const tr = document.createElement("tr");
        const empty = document.createElement("td");
        empty.width = "10%";
        empty.textContent = " ";
        tr.appendChild(empty);

        const name = document.createElement("td");
        const name_link = document.createElement("a");
        name_link.href = "api/v1/songplaylist/"+song.uuid;
        name_link.textContent = song.name;
        name.appendChild(name_link);
        tr.appendChild(name);

        const download = document.createElement("td");
        download.align = "right";
        download.appendChild(document.createTextNode("["));
        download.appendChild(getLink("my_link", "Info"));
        download.appendChild(document.createTextNode("] ["));
        download.appendChild(getLink("api/v1/directplay/" + song.uuid, "Download"));
        download.appendChild(document.createTextNode("]"));
        tr.appendChild(download);

        table.appendChild(tr);
    }
    if (songs.length === 0) {
        document.getElementById("song_wrapper_id").style.visibility = 'hidden';
    } else {
        document.getElementById("song_wrapper_id").style.visibility = 'visible';
    }
}

function process_dir_lists(dirs) {
    const table = document.getElementById("dir_list");
    table.replaceChildren();
    for (let directory of dirs) {
        const tr = document.createElement("tr");

        const nbsp = document.createElement("td");
        nbsp.width = "10%";
        nbsp.textContent = " ";
        tr.appendChild(nbsp);

        const name = document.createElement("td");
        const name_link = document.createElement("a");
        name_link.href = "#";
        name_link.textContent = directory.name;
        name_link.addEventListener('click', () => {
            visit_subfolder(directory.id);
        });
        name.appendChild(name_link);
        tr.appendChild(name);

        const content = document.createElement("td")
        content.textContent = "" + directory.songs + " songs";
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
    if (dirs.length === 0) {
        document.getElementById("dir_wrapper_id").style.visibility = 'hidden';
    } else {
        document.getElementById("dir_wrapper_id").style.visibility='visible';
    }
}


function generate_random_list() {
    fetch('/api/v1/random').then(
        async (response) => {
            const data = await response.json();
            const table = document.getElementById("random_list");
            for (let song of data) {
                const li = document.createElement("li");
                appendText(li, "[");
                li.appendChild(getLink("index.html?id="+song.folder, "+"));
                appendText(li, "] · ");
                li.appendChild(getLink('api/v1/songplaylist/'+song.uuid, song.name));
                table.appendChild(li);
            }
        }
    );
}

function generate_random_directory() {
    const songs = [
        ["/my/link/foo.mp3", "02. Example Song"]
    ];
    const table = document.getElementById("random_list_id");
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

function generate_custom_playlist() {
    fetch('/api/v1/tree').then(
        async (response) => {
            const data = await response.json();
            const tree = getTreeFromData(data);
            const table = document.getElementById("playlist_id");
            addElement(table, tree);
        }
    )
    document.getElementById("playbutton_id").addEventListener("click", () => {
        fetch('/api/v1/multifolderplaylist', {
            method: 'POST',
            body: JSON.stringify(getCheckboxedFolders()),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        });
    })
}

function getCheckboxedFolders() {
    return [1];
}

function getTreeFromData(data) {
    let tree = new DirectoryTree(data.id, data.name, []);
    if (data.children.length === 0) return tree;
    for (let child of data.children) {
        tree.directoryTrees.push(getTreeFromData(child));
    }
    return tree;
}

function addElement(parent, directoryTree) {
    const list = document.createElement("li");

    const checkbox = document.createElement("input");
    checkbox.type="checkbox";
    checkbox.name = directoryTree.id;
    list.appendChild(checkbox);

    const link = document.createElement("a");
    link.href=directoryTree.name;
    link.textContent=directoryTree.name;
    list.appendChild(link);

    if (!directoryTree.isLeaf()) {
        const subList = document.createElement("ul");
        for (let child of directoryTree.directoryTrees) {
            addElement(subList, child);
        }
        list.appendChild(subList);
    }

    parent.appendChild(list);
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
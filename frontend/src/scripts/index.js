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
    const parent = document.getElementById("breadcrumb_parent_id");
    const lower_parent = document.getElementById("lower_breadcrumb_parent_id");
    update_breadcrumb_element(parent);
    update_breadcrumb_element(lower_parent);
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

function trim_breadcrumbs(index) {
    breadCrumbs = breadCrumbs.slice(0, index + 1);
    update_breadcrumbs();
}
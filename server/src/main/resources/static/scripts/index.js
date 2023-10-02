let breadCrumbs = [{"name": "Home", "id": null, "index": 0}];

function update_breadcrumbs() {
    const parent = document.getElementById("breadcrumb_parent_id");
    parent.replaceChildren();
    let first = true;
    for (let crumb of breadCrumbs) {
        if (first) {
            first = false;
        } else {
            parent.appendChild(document.createTextNode(" * "));
        }
        const link = document.createElement("a");
        link.href = "#";
        link.textContent = crumb.name;
        link.addEventListener('click', () => {
            navigate_breadcrumb(crumb.id, crumb.index);
        });
        parent.appendChild(link);
    }
}

function add_breadcrumb(elem) {
        elem.index = breadCrumbs.length;
        breadCrumbs.push(elem);
        update_breadcrumbs();
}

function trim_breadcrumbs(index) {
    breadCrumbs = breadCrumbs.slice(0, index + 1);
    update_breadcrumbs();
}
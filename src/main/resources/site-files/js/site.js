let tags = document.getElementsByClassName("markdown")
for (let i = 0; i < tags.length; i++) {
    let tag = tags[i];
    tag.innerHTML = markdown.toHTML(tag.innerHTML)
}
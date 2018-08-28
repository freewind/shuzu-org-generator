var tags = document.getElementsByClassName("markdown")
var converter = new showdown.Converter();

for (var i = 0; i < tags.length; i++) {
    let tag = tags[i];
    tag.innerHTML = converter.makeHtml(tag.innerHTML)
}
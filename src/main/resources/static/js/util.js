
function truncateTextOfCertainClass(clazz, maxLen) {
    $(clazz).each(function () {
        var text = $(this).text();
        if (text.length > maxLen) {
            $(this).attr('title', text);
            var chunk = text.substring(0, maxLen-1) + '...';
            $(this).text(chunk);
        }
    });
}

function mockFormKv(URL, method, kv) {

    var form = $("<form></form>").attr("action", URL).attr("method", method);
    for (var k in kv) {
        form.append($("<input/>")
            .attr("type", "hidden")
            .attr("name", k)
            .attr("value", kv[k]));
        console.log(k + ' : ' + kv[k]);
    }
    form.appendTo('body').submit().remove();
}
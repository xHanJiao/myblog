
var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");
var token_name = "_csrf";
var header_name = "_csrf_header";
var csrf_kv = {};
csrf_kv[token_name] = token
csrf_kv[header_name] = header

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

function commonInit() {
    $('#modal1').modal();
    $('select').material_select();
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
    if (method === "post" || method === "POST") {
        form.append($("<input/>")
            .attr("type", "hidden")
            .attr("name", "_csrf")
            .attr("value", token));
        form.append($("<input/>")
            .attr("type", "hidden")
            .attr("name", "_csrf_header")
            .attr("value", header));
    }
    console.log('token : ' + token);
    console.log('header : ' + header);
    form.appendTo('body').submit().remove();
}
// var token = $("meta[name='_csrf']").attr("content");
// var header = $("meta[name='_csrf_header']").attr("content");

function mockFormKv(URL, method, kv) {
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");

    var form = $("<form></form>").attr("action", URL).attr("method", method);
    for (var k in kv) {
        form.append($("<input/>")
            .attr("type", "hidden")
            .attr("name", k)
            .attr("value", kv[k]));
        console.log(k + ' : ' + kv[k]);
    }
    // if (method === "post" || method === " POST") {
    //     form.append($("<input/>")
    //         .attr("type", "hidden")
    //         .attr("name", "_csrf")
    //         .attr("value", token));
    //     form.append($("<input/>")
    //         .attr("type", "hidden")
    //         .attr("name", "_csrf_header")
    //         .attr("value", header));
    // }
    console.log("add mock form");

    form.appendTo('body').submit().remove();
}
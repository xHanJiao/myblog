var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");
var token_name = "_csrf";
var header_name = "_csrf_header";
var csrf_kv = {};
var bodyColor = 'grey lighten-3';
csrf_kv[token_name] = token;
csrf_kv[header_name] = header;

function truncateTextOfCertainClass(clazz, maxLen) {
    $(clazz).each(function () {
        var text = $(this).text();
        if (text.length > maxLen) {
            $(this).attr('title', text);
            var chunk = text.substring(0, maxLen - 1) + '...';
            $(this).text(chunk);
        }
    });
}

function commonInit() {

    $('#modal1').modal();
    $('body').addClass(bodyColor);
    $('select').material_select();
    var $sidebar = $(".sidebar");
    if ($sidebar.length > 0) {
        $(function () {
            var $window = $(window),
                offset = $sidebar.offset(),
                topPadding = 40;

            $sidebar.find('div').addClass(bodyColor);
            $('.greeting-text').css('font-size', '13px').addClass('grey-text');
            $('.statistic').css('line-height', '0.5em');

            $window.scroll(function () {
                if ($window.scrollTop() > offset.top) {
                    $sidebar.stop().animate({
                        marginTop: $window.scrollTop() - offset.top + topPadding
                    });
                } else {
                    $sidebar.stop().animate({
                        marginTop: 0
                    });
                }
            });
        });
    }
    var $up_btn = $('.up-btn');
    if ($up_btn.length > 0) {
        $up_btn.click(function () {
            $("html,body").animate({scrollTop: 0}, 100);
            return false;
        });

        $('.down-btn').click(function () {
            $('html, body').animate({scrollTop: $(document).height()}, 500);
            return false;
        });
    }

}

$(document).ready(function () {
    commonInit();
});

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
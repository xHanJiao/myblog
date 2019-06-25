
$(document).ready(function () {

    var $actDiv = $('div[class=collapsible-header]');
    $actDiv.each(function (index) {
        if (index === Math.floor($actDiv.length / 2)) {
            // $(this).addClass('active');
            $(this).trigger('click');
        }
    });

    console.log('$actDiv.length : ' + Math.floor($actDiv.length / 2));

    $('.collapsible').on('mousewheel', function(event) {
        //输出滚轮事件响应结果
        // console.log(event.deltaX, event.deltaY, event.deltaFactor);
        //上下滚动时让鼠标垂直移动
        var actLi = $('li[class=active]');
        if (actLi.length > 0) {
            // console.log(actLi.find('a').text());
            actLi.find('div[class=collapsible-header]').trigger('click');
            if (event.deltaY < 0) {
                actLi.next().find('div[class=collapsible-header]').trigger('click');
            } else if (event.deltaY > 0) {
                actLi.prev().find('div[class=collapsible-header]').trigger('click');
            } else {
                // console.log('no scroll');
            }
        } else {
            $("ul li:first-child").find('div[class=collapsible-header]').trigger('click');
        }
    });

    commonInit();
    truncateTextOfCertainClass('.aTitles', 8);

    $('.cate_title').each(function () {
        var title = $(this).text();
        var num = $(this).siblings('input[type=hidden]').val();
        $(this).text(title + '(' + num + ')');
    });

    $('.bottomBtnHolder')
        .css('padding-left', '5%')
        .css('padding-right', '5%')
        .css('margin-top', '3%');

    $('.modi').on('click', function () {
            var name = $(this).siblings('input[type=hidden]').val();
            // console.log('articleId : ' + name);
            var modiOper = "";
            if ($(this).hasClass('delete')) {
                modiOper = '/del/';
            } else if ($(this).hasClass('pub')) {
                modiOper = '/recover/';
            } else if ($(this).hasClass('hidee')) {
                modiOper = '/hidden/'
            } else if ($(this).hasClass('modiCate')) {
                var contentHolder = $(this).parent().prev();
                var title = contentHolder.children('a').text();
                var content = contentHolder.children('p[class=cate_dscrp]').text();
                // console.log('title : ' + title);
                console.log('content : ' + content);
                $('#cateName').val(title);
                $('#cateDescription').val(content);
                $('#cateForm').attr('action', '/modify/category');
                return true;
            } else {
                // console.log('cannot get modiOper');
                return false;
            }
            var baseURL = '/category';
            $.post(baseURL + modiOper + name, csrf_kv, function () {
                window.location.reload();
            });
        }
    ).hide();

    $('#categoryHolder').hide();

    $('.boardOrCate').click(function () {
        $('#categoryHolder').toggle();
        $('#articleListHolder').toggle();
    });

    $('.setBtn').click(function () {
        $(this).siblings().toggle();
    });

    $('#createCate').click(function () {
        $('#cateForm').attr('action', '/add/category');
        return true;
    });

    $('.logout').click(function () {
        $.post('/logout', csrf_kv, function () {
            window.location.reload();
        });
    });
});

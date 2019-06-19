var baseURL = '/category';

$(document).ready(function () {

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
            console.log('articleId : ' + name);
            var modiOper = "";
            if ($(this).hasClass('delete')) {
                modiOper = '/del/';
            } else if ($(this).hasClass('pub')) {
                modiOper = '/recover/';
            } else if ($(this).hasClass('hidee')) {
                modiOper = '/hidden/'
            } else if ($(this).hasClass('modiCate')) {
                var contentHolder = $(this).parents('div[class=card-action]').prev();
                var title = contentHolder.children('span').text();
                var content = contentHolder.children('p').text();
                $('#cateName').val(title);
                $('#cateDescription').val(content);
                $('#cateForm').attr('action', '/modify/category');
                return true;
            } else {
                console.log('cannot get modiOper');
                return false;
            }
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

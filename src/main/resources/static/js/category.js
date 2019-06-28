$(document).ready(function () {

    $('.modi').on('click', function () {
            var name = $(this).siblings('input[type=hidden]').val();
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
                console.log('content : ' + content);
                $('#cateName').val(title);
                $('#cateDescription').val(content);
                $('#cateForm').attr('action', '/modify/category');
                return true;
            } else {
                return false;
            }
            var baseURL = '/category';
            $.post(baseURL + modiOper + name, csrf_kv, function () {
                window.location.reload();
            });
        }
    ).css('color', '#37474F');

    $('.cate_title').each(function () {
        var title = $(this).text();
        var num = $(this).siblings('input[type=hidden]').val();
        $(this).text(title + '(' + num + ')');
    });
});

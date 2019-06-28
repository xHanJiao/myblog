
$(document).ready(function () {

    $('.createTimeHolder').hide();

    $('#nextPage').click(function () {
        var currentPage = $('#currentPage').val();
        mockFormKv('/index', 'get', {page: parseInt(currentPage) + 1});
    });

    $('#prevPage').click(function () {
        var currentPage = $('#currentPage').val();
        mockFormKv('/index', 'get', {page: parseInt(currentPage) - 1});
    });

    $('.bottomBtnHolder')
        .css('padding-left', '5%')
        .css('padding-right', '5%')
        .css('margin-top', '3%');

    truncateTextOfCertainClass('.aTitles', 8);

    $('.cate_title').each(function () {
        var title = $(this).text();
        var num = $(this).siblings('input[type=hidden]').val();
        $(this).text(title + '(' + num + ')');
    });

    $('#createCate').click(function () {
        $('#cateForm').attr('action', '/add/category');
        return true;
    });



});

function triggerLi(idx) {
    $('.collapsible-header').each(function (index) {
        if (index === idx) {
            $(this).trigger('click');
        }
    });
}

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

    $('.logout').click(function () {
        $.post('/logout', csrf_kv, function () {
            window.location.reload();
        });
    });

    var $collapsible = $('.collapsible');

    $collapsible.collapsible({
        onOpen: function (el) {
            $('.createTimeHolder').hide();
            $(el).find('span[class *= createTimeHolder]').show();
            var sub = initIdx - $(el).index();
            while (sub !== 0) {
                var endOne = $('.titleLi:last'),
                    startOne = $('.titleLi:first');
                if (sub > 0) {
                    startOne.before(endOne);
                    sub -= 1;
                } else {
                    endOne.after(startOne);
                    sub += 1;
                }
            }
        }
    });

    var $actDiv = $('.collapsible-header'),
        totalLiNum = $actDiv.length,
        initIdx = Math.floor(totalLiNum / 2);
    triggerLi(initIdx);

    $actDiv.addClass(bodyColor);

    $collapsible.on('mousewheel', function (event) {
        $('.createTimeHolder').hide();
        var endOne = $('.titleLi:last'),
            startOne = $('.titleLi:first');
        if (event.deltaY < 0) {
            startOne.before(endOne);
        } else if (event.deltaY > 0) {
            endOne.after(startOne);
        } else {

        }
        triggerLi(initIdx);
    });

});

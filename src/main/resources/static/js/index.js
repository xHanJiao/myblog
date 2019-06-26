function triggerLi(idx) {
    $('.collapsible-header').each(function (index) {
        if (index === idx) {
            $(this).trigger('click');
        }
    });
}

$(document).ready(function () {

    $('#nextPage').click(function () {
        var currentPage = $('#currentPage').val();
        mockFormKv('/index', 'get', {page: currentPage + 1});
    });

    $('#prevPage').click(function () {
        var currentPage = $('#currentPage').val();
        mockFormKv('/index', 'get', {page: currentPage - 1});
    });

    $('.bottomBtnHolder')
        .css('padding-left', '5%')
        .css('padding-right', '5%')
        .css('margin-top', '3%');

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
    ).hide();

    truncateTextOfCertainClass('.aTitles', 8);

    $('.cate_title').each(function () {
        var title = $(this).text();
        var num = $(this).siblings('input[type=hidden]').val();
        $(this).text(title + '(' + num + ')');
    });

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

    $(document).ready(function () {
        $('.collapsible').collapsible({
            onOpen: function (el) {
                var sub = initIdx - $(el).index();
                while (sub !== 0) {
                    var endOne = $('.articleLi:last'),
                        startOne = $('.articleLi:first');
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
    });

    var $actDiv = $('.collapsible-header'),
        totalLiNum = $actDiv.length,
        initIdx = Math.floor(totalLiNum / 2);
    triggerLi(initIdx);

    $actDiv.addClass(bodyColor);

    $('.collapsible').on('mousewheel', function (event) {
        var endOne = $('.articleLi:last'),
            startOne = $('.articleLi:first');
        if (event.deltaY < 0) {
            startOne.before(endOne);
        } else if (event.deltaY > 0) {
            endOne.after(startOne);
        } else {

        }
        triggerLi(initIdx);
    });

});

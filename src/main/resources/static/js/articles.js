function addBtn(type) {
    $('.btnHolder').each(function () {
        $(this).append($('<a></a>')
            .addClass('btn modiBtn btn-flat waves-effect waves-light').addClass(type)
            .append($('<i class="material-icons"></i>').text(type)));
    });
}

function getArticleId() {
    return $(this).parent().prev().find('input[type=hidden]').val();
}

$('.articlesHolder').on('click', '.visibility_off', function () {
    var articleId = getArticleId.call(this);
    $.post('/uvpub/' + articleId, csrf_kv, function () {
        window.location.reload();
    })
}).on('click', '.visibility', function () {
    var articleId = getArticleId.call(this);
    $.post('/vpub/' + articleId, csrf_kv, function () {
        window.location.reload();
    })
}).on('click', '.delete', function () {
    var articleId = getArticleId.call(this);
    $.post('/del/article/' + articleId, csrf_kv, function () {
        window.location.reload();
    })
}).on('click', '.delete_forever', function () {
    var articleId = getArticleId.call(this);
    $.post('/del/article/' + articleId, csrf_kv, function () {
        window.location.reload();
    })
}).on('click', '.mode_edit', function () {
    var articleId = getArticleId.call(this);
    mockFormKv('/edit/article', 'get', {id: articleId});

});

$(document).ready(function () {

    // $('#pages').material_select();
    $('.fixed-action-btn').hide();
    truncateTextOfCertainClass('.aTitles', 10);

    $('.modiState').click(function () {
        // 先清理所有已经存在的修改按钮
        var btnHolder = $('.btnHolder');
        if ($(this).parent().siblings('.btnHolder').length > 0) {
            btnHolder.remove();
            return;
        }
        btnHolder.remove();

        var articleId = $(this).siblings('input[type=hidden]').val();
        console.log('articleId : ' + articleId);
        var holder = $('<div></div>').addClass('section btnHolder');
        $(this).parent().after(holder);
        $.get('/article/state/' + articleId, function (data, status) {
            if (status === 'success') {
                switch (data) {
                    case 0:
                        addBtn('visibility');
                        addBtn('visibility_off');
                        addBtn('mode_edit');
                        break;
                    case 1:
                        addBtn('delete');
                        addBtn('visibility_off');
                        addBtn('mode_edit');
                        break;
                    case 2:
                        addBtn('delete');
                        addBtn('visibility');
                        addBtn('mode_edit');
                        break;
                    case 3:
                        addBtn('visibility_off');
                        addBtn('visibility');
                        addBtn('delete_forever');
                        addBtn('mode_edit');
                        break;
                    default:
                        break;
                }
            }
        });
    });
});

function toPage(sobj) {
    var page = sobj.options[sobj.selectedIndex].value;
    var metaUrl = $('#metaUrl').val() + $('.metainf').text();
    mockFormKv(metaUrl, 'get', {page: page});
}

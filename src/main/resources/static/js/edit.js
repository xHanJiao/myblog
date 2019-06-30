
var articleImageFolder = '/articleImages/';

function addCardOfImage(data) {
    $('#picHolder')
        .append($("<div class='onePic col card m3 s12 yellow darken-1'></div>")
            .append($("<div class='card-content'></div>")
                .append($("<img class=\"responsive-img\">").attr('src', data))
                .append($("<p class='flow-text href-holder'>").text(data)))
            .append($("<div class='card-action'></div>")
                .append($("<a onclick=delImg(this)>删除图片</a>").attr('href', '#'))
                .append($("<input type='hidden'/>").val(data))));
}

function uploadFormData(file, url) {
    var formData = new FormData();
    formData.append('picture', file);
    formData.append(token_name, token);
    formData.append(header_name, header);
    $.ajax({
        url: url,
        type: 'post',
        data: formData,
        contentType: false,
        processData: false,
        success: function (data) {
            addCardOfImage(articleImageFolder + data);
        }
    });
}

$('#submitPic').click(function () {
    var file = document.getElementById('pic').files[0];
    var url = '/article/uploadPic';
    if (!file) return false;
    uploadFormData(file, url);
});

function delImg(obj) {
    var name = $(obj).next().val();
    name = name.replace(articleImageFolder, '');
    $.post('/del/image/' + name, csrf_kv, function (data, status) {
        if (status === "success") {
            $(obj).parents('.onePic').remove();
        }
    });
}


var editor = CKEDITOR.replace('editor1', {
    height: 400
});
var content = null;

function getImagePaths() {
    var paths = [];
    $('.href-holder').each(function () {
        paths.push($(this).text());
    });
    return paths;
}

$(document).ready(function () {
    var articleId = $('#modSig').val();

    $('#saveDraft').click(function () {
        var data = {
            title: $('#aTitle').val(), content: content = editor.getData(),
            commentEnable: $('#commentEnable').prop('checked'), state: 0, category: $("#categories").val(),
            imagePaths: getImagePaths(), id: articleId
        };
        mockFormKv('/add/draft', 'POST', data);
    });

    $('#sbmtHistory').click(function () {
        var data = {
            title: $('#aTitle').val(), snapshotContent: editor.getData(),
            imagePaths: getImagePaths(), articleId: articleId
        };
        mockFormKv('/add/history', 'POST', data);
    });

    $('#backToHistory').click(function () {
        var historyId = $('input[name=recordId]:checked').val();
        var data = {};
        data['historyId'] = historyId;
        data['articleId'] = articleId;
        data[token_name] = token;
        data[header_name] = header;

        $.post('/recover/history', data, function (d, status) {
            if (status === 'success') {
                console.log(d);
                editor.setData(d['snapshotContent']);
            }
        })
    });

    $('#viewHistory').click(function () {
        var historyId = $('input[name=recordId]:checked').val();
        var data = {};
        data['historyId'] = historyId;
        data['articleId'] = articleId;
        data[token_name] = token;
        data[header_name] = header;
        $.post('/view/history', data, function (d, status) {
            if (status === "success") {
                $('#viewModal').find('div').html(d).modal('open');
            }
        })
    });

    $('#delHistory').click(function () {
        var historyId = $('input[name=recordId]:checked').val();
        console.log('historyId : ' + historyId);
        mockFormKv('/del/history', 'POST', {historyId: historyId, articleId: articleId});
    });

    if (articleId) {
        $.get('/api/content/' + articleId, function (data, status) {
            if (status === "success") {
                editor.setData(data['article']);
            }
        });
    }
    $('#categories').material_select();
    $('#sbmt').on('click', function () {
        content = editor.getData();
        var modify = $('#modSig').val(),
            cme = $('#commentEnable').prop('checked'),
            pub = $('#isPublished').prop('checked'),
            fns = $('#finished').prop('checked'),
            checkValue = $("#categories").val(),
            paths = getImagePaths();
        var state;
        if (fns) {
            state = 0;
        } else if (pub) {
            state = 1;
        } else {
            state = 2;
        }
        var data = {
            title: $('#aTitle').val(), content: content,
            commentEnable: cme, state: state, category: checkValue,
            imagePaths: paths
        };
        if (modify) {
            mockFormKv('/modify/article/' + modify, 'POST', data);
        } else {
            mockFormKv('/add/article', 'POST', data);
        }
    });
});



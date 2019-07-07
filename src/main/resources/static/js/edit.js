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

    CKEDITOR.instances.editor1.on('blur', function () {
        if (editor.getData()) {
            var data = {content: editor.getData()};
            data[token_name] = token;
            data[header_name] = header;
            if (articleId) {
                $.post('/api/content/' + articleId, data,
                    function (d, status) {
                    });
            }
        }
    });

    $('#sbmtHistory').click(function () {
        var data = {
            "title": $('#aTitle').val(), "snapshotContent": editor.getData(),
            'imagePaths': getImagePaths(), 'articleId': articleId
        };
        var jsonStr = JSON.stringify(data);
        $.ajax({
            type: 'POST',
            url: '/add/history',
            contentType: "application/json",
            data: jsonStr,
            beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function (d) {
                console.log(d);
                var $historyHolder = $('.historyHolder'),
                    recordId = d['recordId'],
                    labelText = d['title'] + '-' + d['createTime'];
                $historyHolder.append($('<p></p>').addClass('history')
                    .append($('<input type="radio"/>')
                        .attr('id', recordId).val(recordId).attr('name', 'recordId'))
                    .append($('<label></label>')
                        .attr('for', recordId).text(labelText)));
            }
        });
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
        var $checkedOne = $('input[name=recordId]:checked'),
            historyId = $checkedOne.val(),
            data = {historyId: historyId, articleId: articleId},
            jsonStr = JSON.stringify(data);
        $.ajax({
            type: 'POST',
            url: '/del/history',
            contentType: "application/json",
            data: jsonStr,
            beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function () {
                $checkedOne.parent().remove();
            }
        })
        // mockFormKv('/del/history', 'POST', {historyId: historyId, articleId: articleId});
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
        var modify = $('#modSig').val(),
            pub = $('#isPublished').prop('checked'),
            fns = $('#finished').prop('checked');
        var state;
        if (fns) {
            state = 0;
        } else if (pub) {
            state = 1;
        } else {
            state = 2;
        }
        var data = {
            title: $('#aTitle').val(), content: editor.getData(),
            commentEnable: $('#commentEnable').prop('checked'), state: state,
            category: $("#categories").val(), imagePaths: getImagePaths()
        };
        if (modify) {
            mockFormKv('/modify/article/' + modify, 'POST', data);
        } else {
            mockFormKv('/add/article', 'POST', data);
        }
    });
});



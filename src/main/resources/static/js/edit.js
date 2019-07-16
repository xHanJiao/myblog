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

$('#submitPic').click(function () {
    var file = document.getElementById('pic').files[0];
    var url = '/article/uploadPic';
    if (!file) return false;
    var formData = new FormData();
    formData.append('picture', file);
    $.ajax({
        url: url,
        type: 'post',
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        data: formData,
        contentType: false,
        processData: false,
        success: function (data) {
            addCardOfImage(articleImageFolder + data);
        }
    });
});

function delImg(obj) {
    var name = $(obj).next().val();
    name = name.replace(articleImageFolder, '');
    $.post('/del/image/' + name, csrf_kv, function (data) {
        $(obj).parents('.onePic').remove();
    });
}

var editor = CKEDITOR.replace('editor1', {
    height: 400
});

function getImagePaths() {
    var paths = [];
    $('.href-holder').each(function () {
        paths.push($(this).text());
    });
    return paths;
}

$(document).ready(function () {
    var $modSig = $('#modSig'),
        articleId = $modSig.val();

    if (articleId) {
        $.get('/api/content/' + articleId, function (data, status) {
            if (status === "success") {
                editor.setData(data['article']);
            }
        });
    }

    CKEDITOR.instances.editor1.on('blur', saveHistory);

    function saveHistory() {
        var aId = $modSig.val(),
            data = {
                "title": $('#aTitle').val(), "snapshotContent": editor.getData(),
                'articleId': aId, 'imagePaths': getImagePaths()
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
                var $historyHolder = $('.historyHolder'),
                    recordId = d['recordId'],
                    articleId = d['articleId'],
                    labelText = d['title'] + '-' + d['createTime'];
                if ($historyHolder.children('p').length > 2) {
                    console.log('more than 3 children, current children is ' + $historyHolder.children('p').length);
                    $('div.historyHolder p:first').remove().next().remove();
                }
                $historyHolder.append($('<p></p>').addClass('history')
                    .append($('<input type="radio"/>')
                        .attr('id', recordId).val(recordId).attr('name', 'recordId'))
                    .append($('<label></label>')
                        .attr('for', recordId).text(labelText)));
                if (!aId) {
                    $modSig.val(articleId);
                }
            }
        });
    }

    $('#sbmtHistory').click(saveHistory);

    $('#backToHistory').click(function () {
        var data = {}, aId = $modSig.val();
        data['historyId'] = $('input[name=recordId]:checked').val();
        data['articleId'] = aId;
        data[token_name] = token;
        data[header_name] = header;

        $.post('/recover/history', data, function (d) {
            editor.setData(d['snapshotContent']);
            $('#aTitle').val(d['title']);
        })
    });

    $('#viewHistory').click(function () {
        var data = {}, aId = $modSig.val();
        data['historyId'] = $('input[name=recordId]:checked').val();
        data['articleId'] = aId;
        data[token_name] = token;
        data[header_name] = header;
        $.post('/view/history', data, function (d) {
            $('#viewModal').find('div').html(d).modal('open');
        })
    });

    $('#delHistory').click(function () {
        var $checkedOne = $('input[name=recordId]:checked'),
            historyId = $checkedOne.val(),
            aId = $modSig.val(),
            data = {historyId: historyId, articleId: aId},
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
    });

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



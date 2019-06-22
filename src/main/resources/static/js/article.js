$(document).ready(function () {

    commonInit();

    $('li').css('list-style-type', '.disc');
    // $('li').css('list-style-type', '.decimal');

    $('img').addClass('responsive-img');

    var addCommDiv = $('#addCommDiv');
    addCommDiv.hide();

    $('#commBtn').on('click', function () {
        addCommDiv.toggle();
        $('#content').val("");
        $("html,body").animate({scrollTop: addCommDiv.offset().top}, 1000);
    });

    $('.stateBtn').addClass('btn-floating');

    $('.reply').on('click', function () {
        var title = 'reply to: @' +
            $(this).siblings('input[class=replyTo]').val() + '   ';
        console.log(title);
        addCommDiv.show();
        $('#content').val(title);
        $("html,body").animate({scrollTop: addCommDiv.offset().top}, 1000);
    });

    $('#uphead').on('click', function () {
        $("html,body").animate({scrollTop: 0}, 1000);
        return false;
    });

    $('.delcomm').on('click', function () {
        var delContent = $(this).parents('div[class=row]')
            .prev().find('p').text();
        console.log('delcomm : ' + delContent);
        mockFormKv('/del/comment', 'POST', {content: delContent, articleId: $('#articleId').val()});
    });

    $('#editBtn').on('click', function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        mockFormKv('/edit/article/' + articleId, 'get', {});
    });

    $('#vpub').on('click', function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        mockFormKv('/vpub/' + articleId, 'post', {});
    });

    $('#uvpub').on('click', function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        mockFormKv('/uvpub/' + articleId, 'post', {});
    });

    $('.deleteBtn').on('click', function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        // mockFormKv('/del/article/' + articleId, 'POST', {});
        $.post('/del/article/' + articleId, csrf_kv, function () {
            window.location.replace('/index');
        });
    });

    $('.hideBtn').click(function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        mockFormKv('/uvpub/' + articleId, 'POST', {});
    });

    $('.showBtn').click(function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        mockFormKv('/vpub/' + articleId, 'POST', {});
    });

    $('#recoverBtn').on('click', function () {
        var articleId = $('#articleId').val();
        console.log('articleId : ' + articleId);
        $.post('/recover/article/' + articleId, csrf_kv, function () {
            window.location.reload();
        });
    });
});

$('#sbmt').click(function () {
    var creator = $('#creator'), email = $('#email'),
        content = $('#content'), URL = '/addcomm',
        articleId = $('#articleId').val();
    mockFormKv(URL, "POST", {
        articleId: articleId, email: email.val(),
        content: content.val(), creator: creator.val()
    });
    return true;
});

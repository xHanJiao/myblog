function addMyAnchorClass($articleHolder, hn) {
    if ($articleHolder.find(hn).length > 0) {
        $articleHolder.find(hn).addClass('myAnchor');
    }
}

$(document).ready(function () {

    $('table').addClass('bordered');

    $('li').css('list-style-type', '.disc');

    $('img').addClass('responsive-img');

    var addCommDiv = $('#addCommDiv');
    addCommDiv.hide();

    $('#commBtn').on('click', function () {
        addCommDiv.toggle();
        $('#content').val("");
        $("html,body").animate({scrollTop: addCommDiv.offset().top}, 1000);
    });

    var $articleHolder = $('.articleHolder');
    var anchorColorInText = 'black-text';
    var anchorColorInLi = 'grey-text';
    var anchorMaxLen = 10;

    addMyAnchorClass($articleHolder, 'h3');
    addMyAnchorClass($articleHolder, 'h4');
    addMyAnchorClass($articleHolder, 'h5');

    $articleHolder.find('.myAnchor').each(
        function (index) {
            var fullText = $(this).text();
            $(this).text('').append($('<a></a>')
                .attr('id', 'myAnchor' + index)
                .attr('name', 'myAnchor' + index)
                .text(fullText).addClass('scrollspy'));
        }
    );
    if ($articleHolder.find('a').length > 0) {
        var $anchorHolder = $("#anchorHolder");
        var $window = $(window),
            offset = $anchorHolder.offset(),
            topPadding = $(window).height() / 4;
        $anchorHolder.css('padding-top', topPadding);

        $articleHolder.find('a').each(function () {
            var parentTag = $(this).parent().get(0).tagName;
            if (!parentTag.startsWith('H')) return;

            var fullText = $(this).text();
            var truncateText = fullText.length > anchorMaxLen
                ? fullText.substring(0, anchorMaxLen - 1) + '...'
                : fullText;

            var leftPadding = '';
            if (parentTag === 'H4') {
                truncateText = ' > ' + truncateText;
                leftPadding = '1em';
            }
            else if (parentTag === 'H5') {
                truncateText = ' >> ' + truncateText;
                leftPadding = '2em';
            }
            else {
                leftPadding = '0em';
            }

            $(this).addClass(anchorColorInText);
            $('#anchorHolder').append(
                $('<li></li>').append(
                    $('<a></a>').addClass(anchorColorInLi)
                        .text(truncateText)
                        .attr('title', fullText)
                        .css('padding-left', leftPadding)
                        .attr('href', '#' + $(this).attr('id')))
            );
        });

        $(window).resize(function () {
            var newPadding = $(window).height() / 4;
            $anchorHolder.css('padding-top', newPadding);
        });

        $(function () {
            $window.scroll(function () {
                if ($window.scrollTop() > offset.top) {
                    $anchorHolder.stop().animate({
                        marginTop: $window.scrollTop() - offset.top
                    });
                } else {
                    $anchorHolder.stop().animate({
                        marginTop: 0
                    });
                }
            });
        });
    }

    $('.scrollspy').scrollSpy();

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

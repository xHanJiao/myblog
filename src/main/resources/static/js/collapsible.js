function triggerLi(idx) {
    $('.collapsible-header').each(function (index) {
        if (index === idx) {
            $(this).trigger('click');
        }
    });
}

$(document).ready(function () {
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
        },
        onClose: function (el) {
            $(el).find('.createTimeHolder').hide();
        }
    });

    var $actDiv = $('.collapsible-header'),
        totalLiNum = $actDiv.length,
        initIdx = Math.floor(totalLiNum / 2) - 1 > 0 ? Math.floor(totalLiNum / 2) - 1 : 0;
    // initIdx = 0;
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
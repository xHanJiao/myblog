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
        },
        onClose: function (el) {
            $(el).find('.createTimeHolder').hide();
        }
    });
    var $actDiv = $('.collapsible-header'),
        totalLiNum = $actDiv.length,
        initIdx = Math.floor(totalLiNum / 2) - 1 > 0 ? Math.floor(totalLiNum / 2) - 1 : 0;
    triggerLi(initIdx);
    $actDiv.addClass(bodyColor);
});
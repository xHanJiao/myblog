$('.up-btn').click(function () {
    $("html,body").animate({scrollTop: 0}, 100);
    return false;
});

$('.down-btn').click(function () {
    // var h = $(document).height()-$(window).height();
    // $(document).scrollTop(h);
    $('html, body').animate({scrollTop: $(document).height()}, 500);
    return false;
});

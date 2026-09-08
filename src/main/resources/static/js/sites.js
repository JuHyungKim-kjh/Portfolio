/**
 * 바로가기 줄이 실제로 넘칠 때만 오른쪽 끝 그러데이션을 켠다.
 * CSS 로는 넘쳤는지 알 수 없어서, 그 한 가지만 여기서 판단한다.
 */
(function () {
    const wrap = document.querySelector('.sites-wrap');
    if (!wrap) return;
    const strip = wrap.querySelector('.sites');
    const sync = () => wrap.toggleAttribute('data-over', strip.scrollWidth > strip.clientWidth + 2);
    sync();
    window.addEventListener('resize', sync);
})();

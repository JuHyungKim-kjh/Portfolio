/* 프로젝트 화면 슬라이더 — 화살표 · 도트 · 좌우 방향키 · 스와이프 */
(function () {
  "use strict";

  var pad = function (n) { return String(n).padStart(2, "0"); };

  function initSlider(root) {
    var track = root.querySelector("[data-track]");
    var slides = Array.prototype.slice.call(root.querySelectorAll("[data-slide]"));
    if (!track || slides.length === 0) return;

    var dots = root.querySelector("[data-dots]");
    var counter = root.querySelector("[data-count]");
    var index = 0;

    if (dots) {
      dots.innerHTML = slides.map(function (_, n) {
        return '<button type="button" class="dot" data-go="' + n + '" aria-label="' + (n + 1) + '번째 화면"></button>';
      }).join("");
    }

    function go(n) {
      index = (n + slides.length) % slides.length;
      track.style.transform = "translateX(" + (-index * 100) + "%)";
      slides.forEach(function (slide, i) {
        var hidden = i !== index;
        slide.setAttribute("aria-hidden", String(hidden));
        // 보이지 않는 슬라이드 안의 링크로 탭 포커스가 새지 않게 한다
        slide.inert = hidden;
      });
      if (dots) {
        Array.prototype.forEach.call(dots.children, function (dot, i) {
          dot.classList.toggle("is-on", i === index);
        });
      }
      if (counter) counter.textContent = pad(index + 1) + " / " + pad(slides.length);
    }

    root.addEventListener("click", function (e) {
      var button = e.target.closest("[data-prev],[data-next],[data-go]");
      if (!button || !root.contains(button)) return;
      if (button.hasAttribute("data-go")) go(Number(button.dataset.go));
      else go(index + (button.hasAttribute("data-next") ? 1 : -1));
    });

    root.addEventListener("keydown", function (e) {
      if (e.key === "ArrowRight") { go(index + 1); e.preventDefault(); }
      if (e.key === "ArrowLeft") { go(index - 1); e.preventDefault(); }
    });

    var startX = null;
    root.addEventListener("touchstart", function (e) { startX = e.touches[0].clientX; }, { passive: true });
    root.addEventListener("touchend", function (e) {
      if (startX === null) return;
      var dx = e.changedTouches[0].clientX - startX;
      if (Math.abs(dx) > 45) go(index + (dx < 0 ? 1 : -1));
      startX = null;
    }, { passive: true });

    go(0);
  }

  /* 메인에서 보고 있는 구역에 맞춰 좌측 레일을 표시한다 */
  function initScrollSpy() {
    var scroller = document.getElementById("app");
    var sections = document.querySelectorAll("#app .sec[data-section]");
    var links = document.querySelectorAll(".rail nav a[data-section]");
    if (!scroller || sections.length === 0 || !("IntersectionObserver" in window)) return;

    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        var name = entry.target.dataset.section;
        links.forEach(function (link) {
          link.classList.toggle("on", link.dataset.section === name);
        });
      });
    }, { root: scroller, threshold: 0.55 });

    sections.forEach(function (section) { observer.observe(section); });
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-slider]").forEach(initSlider);
    initScrollSpy();
  });
})();

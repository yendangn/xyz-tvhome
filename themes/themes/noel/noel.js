// Noel only: the countdown card, in whole days to the next 25 December (a greeting on the day itself), and the
// twinkling lights.
(function () {
  var WEEK = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'], timer = 0;
  function set(id, text) { var e = document.getElementById(id); if (e) e.textContent = text; }

  function draw() {
    var now = new Date(), y = now.getFullYear();
    var today = new Date(y, now.getMonth(), now.getDate()), xmas = new Date(y, 11, 25);
    if (today > xmas) xmas = new Date(y + 1, 11, 25);
    var days = Math.round((xmas - today) / 86400000);
    set('xmasName', 'Giáng sinh ' + xmas.getFullYear());
    set('xmasDate', WEEK[xmas.getDay()] + ', 25 tháng 12, ' + xmas.getFullYear());
    set('xmasDays', days ? days : '');
    set('xmasLabel', days ? 'ngày nữa là Giáng sinh' : 'Giáng sinh vui vẻ!');
    // Again just after midnight, when the count drops.
    clearTimeout(timer);
    timer = setTimeout(draw, new Date(y, now.getMonth(), now.getDate() + 1) - now + 1000);
  }

  // The lights: every 0.8 s each group (b0–b2) moves on to its next state (s0 bright, s1 dim, s2 half), the groups a
  // state apart. A timer, not a CSS animation: between turns nothing runs (see style.css). Stops while Home is hidden.
  var bulbs = [].slice.call(document.querySelectorAll('.bulbs .l')), turn = 0;
  function twinkle() {
    turn = (turn + 1) % 3;
    for (var i = 0; i < bulbs.length; i++) {
      var g = +bulbs[i].className.match(/\bb(\d)/)[1];
      bulbs[i].className = bulbs[i].className.replace(/ s\d|$/, ' s' + (turn + g) % 3);
    }
  }
  twinkle();
  setInterval(twinkle, 800);

  // The app calls refresh() each time Home comes back; timers stood still while it was hidden.
  var refresh = window.refresh;
  window.refresh = function () { if (refresh) refresh(); draw(); };
  draw();
})();

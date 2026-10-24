// Noel lò sưởi only: the countdown card, in whole days to the next 25 December (a greeting on the day itself), the
// fire's flicker. (The snow falls by itself: style.css.)
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

  // The flicker: every 250 ms each flame takes the next shape of its cycle (the outer and inner ones the design's
  // flameA, a0–a3, the inner one backwards; the middle one flameB, b0–b2) and the glow the next brightness (g0–g2).
  // A timer, not a CSS animation: between turns nothing runs (see style.css). Stops while Home is hidden.
  var f = document.querySelectorAll('.fire .fl'), glow = document.querySelector('.fire .glow'), turn = 0;
  function flicker() {
    turn = (turn + 1) % 12;
    f[0].setAttribute('class', 'fl f1 a' + turn % 4);
    f[1].setAttribute('class', 'fl f2 b' + turn % 3);
    f[2].setAttribute('class', 'fl f3 a' + (4 - turn % 4) % 4);
    glow.className = 'glow g' + turn % 3;
  }
  if (f.length == 3 && glow) setInterval(flicker, 250);

  // The app calls refresh() each time Home comes back; timers stood still while it was hidden.
  var refresh = window.refresh;
  window.refresh = function () { if (refresh) refresh(); draw(); };
  draw();
})();

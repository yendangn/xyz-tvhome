// Đồng hồ kim only: draws the dial's ticks once, then turns the hands every second.
(function () {
  var NS = 'http://www.w3.org/2000/svg', R = 300, C = 310;
  var ticks = document.getElementById('ticks');
  for (var i = 0; i < 60; i++) {
    var big = i % 5 == 0, a = i * Math.PI / 30, r0 = big ? R - 34 : R - 12;
    var l = document.createElementNS(NS, 'line');
    l.setAttribute('class', big ? 'h' : 'm');
    l.setAttribute('x1', (C + r0 * Math.sin(a)).toFixed(1));
    l.setAttribute('y1', (C - r0 * Math.cos(a)).toFixed(1));
    l.setAttribute('x2', (C + R * Math.sin(a)).toFixed(1));
    l.setAttribute('y2', (C - R * Math.cos(a)).toFixed(1));
    ticks.appendChild(l);
  }

  // A timer on the second boundary, not an endless CSS animation: the second hand glides for a quarter of a second
  // (style.css), and between glides the page is idle. Only transforms change.
  var dial = document.querySelector('.dial');
  var h = document.getElementById('handH'), m = document.getElementById('handM'), s = document.getElementById('handS');
  var timer = 0;
  // Each hand only ever turns forward: its angle keeps counting past 360, so 59 s to 0 s is one more step clockwise,
  // not a glide back round the dial.
  function turn(el, deg) {
    var last = +el.getAttribute('data-deg') || 0;
    var next = last + ((deg - last) % 360 + 360) % 360;
    el.setAttribute('data-deg', next);
    el.style.transform = 'rotate(' + next.toFixed(2) + 'deg)';
  }
  function draw() {
    var now = new Date(), sec = now.getSeconds(), min = now.getMinutes() + sec / 60;
    turn(h, (now.getHours() % 12 + min / 60) * 30);
    turn(m, min * 6);
    turn(s, sec * 6);
    clearTimeout(timer);
    timer = setTimeout(draw, 1000 - new Date().getMilliseconds());
  }
  // Puts the hands in place at once, with no glide: after a while away they may have far to go.
  function jump() {
    dial.classList.add('still');
    draw();
    void dial.offsetWidth;   // apply the new angles before the glide comes back
    dial.classList.remove('still');
  }

  // The app calls refresh() each time Home comes back; timers stood still while it was hidden.
  var refresh = window.refresh;
  window.refresh = function () { if (refresh) refresh(); jump(); };
  jump();
})();

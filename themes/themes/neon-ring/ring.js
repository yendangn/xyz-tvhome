// Neon Ring only: the ring around the clock fills with the day (minutes gone of 24 hours), and so does "…% ngày đã qua".
(function () {
  var C = 2 * Math.PI * 220;
  var ring = document.getElementById('dayRing'), pct = document.getElementById('dayPct'), timer = 0;

  function draw() {
    var now = new Date(), f = (now.getHours() * 60 + now.getMinutes()) / 1440;
    if (ring) ring.setAttribute('stroke-dasharray', (C * f).toFixed(1) + ' ' + C.toFixed(1));
    if (pct) pct.textContent = Math.round(f * 100);
    clearTimeout(timer);
    timer = setTimeout(draw, 60000 - now.getSeconds() * 1000 - now.getMilliseconds());
  }

  // The app calls refresh() each time Home comes back; timers stood still while it was hidden.
  var refresh = window.refresh;
  window.refresh = function () { if (refresh) refresh(); draw(); };
  draw();
})();

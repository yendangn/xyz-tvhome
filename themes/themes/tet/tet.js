// Tết only: the next Lunar New Year (mùng 1 tháng Giêng, from core.js's window.lunar), its year's name and date, and
// the days left; on the day itself, a greeting. Redrawn just after midnight and whenever Home comes back.
(function () {
  var WEEK = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
  var card = document.getElementById('tet'), timer = 0;
  function set(id, text) { var e = document.getElementById(id); if (e) e.textContent = text; }

  function draw() {
    var now = new Date();
    for (var i = 0; i < 400; i++) {
      var d = new Date(now.getFullYear(), now.getMonth(), now.getDate() + i);
      var l = window.lunar.of(d.getDate(), d.getMonth() + 1, d.getFullYear());
      if (l.d != 1 || l.m != 1 || l.leap) continue;
      set('tetName', 'Tết ' + window.lunar.canChi(l.y));
      set('tetDate', WEEK[d.getDay()] + ', ' + d.getDate() + ' tháng ' + (d.getMonth() + 1) + ', ' + d.getFullYear() + ' · mùng 1 tháng Giêng');
      set('tetDays', i ? i : '');
      set('tetLabel', i ? 'ngày nữa là Tết' : 'Chúc mừng năm mới!');
      if (card) card.setAttribute('data-chi', window.lunar.chi(l.y));
      break;
    }
    clearTimeout(timer);
    timer = setTimeout(draw, new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1) - now + 1000);
  }

  // The app calls refresh() each time Home comes back; timers stood still while it was hidden.
  var refresh = window.refresh;
  window.refresh = function () { if (refresh) refresh(); draw(); };
  draw();
})();

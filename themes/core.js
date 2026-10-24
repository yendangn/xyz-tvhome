// Shared by every theme page: the lunar calendar, the clock and dates, prices, the network icon and the remote.
// (The theme picker is native: ThemePanel.kt.) A theme page only lays out elements with the ids and data attributes
// below; anything a theme leaves out is skipped.
//
//   Text:  clock, hello, weekday, weekdayUpper, dayNum, monthNum, monthName, monthYear, monthYearUpper, dateLong, dateText,
//          lunarLong, lunarShort, lunarSlash, ccYear ("Năm Bính Ngọ"), ccYearName ("Bính Ngọ"), ccMonth, ccDay,
//          calTitle, calSpan, calGrid (cells: .cell.sun/.special/.today with <b>day</b><i>lunar</i>), cal (.six),
//          sjc, ring, sjcBuy, ringBuy, e10, e5, do, priceTime
//   Focus: [data-nav="top"] and [data-nav="dock"]; data-action="wifi" | "themes" | "music" | "apps" | "settings", or
//          data-pkg. The focused element gets the class "focus"; the Wi-Fi one gets "off" while offline, the music one
//          (for a theme with "music" in theme.json) while the music is off.
//   For a theme's own script: window.lunar.of(day, month, year) gives that day's lunar date {d, m, y, leap}, and
//          window.lunar.canChi(lunarYear) its year's name ("Đinh Mùi"), split as {can, chi} by lunar.can/chi.
//   The app: window.TV is what a page can call (app/src/main/java/com/xyz/tvhome/web/TvApi.kt; not there when the page
//          is opened in a browser), and the app calls window.nav, refresh, onPrices and onNetwork below (PageApi.kt).
//          A theme's script wraps those rather than replacing them; examples: README "Dùng bridge trong JavaScript"
//          (README.en.md "Using the bridge from JavaScript").
(function () {
  // ---- Vietnamese lunar calendar (UTC+7), Ho Ngoc Duc's algorithm ----
  var TZ = 7;
  function INT(d) { return Math.floor(d); }
  function jdFromDate(dd, mm, yy) {
    var a = INT((14 - mm) / 12), y = yy + 4800 - a, m = mm + 12 * a - 3;
    var jd = dd + INT((153 * m + 2) / 5) + 365 * y + INT(y / 4) - INT(y / 100) + INT(y / 400) - 32045;
    if (jd < 2299161) jd = dd + INT((153 * m + 2) / 5) + 365 * y + INT(y / 4) - 32083;
    return jd;
  }
  function newMoon(k) {
    var T = k / 1236.85, T2 = T * T, T3 = T2 * T, dr = Math.PI / 180;
    var Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
    Jd1 += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
    var M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
    var Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
    var F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
    var C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
    C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr) - 0.0004 * Math.sin(dr * 3 * Mpr);
    C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
    C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
    C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
    C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
    var deltat = T < -11 ? 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3
                         : -0.000278 + 0.000265 * T + 0.000262 * T2;
    return Jd1 + C1 - deltat;
  }
  function newMoonDay(k) { return INT(newMoon(k) + 0.5 + TZ / 24); }
  function sunLong(jdn) {
    var T = (jdn - 2451545.5 - TZ / 24) / 36525, T2 = T * T, dr = Math.PI / 180;
    var M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
    var L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
    var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
    DL += (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
    var L = (L0 + DL) * dr;
    L = L - Math.PI * 2 * INT(L / (Math.PI * 2));
    return INT(L / Math.PI * 6);
  }
  function month11(yy) {
    var k = INT((jdFromDate(31, 12, yy) - 2415021) / 29.530588853), nm = newMoonDay(k);
    return sunLong(nm) >= 9 ? newMoonDay(k - 1) : nm;
  }
  function leapOffset(a11) {
    var k = INT((a11 - 2415021.076998695) / 29.530588853 + 0.5), last, i = 1, arc = sunLong(newMoonDay(k + i));
    do { last = arc; i++; arc = sunLong(newMoonDay(k + i)); } while (arc != last && i < 14);
    return i - 1;
  }
  function lunarOf(dd, mm, yy) {
    var dn = jdFromDate(dd, mm, yy), k = INT((dn - 2415021.076998695) / 29.530588853);
    var start = newMoonDay(k + 1); if (start > dn) start = newMoonDay(k);
    var a11 = month11(yy), b11 = a11, ly;
    if (a11 >= start) { ly = yy; a11 = month11(yy - 1); } else { ly = yy + 1; b11 = month11(yy + 1); }
    var day = dn - start + 1, diff = INT((start - a11) / 29), leap = 0, mon = diff + 11;
    if (b11 - a11 > 365) { var ld = leapOffset(a11); if (diff >= ld) { mon = diff + 10; if (diff == ld) leap = 1; } }
    if (mon > 12) mon -= 12;
    if (mon >= 11 && diff < 4) ly -= 1;
    return { d: day, m: mon, y: ly, leap: leap, jd: dn };
  }
  var CAN = ['Giáp','Ất','Bính','Đinh','Mậu','Kỷ','Canh','Tân','Nhâm','Quý'];
  var CHI = ['Tý','Sửu','Dần','Mão','Thìn','Tỵ','Ngọ','Mùi','Thân','Dậu','Tuất','Hợi'];
  var WEEK = ['Chủ Nhật','Thứ Hai','Thứ Ba','Thứ Tư','Thứ Năm','Thứ Sáu','Thứ Bảy'];
  var MONTH = ['Một','Hai','Ba','Tư','Năm','Sáu','Bảy','Tám','Chín','Mười','Mười Một','Mười Hai'];
  function mName(l) { return l.m + (l.leap ? ' nhuận' : ''); }
  function pad(n) { return ('0' + n).slice(-2); }

  // ---- Drawing ----
  function $(id) { return document.getElementById(id); }
  function set(id, text) { var e = $(id); if (e) e.textContent = text; }
  var shownDay = '';

  function drawDay(now) {
    var d = now.getDate(), m = now.getMonth() + 1, y = now.getFullYear(), l = lunarOf(d, m, y);
    set('weekday', WEEK[now.getDay()]);
    set('weekdayUpper', WEEK[now.getDay()].toUpperCase());
    set('dayNum', d);
    set('monthNum', m);
    set('monthName', 'Tháng ' + MONTH[m - 1]);
    set('monthYearUpper', ('Tháng ' + MONTH[m - 1] + ' · ' + y).toUpperCase());
    set('dateLong', WEEK[now.getDay()] + ', ' + pad(d) + '/' + pad(m) + '/' + y);
    set('dateText', d + ' tháng ' + m + ', ' + y);
    set('monthYear', 'tháng ' + m + ', ' + y);
    set('lunarLong', 'Âm lịch ' + l.d + ' tháng ' + mName(l));
    set('lunarShort', l.d + '/' + mName(l));
    set('lunarSlash', l.d + ' / ' + mName(l));
    set('ccYear', 'Năm ' + CAN[(l.y + 6) % 10] + ' ' + CHI[(l.y + 8) % 12]);
    set('ccYearName', CAN[(l.y + 6) % 10] + ' ' + CHI[(l.y + 8) % 12]);
    set('ccMonth', 'Tháng ' + CAN[(l.y * 12 + l.m + 3) % 10] + ' ' + CHI[(l.m + 1) % 12]);
    set('ccDay', 'Ngày ' + CAN[(l.jd + 9) % 10] + ' ' + CHI[(l.jd + 1) % 12]);

    var grid = $('calGrid');
    if (!grid) return;
    var first = new Date(y, m - 1, 1), days = new Date(y, m, 0).getDate(), off = (first.getDay() + 6) % 7;
    var a = lunarOf(1, m, y), b = lunarOf(days, m, y);
    set('calTitle', 'Tháng ' + m);
    set('calSpan', 'âm lịch tháng ' + mName(a) + (a.m == b.m && a.leap == b.leap ? '' : ' và ' + mName(b)));
    var html = '';
    for (var i = 0; i < off; i++) html += '<div class="cell"></div>';
    for (var dd = 1; dd <= days; dd++) {
      var ld = lunarOf(dd, m, y), col = (off + dd - 1) % 7, cls = 'cell';
      if (col == 6) cls += ' sun';
      if (ld.d == 1 || ld.d == 15) cls += ' special';
      if (dd == d) cls += ' today';
      html += '<div class="' + cls + '"><b>' + dd + '</b><i>' + (ld.d == 1 ? '1/' + ld.m : ld.d) + '</i></div>';
    }
    grid.innerHTML = html;
    if ($('cal')) $('cal').classList.toggle('six', off + days > 35);
  }

  // The clock's nodes are made once; each tick only changes their text, and hours:minutes only once a minute.
  var clockHM = null, clockSec = null;
  function tick() {
    var now = new Date();
    var clock = $('clock');
    if (clock) {
      if (!clockSec) {
        clock.textContent = '';
        clockHM = clock.appendChild(document.createTextNode(''));
        clockSec = clock.appendChild(document.createElement('span'));
        clockSec.className = 'sec';
      }
      var hm = pad(now.getHours()) + ':' + pad(now.getMinutes());
      if (clockHM.data != hm) clockHM.data = hm;
      clockSec.textContent = ':' + pad(now.getSeconds());
    }
    var key = now.toDateString();
    if (key != shownDay) { shownDay = key; drawDay(now); }
  }

  // TV.prices() is the app's PriceBoard (prices/PriceBoard.kt): {sjc, ring: {buy, sell, t}, e10, e5, do: {price, t}},
  // gold in đồng per lượng, fuel in đồng per litre, t when it was read; a product missing has never been read.
  // Gold is shown in million đồng per lượng (142000000 -> "142,0"), fuel with dots (29050 -> "29.050").
  function gold(v) { return v ? (v / 1e6).toFixed(1).replace('.', ',') : '–'; }
  function litre(v) { return v ? String(v).replace(/\B(?=(\d{3})+$)/g, '.') : '–'; }
  // Never pass an old price off as today's: one not read for 3 hours is dimmed, and gold older than a day or fuel
  // older than 8 days (prices change about weekly) is hidden.
  var HOUR = 3600000, DIM = 3 * HOUR, GOLD_GONE = 24 * HOUR, FUEL_GONE = 8 * 24 * HOUR;
  function price(id, text, t, gone) {
    var e = $(id);
    if (!e) return;
    var age = t ? Date.now() - t : Infinity;
    e.textContent = age > gone ? '–' : text;
    e.style.opacity = age > DIM && age <= gone ? '.4' : '';
  }
  function onPrices() {
    if (!window.TV) return;
    var p = JSON.parse(TV.prices());
    var sjc = p.sjc || {}, ring = p.ring || {}, e10 = p.e10 || {}, e5 = p.e5 || {}, dsl = p['do'] || {};
    price('sjc', gold(sjc.sell), sjc.t, GOLD_GONE);
    price('ring', gold(ring.sell), ring.t, GOLD_GONE);
    price('sjcBuy', gold(sjc.buy), sjc.t, GOLD_GONE);
    price('ringBuy', gold(ring.buy), ring.t, GOLD_GONE);
    price('e10', litre(e10.price), e10.t, FUEL_GONE);
    price('e5', litre(e5.price), e5.t, FUEL_GONE);
    price('do', litre(dsl.price), dsl.t, FUEL_GONE);
    // When gold was last read; with the date once that is not today.
    var last = Math.max(sjc.t || 0, ring.t || 0);
    if (last) {
      var d = new Date(last), now = new Date();
      set('priceTime', 'cập nhật ' + (d.toDateString() == now.toDateString() ? '' : pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + ' ') +
        pad(d.getHours()) + ':' + pad(d.getMinutes()));
    } else set('priceTime', '');
  }
  function net() {
    var w = document.querySelector('[data-action="wifi"]');
    if (w && window.TV) w.classList.toggle('off', !TV.online());
  }
  function sound() {
    var m = document.querySelector('[data-action="music"]');
    if (m && window.TV && TV.musicOn) m.classList.toggle('off', !TV.musicOn());
  }
  // On every return to Home; tick() redraws the day only when the date has changed. The focus goes back to YouTube.
  function refresh() { tick(); onPrices(); net(); sound(); home(); }

  // ---- Remote: Java passes the D-pad as nav('left' | 'right' | 'up' | 'down' | 'ok' | 'back') ----
  var top = [].slice.call(document.querySelectorAll('[data-nav="top"]'));
  var dock = [].slice.call(document.querySelectorAll('[data-nav="dock"]'));
  var row = 'dock', di = 0, ti = 0;

  // The default focus: YouTube's tile, or the first in the dock when a theme has none.
  function home() {
    row = 'dock';
    di = Math.max(0, dock.findIndex(function (el) { return el.getAttribute('data-pkg') == 'com.google.android.youtube.tv'; }));
    paint();
  }
  function paint() {
    for (var i = 0; i < dock.length; i++) dock[i].classList.toggle('focus', row == 'dock' && i == di);
    for (var j = 0; j < top.length; j++) top[j].classList.toggle('focus', row == 'top' && j == ti);
  }
  function activate(el) {
    var a = el.getAttribute('data-action'), pkg = el.getAttribute('data-pkg');
    // The theme and app panels are native; while one is open, Kotlin keeps the remote keys to itself.
    if (a == 'themes') TV.openThemes();
    else if (a == 'apps') TV.openApps();
    else if (a == 'music') el.classList.toggle('off', !TV.toggleMusic());
    else if (a == 'wifi') TV.openSettings('wifi');
    else if (a == 'settings') TV.openSettings('all');
    else if (pkg) TV.launch(pkg);
  }
  function nav(dir) {
    if (dir == 'back') return;
    if (dir == 'ok') { activate(row == 'dock' ? dock[di] : top[ti]); return; }
    if (row == 'dock') {
      if (dir == 'left') di = Math.max(0, di - 1);
      else if (dir == 'right') di = Math.min(dock.length - 1, di + 1);
      else if (dir == 'up' && top.length) { row = 'top'; }
    } else {
      if (dir == 'left') ti = Math.max(0, ti - 1);
      else if (dir == 'right') ti = Math.min(top.length - 1, ti + 1);
      else if (dir == 'down') row = 'dock';
    }
    paint();
  }

  // ---- Touch, for phones and tablets: a tap moves the focus there, then acts as OK ----
  document.addEventListener('click', function (ev) {
    var el = ev.target.closest('[data-nav]');
    if (!el) return;
    var t = top.indexOf(el), d = dock.indexOf(el);
    if (t >= 0) { row = 'top'; ti = t; } else if (d >= 0) { row = 'dock'; di = d; } else return;
    paint();
    activate(el);
  });

  function fit() {
    var s = Math.min(window.innerWidth / 1920, window.innerHeight / 1080), st = $('stage');
    if (st) st.style.transform = 'scale(' + s + ')';
  }

  window.nav = nav;
  window.lunar = {
    of: lunarOf,
    can: function (y) { return CAN[(y + 6) % 10]; },
    chi: function (y) { return CHI[(y + 8) % 12]; },
    canChi: function (y) { return CAN[(y + 6) % 10] + ' ' + CHI[(y + 8) % 12]; }
  };
  window.refresh = refresh;
  window.onPrices = onPrices;
  // The app calls it when the network comes or goes while Home is shown.
  window.onNetwork = net;
  window.addEventListener('resize', fit);
  window.addEventListener('load', fit);
  setTimeout(fit, 300);
  fit(); refresh();
  // Tick on the second boundary, so the seconds change in step with the real clock.
  setTimeout(function () { tick(); setInterval(tick, 1000); }, 1000 - new Date().getMilliseconds());
  setInterval(net, 30000);
})();

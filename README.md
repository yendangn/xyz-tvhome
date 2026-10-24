# TV Home

**Tiếng Việt** · [English](README.en.md)

> **⚠️ Cảnh báo**
>
> Đây là dự án cá nhân, mang tính giải trí. Tác giả không chịu trách nhiệm nếu quá trình cài đặt hay
> sử dụng gây ảnh hưởng tới TV (lỗi phần mềm, mất màn hình chính, mất dữ liệu, hỏng thiết bị…). Người dùng cần đọc
> kỹ hướng dẫn, hiểu rõ từng bước và chấp nhận mọi rủi ro nếu có.
>
> **Rủi ro bảo mật:**
>
> - **Chế độ nhà phát triển (Dev mode) đang bật.** Khi Gỡ lỗi USB và Gỡ lỗi qua mạng còn bật, máy khác trong cùng
>   mạng Wi-Fi có thể kết nối `adb` vào TV. Cài xong nên tắt Dev mode (bước 5.5).
> - **WebView đang bật JavaScript.** Code JavaScript của theme gọi được vào app: mở ứng dụng, mở Cài đặt, bật nhạc.
>   App chỉ nạp file nằm trong app và chặn mọi trang web bên ngoài. Dù vậy, mọi đoạn JS thêm vào theme vẫn cần được
>   review kỹ; không chép JS từ nguồn không rõ ràng.

## Mục lục

- [Các theme](#các-theme)
- [TV Home là gì?](#tv-home-là-gì)
- [Chạy như thế nào?](#chạy-như-thế-nào)
- [Làm từng bước](#làm-từng-bước)
  - [1. Set up dự án](#1-set-up-dự-án)
  - [2. Set up TV](#2-set-up-tv)
  - [3. Thêm theme](#3-thêm-theme)
  - [4. Build](#4-build)
  - [5. Install](#5-install)
  - [Kiểm tra trạng thái](#kiểm-tra-trạng-thái)
  - [Khi gặp lỗi](#khi-gặp-lỗi)
- [Dành cho developer](#dành-cho-developer)
  - [Nguồn giá](#vi-prices)
  - [Nhạc theme](#vi-music)
  - [Bridge: trang ↔ app](#vi-bridge)
    - [Chiều 1: trang gọi app](#chiều-1-trang-gọi-app-windowtv)
    - [Chiều 2: app gọi trang](#chiều-2-app-gọi-trang)

## Các theme

| | |
|---|---|
| ![Retro Việt Nam](docs/screenshots/retro.webp) **Retro Việt Nam** (`retro`) | ![Bìa tạp chí](docs/screenshots/magazine.webp) **Bìa tạp chí** (`magazine`) |
| ![Risograph](docs/screenshots/riso.webp) **Risograph** (`riso`) | ![Pastel tươi vui](docs/screenshots/pastel.webp) **Pastel tươi vui** (`pastel`) |
| ![Hiện đại tối giản](docs/screenshots/modern.webp) **Hiện đại tối giản** (`modern`) | ![Hiện đại sáng](docs/screenshots/modern-light.webp) **Hiện đại sáng** (`modern-light`) |
| ![Liquid Glass](docs/screenshots/liquid.webp) **Liquid Glass** (`liquid`) | ![Neon Ring](docs/screenshots/neon-ring.webp) **Neon Ring** (`neon-ring`) |
| ![Noel](docs/screenshots/noel.webp) **Noel** (`noel`) | ![Noel lò sưởi](docs/screenshots/noel-fireplace.webp) **Noel lò sưởi** (`noel-fireplace`) |
| ![Tết](docs/screenshots/tet.webp) **Tết** (`tet`) | |

Ảnh chụp trên TV VinSmart 4K (1920×1080) ngày 23/10/2026.

## TV Home là gì?

TV Home là màn hình chính làm riêng cho Android TV, dùng thay màn hình mặc định của Google. Mở TV lên sẽ có:

- Lời chào kèm tên chủ nhà, đồng hồ và ngày tháng.
- Lịch âm và lịch tháng.
- Giá vàng và giá xăng dầu, cập nhật mỗi giờ, khi app có nguồn giá. Mặc định chưa có: các ô giá hiện "–" cho tới khi
  bạn viết một nguồn giá (xem [Nguồn giá](#vi-prices)).
- Các ô lớn để mở YouTube, Play Store, Cài đặt và danh sách ứng dụng.
- Nút đổi theme. Riêng theme Noel còn có nhạc.

Hiện có sẵn 11 theme (ảnh ở trên), muốn thêm theme mới cũng được. App làm và chạy thử trên TV VinSmart 4K
(`VINATV_4K`, mã `TR2001`): Android 9, RAM 2 GB, màn hình 1920×1080, chip ARMv7.

## Chạy như thế nào?

Mỗi theme thực chất là một trang web nhỏ (HTML, CSS) nằm trong `themes/themes/<tên>/`. App Android trong `app/` mở
trang đó toàn màn hình và cho remote bấm qua lại giữa các ô.

Bạn build app trên máy tính (Mac hoặc Windows đều được), kết quả là một file APK. Sau đó dùng `adb` đẩy file này sang TV qua Wi-Fi, rồi đặt TV Home làm
màn hình chính.

Làm theme mới thì chỉ sửa trang web, không đụng gì tới code Android.

## Làm từng bước

Các lệnh bên dưới chạy trong **Terminal** trên Mac (bấm ⌘ Space, nhập "Terminal" rồi Enter) hoặc **PowerShell** trên
Windows (bấm phím Windows, nhập "PowerShell" rồi Enter). Lệnh nào khác nhau giữa Mac và Windows đều được ghi riêng.
Copy từng lệnh, dán vào rồi bấm Enter. Mỗi bước đều ghi kết quả đúng trông ra sao. Nếu kết quả khác, hãy dừng lại và hỏi trước khi làm tiếp.

**Nhanh nhất: dùng Claude Code.** Mở dự án trong Claude Code, chạy `/setup`. Claude kiểm tra máy còn thiếu gì
và cài thêm, chuẩn bị dự án, chỉ bạn bấm gì trên TV, rồi build, cài app và đặt TV Home làm màn hình chính luôn. Các bước
dưới đây là những việc Claude làm, dành cho khi bạn muốn chạy từng lệnh.

### 1. Set up dự án

Mỗi máy tính chỉ cần làm một lần.

1. **Cài các công cụ sau** (cài mặc định là được):

   | Công cụ | Tải ở đâu | Dùng để |
   |---|---|---|
   | Git | Mac: chạy `xcode-select --install` trong Terminal, bấm Install. Windows: https://git-scm.com/download/win | Tải code |
   | Java 17 | https://adoptium.net/temurin/releases/?version=17 (Mac: file .pkg. Windows: file .msi, khi cài nhớ chọn **Set JAVA_HOME variable**) | Build app |
   | Android Studio | https://developer.android.com/studio | Có sẵn Android SDK và `adb` |
   | Node.js 20 trở lên | https://nodejs.org (bản LTS) | Xử lý các theme |
   | Google Chrome | https://www.google.com/chrome | Chụp ảnh xem trước theme |

   Kiểm tra lại: `node -v` in ra `v20` trở lên. Trên Mac, `/usr/libexec/java_home -v 17` in ra đường dẫn Java 17;
   trên Windows, `java -version` in ra phiên bản 17.

2. **Mở Android Studio lần đầu**, bấm Next cho tới hết để Android Studio tải Android SDK về. Sau đó vào
   **Settings → Languages & Frameworks → Android SDK**, tab "SDK Platforms" phải tích **Android 14 (API 34)**; tab
   "SDK Tools" phải tích **Android SDK Build-Tools** và **Android SDK Platform-Tools**. Bấm OK.

3. **Tải code về:**

   ```bash
   cd ~
   git clone https://github.com/yendangn/xyz-tvhome.git
   cd xyz-tvhome
   ```

4. **Khai báo thư mục Android SDK và cài thư viện:**

   Mac:

   ```bash
   echo "sdk.dir=$HOME/Library/Android/sdk" > app/local.properties
   npm --prefix scripts install
   ```

   Windows:

   ```powershell
   "sdk.dir=$($env:LOCALAPPDATA -replace '\\','/')/Android/Sdk" | Out-File -Encoding ascii app/local.properties
   npm --prefix scripts install
   ```

   File `app/local.properties` chỉ dùng cho máy này. Git đã bỏ qua file này, đừng commit.

5. **Build thử:**

   ```bash
   # Mac
   JAVA_HOME=$(/usr/libexec/java_home -v 17) app/gradlew -p app assembleDebug
   # Windows
   .\app\gradlew.bat -p app assembleDebug
   ```

   Lần đầu chạy hơi lâu, khoảng vài phút. Cuối cùng in ra **`BUILD SUCCESSFUL`** là xong. Trên Mac, nếu Java mặc định
   đã là 17 thì bỏ phần `JAVA_HOME=…` cũng được.

> Lần sau mở Terminal (hay PowerShell) mới, nhớ chạy hai lệnh này trước. Lệnh thứ hai giúp dùng được `adb`.
>
> ```bash
> # Mac
> cd ~/xyz-tvhome
> export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
> # Windows
> cd ~\xyz-tvhome
> $env:Path += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"
> ```

### 2. Set up TV

Mỗi TV chỉ cần làm một lần.

1. **Bật chế độ nhà phát triển:** Trên TV, vào **Cài đặt → Tuỳ chọn thiết bị → Giới thiệu**, bấm vào dòng
   **Bản dựng** khoảng 7 lần, tới khi TV báo "Bạn đã là nhà phát triển".
2. **Bật gỡ lỗi:** Trở ra **Tuỳ chọn thiết bị → Tuỳ chọn nhà phát triển**, bật **Gỡ lỗi USB**. Có mục
   **Gỡ lỗi qua mạng** thì bật luôn.
3. **Lấy IP của TV:** Vào **Cài đặt → Mạng & Internet**, chọn mạng Wi-Fi đang dùng, ghi lại **địa chỉ IP**
   (ví dụ `192.168.1.39`). Trong các lệnh bên dưới, `<ip-tv>` nghĩa là IP này.
4. **Cho máy tính và TV dùng chung một mạng Wi-Fi.**
5. **Kết nối từ máy tính sang TV:**

   ```bash
   adb connect <ip-tv>:5555
   ```

   TV sẽ hỏi **"Cho phép gỡ lỗi USB?"**. Chọn **Luôn cho phép từ máy tính này** rồi bấm **OK**.

6. **Kiểm tra lại:**

   ```bash
   adb devices -l
   ```

   Kết nối thành công khi có một dòng như sau, ngay sau IP là chữ `device`:

   ```
   192.168.1.39:5555      device product:TR2001_Vsmart model:VINATV_4K device:TR2001
   ```

   Nếu ghi `unauthorized` tức là chưa bấm OK trên TV. Chạy `adb disconnect <ip-tv>:5555` rồi làm lại bước 5.

Nên vào router đặt IP cố định cho TV, để IP không bị đổi sau khi tắt mở TV.

Khi máy tính đang nối nhiều thiết bị cùng lúc (ví dụ thêm một điện thoại cắm USB), lệnh `adb` nào cũng phải có
`-s <ip-tv>:5555`. Các lệnh trong README đều ghi sẵn phần này.

### 3. Thêm theme

**Nhanh nhất:** Trong Claude Code, chạy `/new-theme` rồi đưa link thiết kế, ảnh hoặc mô tả bằng lời. Claude làm theme,
tạo ảnh xem trước và build thử.

**Chạy từng lệnh:**

Một theme là một thư mục:

```
themes/themes/<id>/
  index.html      trang giao diện
  style.css       CSS của theme
  theme.json      tên, vị trí và kiểu bảng chọn theme
  preview.webp    ảnh 768×432 hiện trong bảng chọn theme
  …               ảnh (WebP) và script riêng của theme
```

**3.1 Tạo thư mục.** Đặt id viết thường, không dấu, nối bằng `-` (ví dụ `tet-2027`), kèm một cái tên để hiện trên
TV. Xem các id đã dùng rồi tạo theme:

```bash
node scripts/build-web.mjs --list
node scripts/new-theme.mjs tet-2027 "Tết 2027"
```

Lệnh thứ hai chép theme "modern" ra thư mục mới, mở lên là chạy được ngay. File `style.css` của theme mới là bản chép
đầy đủ, sửa thoải mái. Đừng sửa `themes/shared/modern.css`: các theme khác đang dùng chung file này.

**3.2 Xem thử.** Kéo file `themes/themes/tet-2027/index.html` thả vào Chrome. Giá và Wi-Fi phải chạy trong app mới có.

**3.3 Sửa giao diện.** Sửa `index.html` và `style.css` theo thiết kế, giữ các quy tắc sau:

- `#stage` rộng 1920, cao 1080, có `position: absolute; transform-origin: 0 0`. `core.js` co giãn cho vừa màn
  hình. Dùng đơn vị px.
- Giữ đủ các nút mà remote đi qua:
  - Hàng trên (`data-nav="top"`): nút `data-action="themes"` là nút **duy nhất** mở được bảng chọn theme, nên bắt
    buộc phải có (thiếu nút này, build sẽ báo lỗi); nút `data-action="wifi"` được thêm class `off` khi mất mạng.
  - Hàng dưới (`data-nav="dock"`), đủ 4 ô: YouTube `data-pkg="com.google.android.youtube.tv"`, Play Store
    `data-pkg="com.android.vending"`, `data-action="settings"` và `data-action="apps"` ("Ứng dụng", mở danh sách
    ứng dụng của TV).
- Ô đang chọn có class `focus`. Định kiểu `.focus` giống ô được tô nổi trong thiết kế. Mở lên thì YouTube được chọn
  sẵn.
- Tên chủ nhà ghi thành `<span data-owner>Yên Đặng</span>`. Lúc build, tên này được thay bằng tên trong
  `themes/site.json`; tên để trống thì span bị bỏ, kèm luôn dấu phẩy phía trước.
- `<script src="../../core.js"></script>` đặt cuối `<body>`. Dùng đường dẫn tương đối: font là `../../fonts/…`, ảnh
  riêng của theme là `./…`.
- `core.js` điền nội dung theo id. Thiết kế có phần nào thì dùng id của phần đó (danh sách đầy đủ ở đầu `themes/core.js`):

  | Nhóm | Id |
  |---|---|
  | Giờ, ngày | `clock` (có thêm span `.sec` cho giây; chỉ muốn HH:MM thì thêm `.clock .sec { display: none; }`), `hello`, `weekday`, `dayNum`, `monthNum`, `monthName`, `dateText`, `dateLong` |
  | Âm lịch | `lunarLong`, `lunarShort`, `lunarSlash`, `ccYear`, `ccYearName`, `ccMonth`, `ccDay` |
  | Lịch tháng | `cal` (thêm class `.six` khi tháng có 6 hàng), `calTitle`, `calSpan`, `calGrid` (mỗi ô `.cell` có `<b>` ngày dương, `<i>` ngày âm; class `.sun`, `.special`, `.today`) |
  | Giá | `sjc`, `ring`, `sjcBuy`, `ringBuy`, `e10`, `e5`, `do`, `priceTime` |

- Script riêng của theme dùng được âm lịch: `window.lunar.of(ngày, tháng, năm)` và `window.lunar.canChi(nămÂm)`.
  Ví dụ trong `themes/themes/tet/tet.js`.
- Theme có nhạc: Thêm `"music": "<file>.m4a"` vào `theme.json`, đặt file nhạc cạnh `index.html`, và thêm nút
  `data-nav="top" data-action="music"`. Có nút mà thiếu nhạc, hoặc có nhạc mà thiếu nút, build đều báo lỗi. Nhạc chỉ
  phát khi bấm nút, và chỉ khi TV Home đang mở. Ví dụ: `themes/themes/noel/`.

**3.4 Giữ cho TV chạy mượt.** TV yếu, nên:

- Không dùng `backdrop-filter`, `filter: blur()`, hay CSS `animation` lặp mãi.
- Gradient tĩnh, bóng đổ cứng, hoạ tiết `radial-gradient` thì dùng thoải mái, vì chỉ vẽ một lần.
- Ảnh dùng WebP, tối đa 1920×1080, chất lượng 80.
- Hiệu ứng chạy liên tục (đèn nhấp nháy) thì dùng timer trong JS của theme (`setInterval`, 0,5–1 giây một lần) để
  đổi class. Phần thay đổi phải là thẻ HTML có `will-change: opacity`, và chỉ đổi `opacity` hoặc `transform`, không
  đổi phần tử con của SVG. Ví dụ `themes/themes/noel/noel.js`. Đo trên TV: 28 bóng đèn nhấp nháy theo kiểu này gần như
  không tốn CPU; cũng 28 bóng đó làm bằng SVG với CSS animation thì tốn khoảng 24 % một nhân CPU.

**3.5 Font.** Ưu tiên font có sẵn trong `themes/fonts/`. Cần font mới thì:

1. Tải font từ Google Fonts (`https://raw.githubusercontent.com/google/fonts/main/ofl/<family>/`), kèm file
   `OFL.txt`.
2. Cài công cụ: `python3 -m venv .venv && .venv/bin/pip install fonttools brotli`. Trên Windows dùng `python`
   thay cho `python3`, và `.venv\Scripts\` thay cho `.venv/bin/` ở các lệnh dưới.
3. Font variable thì cố định độ đậm đang dùng: `.venv/bin/fonttools varLib.instancer in.ttf wght=800 -o out.ttf`.
4. Chỉ giữ chữ Latin và tiếng Việt, xuất ra woff2:

   ```bash
   U="U+0000-00FF,U+0131,U+0152-0153,U+02BB-02BC,U+02C6,U+02DA,U+02DC,U+0300-0301,U+0303-0304,U+0308-0309,U+0323,U+0329,U+0102-0103,U+0110-0111,U+0128-0129,U+0168-0169,U+01A0-01A1,U+01AF-01B0,U+1EA0-1EF9,U+2000-206F,U+20AB,U+20AC,U+2122,U+2191,U+2193,U+2212,U+2215,U+FEFF,U+FFFD"
   .venv/bin/pyftsubset out.ttf --unicodes="$U" --layout-features='*' --flavor=woff2 --output-file=themes/fonts/<Family>-<Style>.woff2
   ```

5. Chép giấy phép thành `themes/fonts/<Family>-OFL.txt`. Khi build, giấy phép đi kèm font.

**3.6 Bảng chọn theme (`theme.json`).** Bảng chọn là giao diện Android native, mỗi theme một kiểu riêng:

```json
{
  "name": "Tết 2027",
  "order": 110,
  "panel": { "scrim": "rgba(0,0,0,.6)", "panel": "#FFF8EC", "ink": "#3A1A0A",
             "titleFont": "fonts/panel/oswald_bold.ttf", "nameFont": "fonts/panel/oswald_bold.ttf",
             "tagFont": "fonts/panel/bevietnampro_bold.ttf", "tag": "#C8102E",
             "sel": "#3A1A0A", "selBg": "#F2C230" }
}
```

- `order`: Vị trí trong bảng chọn, số nhỏ đứng trước. Theme mới tạo bằng `new-theme.mjs` nằm cuối.
- Trường bắt buộc trong `panel`: `scrim`, `panel`, `ink`, `titleFont`, `nameFont`, `tagFont`, `tag`, `sel`,
  `selBg`. Các trường tuỳ chọn và giá trị mặc định nằm trong
  `app/src/main/java/com/xyz/tvhome/theme/ThemeStyle.kt`; `themes/themes/retro/theme.json` dùng gần hết.
- Màu: `#RRGGBB`, `#AARRGGBB` hoặc `rgba(r,g,b,a)`. Kích thước: px trên khung 1920×1080.
- Font của bảng chọn phải là file TTF trong `themes/fonts/panel/` (Android không đọc được woff2).
- `footInk`: Màu dòng ghi nguồn giá ở đáy màn hình (mặc định theo `ink`). Dùng khi màu `ink` khó đọc trên nền phía dưới trang.
  Theme không hiện giá thì để `"showSource": false` để ẩn dòng đó.
- Tên theme nên ngắn, dài quá sẽ bị cắt thành "…".

**3.7 Tạo ảnh xem trước và build thử.**

```bash
node scripts/preview.mjs tet-2027
# Mac
JAVA_HOME=$(/usr/libexec/java_home -v 17) app/gradlew -p app assembleDebug -Pthemes=tet-2027
# Windows
.\app\gradlew.bat -p app assembleDebug -Pthemes=tet-2027
```

Lệnh thứ nhất chụp trang ở 1920×1080 bằng Chrome (với giá mẫu) rồi lưu `themes/themes/tet-2027/preview.webp`; mở ảnh ra so
lại với thiết kế. `node scripts/preview.mjs --all` chụp lại mọi theme. Nếu Chrome cài ở thư mục khác thường, đặt
`CHROME=/đường/dẫn/tới/chrome` (Mac) hoặc `$env:CHROME="C:\đường\dẫn\tới\chrome.exe"` (Windows).

Lệnh thứ hai build thử. Thiếu font, thiếu ảnh hay sai đường dẫn đều bị báo lỗi rõ ràng. In ra **`BUILD SUCCESSFUL`** là được.

### 4. Build

**Nhanh nhất:** Trong Claude Code, chạy `/build-release`. Claude sẽ hỏi muốn lấy những theme nào, lời chào ghi tên
ai, rồi build và cài lên TV luôn nếu bạn muốn.

**Chạy từng lệnh:**

1. **Đổi tên trong lời chào:** Mở `themes/site.json`, sửa thành tên bạn muốn, ví dụ `{ "owner": "Yên Đặng" }`. Để
   `""` thì lời chào chỉ còn "Xin chào".
2. **Build:**

   ```bash
   # Mac
   JAVA_HOME=$(/usr/libexec/java_home -v 17) app/gradlew -p app assembleRelease
   # Windows
   .\app\gradlew.bat -p app assembleRelease
   ```

   In ra **`BUILD SUCCESSFUL`** là đã có app, nằm ở **`app/build/outputs/apk/release/app-release.apk`**.

Có thể thêm các tuỳ chọn sau vào cuối lệnh build:

| Thêm | Để làm gì |
|---|---|
| `-Pthemes=retro,tet-2027` | Chỉ lấy các theme này (không ghi thì lấy hết) |
| `-Powner="Tên khác"` | Đổi tên trong lời chào, chỉ áp dụng cho lần build này |
| `-Pmusic=noop` | Không phát nhạc theme. Nút nhạc luôn tắt. |
| `-Pnode="$(command -v node)"` | Dùng khi Gradle báo không tìm ra `node` (hay gặp khi mở Android Studio từ Dock). |

Khi build, Gradle gọi `scripts/build-web.mjs`. Script này chỉ chép vào APK những file mà các theme được chọn cần dùng, bản
release thì nén luôn HTML, CSS, JS. Xem danh sách theme: `node scripts/build-web.mjs --list`.

### 5. Install

1. **Kết nối TV** (chưa làm bước 2 thì làm trước):

   ```bash
   adb connect <ip-tv>:5555
   ```

2. **Cài và mở app:**

   ```bash
   adb -s <ip-tv>:5555 install -r app/build/outputs/apk/release/app-release.apk
   adb -s <ip-tv>:5555 shell am start -n com.xyz.tvhome/.HomeActivity
   ```

   In ra `Success` là cài xong, TV sẽ mở TV Home lên. `install -r` cài đè bản cũ, giữ nguyên theme đang chọn và giá đã
   lưu. Nếu TV Home đang là màn hình chính, cài xong TV sẽ mở lại bản mới.

3. **Đặt làm màn hình chính** (chỉ làm một lần). Android TV 9 không có mục chỉnh trong Cài đặt nên phải dùng lệnh:

   ```bash
   adb -s <ip-tv>:5555 shell cmd package set-home-activity com.xyz.tvhome/.HomeActivity
   adb -s <ip-tv>:5555 shell pm disable-user --user 0 com.google.android.tvlauncher
   adb -s <ip-tv>:5555 shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
   ```

   Lệnh đầu đặt TV Home làm màn hình chính. Lệnh thứ hai tắt màn hình chính của Google, để bấm Home không quay về
   đó nữa. Lệnh cuối mở màn hình chính ngay; không chạy thì bấm nút Home trên remote cũng được.

4. **Chọn theme:** Trên TV, đi lên nút đổi theme ở hàng trên cùng, bấm OK rồi chọn một theme.

5. **Tắt chế độ nhà phát triển** (nên làm sau khi cài xong, để giảm rủi ro bảo mật):

   ```bash
   adb disconnect <ip-tv>:5555
   ```

   Sau đó trên TV, vào **Cài đặt → Tuỳ chọn thiết bị → Tuỳ chọn nhà phát triển**, tắt **Gỡ lỗi USB** và
   **Gỡ lỗi qua mạng**, rồi tắt luôn công tắc **Tuỳ chọn nhà phát triển** ở đầu trang. TV Home vẫn là màn hình chính.
   Lần sau muốn cài bản mới, trả lại màn hình chính của Google hay gỡ app, bật lại theo bước 2.

**Trả lại màn hình chính của Google:**

```bash
adb -s <ip-tv>:5555 shell pm enable com.google.android.tvlauncher
adb -s <ip-tv>:5555 shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
adb -s <ip-tv>:5555 shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
```

Muốn dùng lại TV Home thì chạy lại bước 5.3.

**Gỡ app:** Trả lại màn hình chính của Google trước (ba lệnh trên), rồi mới chạy:

```bash
adb -s <ip-tv>:5555 uninstall com.xyz.tvhome
```

Nếu gỡ TV Home khi chưa bật lại màn hình chính của Google, TV sẽ rơi về **FallbackHome**, một màn hình gần như
trống.

### Kiểm tra trạng thái

```bash
# Màn hình chính hiện tại
adb -s <ip-tv>:5555 shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME

# App đang mở trên màn hình
adb -s <ip-tv>:5555 shell "dumpsys activity activities | grep -m1 mResumedActivity"

# Các app đang bị tắt (có com.google.android.tvlauncher tức là màn hình chính của Google đang tắt)
adb -s <ip-tv>:5555 shell pm list packages -d

# Lỗi của TV Home (log của trang web có tag TvHome)
adb -s <ip-tv>:5555 shell "logcat -d | grep -E 'TvHome|FATAL|AndroidRuntime'"
```

### Khi gặp lỗi

| Hiện tượng | Nguyên nhân và xử lý |
|---|---|
| `failed to connect … Connection refused` | TV chưa bật gỡ lỗi qua mạng. Làm lại bước 2. Kiểm tra cổng bằng `nc -z -G 3 <ip-tv> 5555` (Mac) hoặc `Test-NetConnection <ip-tv> -Port 5555` (Windows). |
| Chờ mãi không kết nối được | TV đang tắt, ngủ sâu, hoặc khác mạng với máy tính. Thử `ping <ip-tv>`. |
| `adb devices` ghi `unauthorized` | Chưa bấm OK trên TV. Chạy `adb disconnect <ip-tv>:5555`, kết nối lại rồi bấm OK. |
| `more than one device/emulator` | Đang nối nhiều thiết bị. Thêm `-s <ip-tv>:5555` vào lệnh. |
| `adb: command not found` (Mac) hoặc `The term 'adb' is not recognized` (Windows) | Chưa chạy lệnh thêm `adb` vào PATH ở cuối bước 1. |
| Đã đổi màn hình chính mà TV vẫn mở app cũ | `set-home-activity` chỉ có hiệu lực từ lần mở sau. Bấm Home trên remote, hoặc chạy lệnh `am start … HOME` ở bước 5. |
| Bấm Home ra màn hình gần như trống | TV đang ở FallbackHome, không có màn hình chính nào được bật. Chạy bước 5.3, hoặc trả lại màn hình chính của Google. |
| Mất kết nối sau khi TV khởi động lại | Chạy lại `adb connect <ip-tv>:5555`. IP đổi thì xem lại IP ở bước 2. |

## Dành cho developer

- Cấu trúc thư mục:

  ```
  themes/
    core.js             dùng chung cho mọi theme: đồng hồ, âm lịch, giá, Wi-Fi, di chuyển bằng remote
    fonts/              font web (woff2) và giấy phép OFL
    fonts/panel/        font TTF cho bảng chọn theme (native)
    shared/modern.css   bố cục chung của các theme hiện đại (theme mới đừng sửa file này)
    site.json           tên chủ nhà trong lời chào
    themes/<id>/        index.html, style.css, theme.json, preview.webp, ảnh và script riêng
  app/                  app Android (Kotlin, package com.xyz.tvhome): WebView, phím remote, giá, nhạc, bảng chọn theme
    src/main/           code chung
    src/public/         LivePrices: nguồn giá, mặc định để trống, có bản mẫu trong comment
  scripts/              build-web.mjs (chép và nén các theme được chọn), new-theme.mjs, preview.mjs
  ```

- **Kiểm tra PR.** Mỗi pull request vào `main` chạy `check_pr` (GitHub Actions, `.github/workflows/check_pr.yml`): cài
  thư viện của `scripts/` rồi build bản release. Build không thành công thì không merge được. APK của lần chạy nằm
  trong mục Artifacts của lần chạy đó.

- <a id="vi-prices"></a>**Nguồn giá.** Giá vàng, giá xăng dầu đi qua interface `Prices` (`app/src/main/java/com/xyz/tvhome/prices/`). Nguồn
  nào cũng trả về một `PriceBoard`, và chỉ `PriceBoard.toJson()` mới tạo JSON cho `core.js` đọc.

  | Trường | Kiểu | Đơn vị |
  |---|---|---|
  | `sjc`, `ring` (vàng miếng SJC, nhẫn 999.9) | `GoldPrice(buy, sell, readAt)` | đồng/lượng, ví dụ `142_000_000` |
  | `e10`, `e5`, `diesel` (E10 RON 95, E5 RON 92, dầu DO 0,05S) | `FuelPrice(perLitre, readAt)` | đồng/lít, ví dụ `29_050` |

  - `readAt` là lúc đọc được giá (epoch ms). Quá 3 giờ không cập nhật thì giá bị làm mờ; vàng quá 1 ngày, xăng dầu
    quá 8 ngày thì hiện "–". Chưa có giá thì để `null`, cũng hiện "–".
  - Giá vô lý thì bị bỏ: vàng ngoài 10–2.000 triệu/lượng hoặc giá mua cao hơn giá bán, xăng dầu ngoài 10.000–80.000
    đ/lít.
  - Nguồn giá là `app/src/public/java/com/xyz/tvhome/prices/LivePrices.kt`. Mặc định file này để trống: không lấy giá,
    các ô giá hiện "–".
  - Muốn có giá: mở `app/src/public/java/com/xyz/tvhome/prices/LivePrices.kt`. Cuối file có một bản
    cài đặt mẫu đầy đủ nằm trong comment (lấy giá mỗi giờ, thử lại khi lỗi, chờ đồng hồ đúng, lấy lại khi có mạng).
    Bỏ comment, xoá class trống ở trên, đổi `PRICES_URL` thành nguồn của bạn, sửa hàm `read()` cho đúng định dạng của
    nguồn, rồi build như bình thường.

- <a id="vi-music"></a>**Nhạc theme.** Cùng cách với giá: interface `Music` (`app/src/main/java/com/xyz/tvhome/music/`) gồm `on`,
  `toggle()`, `theme(file)`, `show()`, `hide()`. Có `PlayerMusic` (mặc định, phát bằng `MediaPlayer`, chỉ khi màn hình
  chính đang hiện) và `NoopMusic` (`-Pmusic=noop`). Theme khai báo nhạc bằng `"music"` trong `theme.json` và một nút
  `data-action="music"`; build báo lỗi nếu có cái này mà thiếu cái kia.

### <a id="vi-bridge"></a>Bridge: trang ↔ app

Trang theme là một trang web (HTML, CSS, JS) chạy bên trong app. Trang không tự mở YouTube hay lấy giá vàng được, còn
app thì cần báo cho trang khi có chuyện xảy ra (bấm phím, có giá mới…). Hai bên nói chuyện qua **bridge**, theo hai
chiều:

```
                    Chiều 1: window.TV.launch(...), TV.prices(), …
  Trang theme (JS)  ─────────────────────────────────────────────▶  App (Kotlin)
  themes/core.js       ◀─────────────────────────────────────────────  HomeActivity
  theme của bạn     Chiều 2: nav(), refresh(), onPrices(), onNetwork()
```

| Chiều | Ai gọi ai | Danh sách hàm | Code cài đặt |
|---|---|---|---|
| 1 | Trang gọi app | `TvApi` (`app/src/main/java/com/xyz/tvhome/web/TvApi.kt`) | `Bridge.kt` |
| 2 | App gọi trang | `PageApi` (`app/src/main/java/com/xyz/tvhome/web/PageApi.kt`) | `Page.kt`, gọi hàm trong `themes/core.js` |

#### Chiều 1: trang gọi app (`window.TV`)

App gắn vào trang một object tên `TV`. Trang gọi hàm của nó để nhờ app làm việc:

| Hàm | Trả về | Làm gì |
|---|---|---|
| `TV.launch(pkg)` | — | Mở app theo package, ví dụ `'com.google.android.youtube.tv'` |
| `TV.openSettings(which)` | — | `'wifi'`: cài đặt Wi-Fi; giá trị khác: Cài đặt chung |
| `TV.openThemes()` | — | Mở bảng chọn theme |
| `TV.openApps()` | — | Mở danh sách ứng dụng của TV |
| `TV.online()` | `true` / `false` | Mạng có vào được Internet không |
| `TV.prices()` | chuỗi JSON | Giá vàng và xăng dầu (xem ví dụ bên dưới) |
| `TV.musicOn()` | `true` / `false` | Nhạc của theme đang bật không |
| `TV.toggleMusic()` | `true` / `false` | Bật hoặc tắt nhạc, trả về trạng thái mới |

**Cách 1, không cần viết JS: dùng thuộc tính `data-*`.** Phần lớn các nút chỉ cần HTML. `core.js` tự lo việc di chuyển
bằng remote, tô ô đang chọn (class `focus`), và gọi `TV` khi bấm OK:

```html
<div class="tile" data-nav="dock" data-pkg="com.netflix.ninja">Netflix</div>   <!-- bấm OK: TV.launch(...) -->
<div class="btn" data-nav="top" data-action="settings">…</div>                 <!-- bấm OK: TV.openSettings(...) -->
```

- `data-nav`: `top` là hàng nút trên cùng, `dock` là hàng ô lớn ở dưới.
- `data-pkg`: package của app cần mở.
- `data-action`: `themes`, `wifi`, `music`, `apps` hoặc `settings`.

**Cách 2, tự gọi trong JS.** Viết trong script riêng của theme, đặt sau `core.js` trong `index.html`:

```js
if (window.TV) {                                  // mở index.html bằng Chrome thì không có TV, nên phải kiểm tra
  TV.launch('com.google.android.youtube.tv');     // mở YouTube
  var coMang = TV.online();                       // true / false, trả về ngay
}
```

**Đọc giá:** `TV.prices()` trả về một chuỗi JSON. Sản phẩm nào không có trong JSON nghĩa là chưa có giá.

```js
var gia = JSON.parse(TV.prices());
// gia.sjc  = { buy: 138700000, sell: 141700000, t: <lúc đọc giá, ms> }   vàng SJC, đồng/lượng
// gia.ring = { buy, sell, t }                                            nhẫn 999.9
// gia.e10  = { price: 28250, t }                                         xăng E10, đồng/lít (cũng có e5, do)
if (gia.sjc) hien('sjc', (gia.sjc.sell / 1e6).toFixed(1) + ' triệu');   // "141.7 triệu"
```

**Thêm một hàm mới cho chiều 1:**
1. Khai báo trong `TvApi.kt`, có ghi chú hàm làm gì.
2. Cài đặt trong `Bridge.kt`: `override fun …`, có `@JavascriptInterface` (thiếu dòng này thì JS không thấy hàm).
3. Thêm vào bảng ở trên.

#### Chiều 2: app gọi trang

Khi có chuyện xảy ra, app gọi một hàm của `core.js` để trang vẽ lại:

| App gọi | Khi nào | `core.js` làm gì |
|---|---|---|
| `nav(dir)` | Bấm phím remote (bảng chọn theme đang đóng). `dir` là `'left'`, `'right'`, `'up'`, `'down'`, `'ok'` hoặc `'back'` | Di chuyển ô đang chọn; `'ok'` thì bấm ô đó |
| `refresh()` | Quay về màn hình chính, hoặc TV chỉnh giờ (ví dụ sau khi rút điện) | Vẽ lại ngày giờ, giá, Wi-Fi, nút nhạc; ô đang chọn về YouTube |
| `onPrices()` | Có giá mới | Đọc lại `TV.prices()` và vẽ giá |
| `onNetwork()` | Mạng có lại hoặc mất | Đọc lại `TV.online()` và vẽ biểu tượng Wi-Fi |

**Theme muốn làm thêm việc riêng** (ví dụ vẽ lại ô đếm ngày Tết) thì **bọc** hàm của `core.js`, đừng thay thế, để phần
chung vẫn chạy:

```js
var cu = window.refresh;
window.refresh = function () {
  if (cu) cu();        // việc cũ của core.js: ngày giờ, giá, Wi-Fi…
  veDemNgay();         // việc của theme
};
```

Làm y như vậy với `onPrices` và `onNetwork`. Ví dụ thật: `themes/themes/tet/tet.js`, `themes/themes/noel/noel.js`.

**Thêm một hàm mới cho chiều 2:**
1. Viết hàm trong `themes/core.js` và gắn lên `window` (như `window.onNetwork = net`).
2. Khai báo trong `PageApi.kt`, có ghi chú khi nào app gọi.
3. Cài đặt trong `Page.kt`: `override fun … = js("window.tenHam && tenHam()")`.
4. Gọi nó từ chỗ cần trong app (thường là `HomeActivity`), rồi thêm vào bảng ở trên.

#### Lưu ý chung

- Chiều 1 là gọi đồng bộ: hàm chạy xong trong app mới trả kết quả về JS. Đừng gọi `TV.*` trong vòng lặp hay mỗi giây;
  đọc giá trong `onPrices`, mạng trong `onNetwork`.
- Chỉ truyền được chuỗi, số và `true`/`false`. Dữ liệu nhiều phần thì trả về chuỗi JSON (như `TV.prices()`).
- Chiều 2 không bao giờ lỗi khi trang thiếu hàm: app gọi kiểu `window.onPrices && onPrices()`.
- Âm lịch có sẵn cho script của theme: `lunar.of(25, 12, 2026)` cho ngày âm `{d, m, y, leap}`, `lunar.canChi(2026)`
  cho tên năm `"Bính Ngọ"`.
- Lúc tạo ảnh preview, `scripts/preview.mjs` giả lập `TV` với giá mẫu, nên ảnh có đủ số.

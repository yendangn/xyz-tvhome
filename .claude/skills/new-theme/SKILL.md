---
name: new-theme
description: Start and build a new TV Home theme (a TV home-screen look in themes/themes/<id>/) from a design link, an image or a description. Use when the user wants to add, start, create or port a theme, or turn a design artboard into a theme.
---

# New theme

Talk to the user in Vietnamese. A theme is a folder `themes/themes/<id>/` (`index.html`, `style.css`, `theme.json`,
`preview.webp`, plus its own images). The app needs no Kotlin change for a new theme.

## 1. Gather what is missing

- **id**: lowercase letters, digits and `-` (e.g. `tet-2027`); not an existing folder (`node scripts/build-web.mjs --list`).
- **Name** shown in the theme panel, in Vietnamese (e.g. "Tết 2027").
- **Design**: a claude.ai design link (read the artboard's `.dc.html` with the Artifact tool's `read` action: list
  the files, read `project/canvas.json`, then the board's file), an image, or a description.

Ask once for whatever is missing, all in one AskUserQuestion call. Don't ask for anything you can decide yourself.

## 2. Scaffold

```bash
node scripts/new-theme.mjs <id> "<Tên>"
```

This makes a working page, copied from the "modern" theme, that stands alone (its own full `style.css`; never edit
`themes/shared/modern.css` for a new theme). Restyle it.

## 3. Build the page (`index.html`, `style.css`)

`themes/core.js` fills elements by id and drives focus; the full list is in its header comment. Keep:

- `#stage`: 1920×1080, `position: absolute; transform-origin: 0 0` (core.js scales it to the screen). Lay out in px.
- A `data-nav="top" data-action="themes"` button (the only way into the theme panel; the build fails without it).
  Add it even when the design has none, styled like the design's other round buttons. Keep `data-action="wifi"` (gets `.off` offline) and
  the four dock tiles: `data-nav="dock"` with `data-pkg="com.google.android.youtube.tv"`, `data-pkg="com.android.vending"`,
  `data-action="settings"` and `data-action="apps"` ("Ứng dụng", a 2×2 grid icon: opens the TV's own app list).
  Designs usually draw three tiles; fit the fourth in, smaller type rather than clipped words.
- The greeting's name is `<span data-owner>Yên Đặng</span>`: the build puts the owner's name there (themes/site.json,
  or `-Powner`), so never write a name as plain text.
- `<script src="../../core.js"></script>` last in `<body>`. Paths are relative: `../../fonts/…`, own images beside
  `index.html`.
- Data ids: `clock` (core.js adds a `.sec` span: hide it with `.clock .sec { display: none; }` when the design shows
  HH:MM), `weekday`, `dayNum`, `monthNum`, `dateText`, `dateLong`, `lunarLong`, `lunarShort`, `lunarLine` ("15 tháng 9 âm lịch"), `dayPart` ("buổi chiều", for a greeting
  "Chào <span id="dayPart"></span>, …" that follows the hour), `ccYear`/`ccMonth`/`ccDay`,
  calendar `cal` (gets `.six` for 6-row months: shrink `.cell`), `calTitle`, `calSpan`, `calGrid` (cells
  `.cell` with `<b>` day and `<i>` lunar; `.sun`, `.special`, `.today`), prices `sjc`, `ring`, `sjcBuy`, `ringBuy`,
  `e10`, `e5`, `do`, `priceTime`. Leave out what the design doesn't show.
- Focus: the focused element gets `.focus`. A design usually draws one tile highlighted (often YouTube): make that
  look the `.focus` style, and draw every tile plainly otherwise.

Weak TV boxes: no `backdrop-filter`, no `filter: blur()`, no infinite CSS animations (the Wi-Fi wave in older
themes runs 3 times). Static gradients, hard shadows and halftone `radial-gradient` backgrounds are fine (painted once).
Images: WebP, 1920×1080 at most, quality 80.

An effect that keeps going (twinkling lights, a blinking dot) is still possible, the way Noel's lights do it
(`themes/themes/noel/noel.js`, `style.css`):

- Drive it from a timer in the theme's own JS (`setInterval` every 0.5–1 s) that swaps classes, never from a
  CSS `animation` or `transition` that loops. A running CSS animation makes the WebView produce frames 60 times a
  second even with `steps()` and nothing changing; a timer leaves the page idle between changes.
- Make the changing parts HTML elements with `will-change: opacity` (or `transform`) and change only those
  properties, so each change is one frame of new values for the GPU layers, with no repaint. Not SVG children:
  changing one repaints the whole SVG.
- Timers stop on their own while Home is hidden. If the state must be right on return, wrap `window.refresh`
  (as `ring.js` and `noel.js` do).

Measured on the VinSmart TV (4 cores): 28 twinkling bulbs as SVG with a CSS animation cost about 24 % of one core more
than no effect; as HTML layers driven by a timer, about nothing. Measure a new effect with `/proc/<pid>/stat` CPU
time over 30 s (Android 9's `top` is not reliable), Home on screen and nobody touching the remote.

## 4. Fonts

Prefer fonts already in `themes/fonts/`. For a new one:

1. Download it from `https://raw.githubusercontent.com/google/fonts/main/ofl/<family>/` with its `OFL.txt`. Check the
   licence's first lines for "with Reserved Font Name"; if present, ask before subsetting.
2. Tools: `python3 -m venv <scratchpad>/venv && <scratchpad>/venv/bin/pip install fonttools brotli`.
3. Pin variable axes to what the page uses (`fonttools varLib.instancer in.ttf wght=800 -o out.ttf`; keep `opsz`
   if the page uses several sizes), then subset to Latin + Vietnamese and write woff2:
   ```bash
   U="U+0000-00FF,U+0131,U+0152-0153,U+02BB-02BC,U+02C6,U+02DA,U+02DC,U+0300-0301,U+0303-0304,U+0308-0309,U+0323,U+0329,U+0102-0103,U+0110-0111,U+0128-0129,U+0168-0169,U+01A0-01A1,U+01AF-01B0,U+1EA0-1EF9,U+2000-206F,U+20AB,U+20AC,U+2122,U+2191,U+2193,U+2212,U+2215,U+FEFF,U+FFFD"
   pyftsubset out.ttf --unicodes="$U" --layout-features='*' --flavor=woff2 --output-file=themes/fonts/<Family>-<Style>.woff2
   ```
4. Copy the licence to `themes/fonts/<Family>-OFL.txt` (build-web.mjs ships it with the font, matched by name).

## 5. Theme panel (`theme.json`)

`"name"`, `"order"` (place in the panel; the scaffold puts it last) and `"panel"`: the native picker's look in this
theme. Fields and defaults: `app/src/main/java/com/xyz/tvhome/theme/ThemeStyle.kt`; `themes/themes/retro/theme.json`
uses almost all of them. Required: `scrim`, `panel`, `ink`, `titleFont`, `nameFont`, `tagFont`, `tag`, `sel`, `selBg`.
Colours: `#RRGGBB`, `#AARRGGBB` or `rgba(r,g,b,a)`. Sizes: px on the 1920×1080 stage.

- Panel fonts are static TTFs in `themes/fonts/panel/` (Android can't read woff2): reuse one there, or make one as in
  step 4 with every axis pinned, `--no-hinting`, no `--flavor`, lowercase `snake_case.ttf`.
- Names must fit: the longest theme name at `nameSize` in `nameFont` must be under `276 − selShadow.dx` px
  (measure with fontTools: sum of `hmtx` advances / unitsPerEm × nameSize). The panel cuts longer names with "…".
- No blur in the panel either: a glassy look is a tinted, nearly opaque `panel` colour.
- The data-source line at the screen's foot takes `ink`, faded. When the page's bottom edge is far from the page
  colour `ink` suits (a white snowbank on a night theme), set `footInk`. A theme that shows no prices sets
  `"showSource": false`.
- A theme script can use the lunar calendar: `window.lunar.of(d, m, y)`, `lunar.canChi(y)` (see core.js's header;
  `themes/themes/tet/tet.js` finds the next Tết with it).

## 6. Check

```bash
node scripts/preview.mjs <id>                       # themes/themes/<id>/preview.webp; look at it
app/gradlew -p app assembleDebug -Pthemes=<id>          # fails on a missing font, image or bad link
```

Look at the preview against the design and fix differences. To try it on a device, use the build-release skill, or
`adb install -r app/build/outputs/apk/debug/app-debug.apk`. Ask before pressing keys on a connected device: someone
may be using it.

Finish with what you built, what differs from the design and why, and how it looked in the preview.

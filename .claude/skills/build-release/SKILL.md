---
name: build-release
description: Build the TV Home release APK with a chosen set of themes and the name Home greets, and optionally install it. Lists the themes in themes/themes, asks which to include and whose name to show, then runs Gradle. Use when the user wants a release build, an APK, or to pick which themes go into the app.
---

# Build release

Talk to the user in Vietnamese.

## 1. Gather

Run these together:

```bash
node scripts/build-web.mjs --list                                   # "<id>  <name>" per line, in panel order
"$(grep sdk.dir app/local.properties | cut -d= -f2)/platform-tools/adb" devices -l
cat themes/site.json                                                   # {"owner": "…"}: the name Home greets
```

If `scripts/node_modules/` is missing, run `npm --prefix scripts install` first. If `--list` fails, report its one-line error and stop.

## 2. Ask (one AskUserQuestion call)

- **"Build những theme nào?"** (single select): "Tất cả (<N> theme)" (Recommended) and "Chọn từng theme".
- **"Tên trong lời chào? Gõ tên vào ô Other."** (single select). Only these, as the user wants:
  - "Để trống": no name, and no comma before it ("Xin chào", "Giáng sinh an lành").
  - "Nhập tên" (description: "gõ tên vào ô Other"); it is there because the tool needs two options. Picked without
    typing a name: ask again. The name typed under "Other" is the answer.
- **"Sau khi build?"** (single select), only when `adb devices` lists a device: "Chỉ build" and "Build rồi cài lên
  <model>" (one option per device, up to 3).

If they pick "Chọn từng theme", ask a second AskUserQuestion: the themes split in order into groups of 4, one
multiSelect question per group ("Nhóm 1/2: chọn theme", label = name, description = id), up to 4 questions (16
themes). With more themes than that, list the ids and let the user type them under "Other". Nothing picked: ask again.

## 3. Build

```bash
app/gradlew -p app assembleRelease -Pthemes=<id1>,<id2>   # leave out -Pthemes for every theme
```

Before building, write the answer to `themes/site.json` (`{"owner": "<Tên>"}`, UTF-8; `""` for "Để trống"); the build
reads it (scripts/site.mjs fills every theme's `<span data-owner>`), and so do later builds, plain Gradle ones
too. Say in the report when it changed. (`-Powner="…"` overrides it for one build, but can't ask for no name.)

Run it in the foreground (it takes seconds). Gradle runs `scripts/build-web.mjs`: it copies only the files the chosen
themes use and minifies the pages, printing one `web:` line. If Gradle can't find `node` (Android Studio from the
Dock), add `-Pnode="$(command -v node)"`. On a JDK error on a Mac, prefix `JAVA_HOME=$(/usr/libexec/java_home -v 17)`; on Windows, check that `java -version` says 17.

The panel lists the built themes in their `theme.json` order, not in the order picked. A device whose saved theme
isn't in the build opens on the first one.

## 4. Install (when asked)

```bash
"$ADB" -s <serial> install -r app/build/outputs/apk/release/app-release.apk
"$ADB" -s <serial> shell am start -n com.xyz.tvhome/.HomeActivity
```

Don't press keys on the device afterwards: someone may be using it.

## 5. Report

The themes built, the name greeted, the `web:` line, the APK path (`app/build/outputs/apk/release/app-release.apk`)
and its size, and whether it was installed. On failure, show the error lines, not the whole log.

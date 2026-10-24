---
name: setup
description: Walk the user through the full TV Home setup, from a fresh Mac or Windows PC to the app running as the TV's home screen. Checks the tools, installs what is missing, prepares the project, guides developer mode on the TV, connects adb, builds, installs and sets TV Home as home. Use when the user wants to set up the project, set up a TV, or "cài từ đầu".
---

# Setup

Talk to the user in Vietnamese, in plain words: they may not be a developer. Do every step you can yourself; ask
the user only for what happens on the TV or needs their click. One step at a time: say what you are doing, run it,
say what you saw. On an error, show the few lines that matter and the fix, never the whole log.

The full manual guide is README.md ("Làm từng bước", Vietnamese) or README.en.md ("Step by step", English); this
skill does the same steps. TV steps were tested on a
VinSmart 4K (Android 9). It works on a Mac and on Windows (Claude Code runs Git Bash there, so the bash commands
below work; the differences are noted). Find out which first: `uname -s` prints `Darwin` on a Mac and `MINGW…` or
`MSYS…` on Windows. Below, `$SDK` is the Android SDK: `$HOME/Library/Android/sdk` on a Mac,
`$LOCALAPPDATA/Android/Sdk` on Windows.

## 0. Ask what to do (one AskUserQuestion call)

- **"Làm phần nào?"** (single select): "Tất cả: máy tính, TV, build và cài" (Recommended), "Chỉ cài máy tính",
  "Chỉ kết nối và cài lên TV" (the computer is already set up).

Skip the steps the answer leaves out. Before step 3 onwards, if `app/local.properties` or `scripts/node_modules/` is missing,
do step 2 first anyway.

## 1. Tools on the computer

Check all at once:

```bash
git --version
node -v
ls "$SDK/platforms" "$SDK/platform-tools"
# Mac
sw_vers -productVersion; /usr/libexec/java_home -v 17; ls "/Applications/Google Chrome.app"; command -v brew
# Windows
java -version; ls "/c/Program Files/Google/Chrome/Application/chrome.exe"; command -v winget
```

Needed: Git, JDK 17, Node 20 or later, Android SDK with `platforms/android-34` and `platform-tools`, Chrome (only
for theme previews). List what is missing in one message, then fix each:

| Missing | Fix |
|---|---|
| Git | Mac: `xcode-select --install`; the user clicks Install in the dialog. Windows: Claude Code needs Git for Windows already, so it is there. Wait until `git --version` works. |
| JDK 17 | Mac with Homebrew: `brew install --cask temurin@17`. Windows with winget: `winget install EclipseAdoptium.Temurin.17.JDK`. Otherwise send https://adoptium.net/temurin/releases/?version=17 (Mac .pkg; Windows .msi with **Set JAVA_HOME variable** ticked). |
| Node | Mac with Homebrew: `brew install node@20` and follow its PATH hint. Windows with winget: `winget install OpenJS.NodeJS.LTS`. Otherwise https://nodejs.org (LTS). |
| Android SDK | the user installs Android Studio (https://developer.android.com/studio) and clicks through its setup wizard. Then, in Settings → Languages & Frameworks → Android SDK, tick Android 14 (API 34). |
| Chrome | optional: https://www.google.com/chrome. |

Ask before any `brew install` or `winget install` (it changes their computer). On Windows, a new install is on the
PATH only in a new terminal: ask the user to restart Claude Code if `java` or `node` is still not found. For what the user installs by hand, wait for them to say
done, then check again. Don't go on until every needed tool is found.

If the SDK is not at `$SDK`, ask where it is (Android Studio shows it on that same settings page)
and use that path below.

## 2. The project

If the current folder is not this repo, clone it where the user wants (default `~/xyz-tvhome`):

```bash
git clone https://github.com/yendangn/xyz-tvhome.git ~/xyz-tvhome
```

Then, in the repo:

```bash
# Mac
[ -f app/local.properties ] || echo "sdk.dir=$SDK" > app/local.properties
# Windows (forward slashes: app/local.properties treats \ as an escape)
[ -f app/local.properties ] || echo "sdk.dir=$(cygpath -m "$SDK")" > app/local.properties
npm --prefix scripts install
# Mac
JAVA_HOME=$(/usr/libexec/java_home -v 17) app/gradlew -p app assembleDebug
# Windows
app/gradlew -p app assembleDebug
```

It must end with `BUILD SUCCESSFUL` (the first run takes a few minutes). `app/local.properties` stays on this computer;
never commit it. If Gradle can't find `node`, add `-Pnode="$(command -v node)"`.

From here on use `ADB="$(grep sdk.dir app/local.properties | cut -d= -f2)/platform-tools/adb"` (on Windows the file is
`adb.exe`; the same path works in Git Bash).

## 3. The TV (the user does this with the TV remote)

Give these steps in one message and wait for the user to send the IP:

1. Settings → Device Preferences → About (Cài đặt → Tuỳ chọn thiết bị → Giới thiệu). Press **Build** (Bản dựng)
   about 7 times, until the TV says "You are now a developer".
2. Back to Device Preferences → Developer options. Turn on **USB debugging** (Gỡ lỗi USB), and **ADB over network**
   (Gỡ lỗi qua mạng) if it is there.
3. Settings → Network & Internet: pick the Wi-Fi and read the **IP address** (e.g. `192.168.1.39`).
4. The computer and the TV must be on the same Wi-Fi.

Then connect:

```bash
"$ADB" connect <ip>:5555
"$ADB" devices -l
```

Tell the user to choose **Always allow from this computer** and **OK** on the TV's "Allow USB debugging?" prompt.
Run `devices -l` again until the TV's line says `device`.

| Seen | Do |
|---|---|
| `unauthorized` | OK was not pressed: `"$ADB" disconnect <ip>:5555`, connect again, ask them to press OK. |
| `Connection refused` | debugging over network is off: repeat TV step 2. |
| timeout | TV off or asleep, or another network: `ping -c 3 <ip>`, check the IP. |

Suggest giving the TV a fixed IP in the router, so the IP does not change after a restart.

## 4. Build

Use the build-release skill's steps: ask which themes and whose name the greeting shows, write `themes/site.json`, run
`app/gradlew -p app assembleRelease` (with `-Pthemes=…` when not all). The APK is
`app/build/outputs/apk/release/app-release.apk`.

## 5. Install and make it the home screen

```bash
"$ADB" -s <ip>:5555 install -r app/build/outputs/apk/release/app-release.apk
"$ADB" -s <ip>:5555 shell am start -n com.xyz.tvhome/.HomeActivity
```

Then ask (AskUserQuestion) **"Đặt TV Home làm màn hình chính?"**: "Có" (Recommended) / "Chưa". On yes:

```bash
"$ADB" -s <ip>:5555 shell cmd package set-home-activity com.xyz.tvhome/.HomeActivity
"$ADB" -s <ip>:5555 shell pm disable-user --user 0 com.google.android.tvlauncher
"$ADB" -s <ip>:5555 shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
```

Check it took:

```bash
"$ADB" -s <ip>:5555 shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

The last line must be `com.xyz.tvhome/.HomeActivity`. Don't press keys on the TV yourself: someone may be watching.

## 6. Turn developer mode off

Explain the risk in one line: while debugging is on, any computer on the same Wi-Fi can connect to the TV with adb.
Ask (AskUserQuestion) **"Tắt chế độ nhà phát triển trên TV?"**: "Tắt ngay" (Recommended) / "Để sau". On yes, run
`"$ADB" disconnect <ip>:5555`, then tell the user to open Settings → Device Preferences → Developer options on the
TV, turn off USB debugging and network debugging, then the Developer options switch at the top. TV Home stays the
home screen; they turn debugging on again (step 3) to install a new version, go back to Google or uninstall.

## 7. Report

In a few lines: what was installed on the computer, the TV's IP, the themes built, whether TV Home is the home screen,
whether developer mode is off,
and how to pick a theme (the theme button at the top, OK). Add how to go back to the Google home screen, and that
this must be done before uninstalling TV Home, or the TV has no home screen:

```bash
"$ADB" -s <ip>:5555 shell pm enable com.google.android.tvlauncher
"$ADB" -s <ip>:5555 shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
"$ADB" -s <ip>:5555 shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
```

More troubleshooting: README.md, "Khi gặp lỗi" (README.en.md, "Troubleshooting").

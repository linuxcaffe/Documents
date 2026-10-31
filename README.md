# Fossify Documents (linuxcaffe fork)

> **This is a fork** of [FossifyOrg/Documents](https://github.com/FossifyOrg/Documents), built for reading
> and editing Markdown notes on a phone, including [nb](https://github.com/xwmx/nb) notebooks in
> Termux. Everything below this box is upstream's README. The fork's builds come from the `phone`
> branch; each feature also has its own branch and fork PR, offered upstream one by one
> (Fossify asks for an issue first).

<details>
<summary><b>What the fork adds</b></summary>

| Feature | Fork PR |
|---|---|
| YAML front matter shown as a metadata card | #1 |
| New files created in the folder you're browsing | #2 |
| Hidden files (`.git`, `.index` …) hidden, with a setting to show them | #4 |
| Relative links between Markdown files (`[x](other.md)`) open the linked file | #5 |
| Text files without a known extension (`.bashrc`, `vimrc`) open as text | #6 |
| m-sync status strip in synced folders, conflict copies marked (fork only) | #7 |
| Delete files: selection bar and viewers, always confirmed | #8 |
| Rename a file (also in Termux folders, whose provider can't rename) | #9 |
| New folder | #10 |
| Compact editor header in landscape; settings to scroll it away and hide the formatting toolbar | #11 |
| Move / Copy to another folder, with an in-app folder chooser | #12 |
| Info for a file or folder, with Copy path / Copy link | #13 |

</details>

### Installing the fork

**Today:** the `phone` branch's CI ("Phone build") makes a debug APK signed with one fixed key, so each
build installs over the last. It's the run's artifact `phone-build-foss-debug` (a GitHub login is
needed to download it). It installs as **`org.fossify.documents.debug`**, next to the F-Droid app
(`org.fossify.documents`), which it doesn't touch. On Huawei phones, Play Protect may block an APK
from outside the store: turn its scanning off for the install, then back on.

**Planned:** GitHub Releases on this repo, a signed APK per tagged release, installed and kept up to
date with [Obtainium](https://github.com/ImranR98/Obtainium) (give it this repo's URL once). The
release build will get its own app ID and name, so it lives beside F-Droid's version: a build with
the same ID can't install over an app signed by F-Droid.

**Notes in Termux:** in the app, + → Open folder, then in the picker ☰ → Termux, and the folder you
want (for nb, `.nb/<notebook>`). [tw-mobilize](https://github.com/linuxcaffe/tw-mobilize)'s
`mobilize android` walks through it.

---


<img alt="Logo" src="graphics/icon.webp" width="120" />

<a href='https://play.google.com/store/apps/details?id=org.fossify.documents'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height=80/></a> <a href="https://f-droid.org/packages/org.fossify.documents/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-en.svg" alt="Get it on F-Droid" height=80/></a> <a href="https://apt.izzysoft.de/fdroid/index/apk/org.fossify.documents"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height=80/></a>

Fossify Documents is a private, offline document reader and organizer for Android.

It supports PDF, DOCX, plain text, Markdown, CSV, and HTML documents. Text,
Markdown, and CSV files can be edited, while DOCX files open in a clean mobile
reading view and CSV files use a dedicated table viewer by default. Documents can
be opened directly, organized through selected folders, searched, filtered, sorted,
and marked as favorites.

The app contains no ads or unnecessary permissions and integrates with Fossify's
shared themes, custom fonts, and appearance settings.

➡️ Explore more Fossify apps: https://www.fossify.org<br>
➡️ Open-Source Code: https://www.github.com/FossifyOrg<br>
➡️ Join the community on Reddit: https://www.reddit.com/r/Fossify<br>
➡️ Connect on Telegram: https://t.me/Fossify

<div align="center">
<img alt="Documents home screen" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.png" width="30%">
<img alt="Folder browser" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3_en-US.png" width="30%">
<img alt="Markdown document" src="fastlane/metadata/android/en-US/images/phoneScreenshots/5_en-US.png" width="30%">
</div>

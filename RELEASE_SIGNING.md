# Configurarea semnării APK-ului

APK-ul publicat în prezent a fost construit ca variantă Debug de GitHub Actions. Cheia Debug generată de runner nu este o cheie de publicare stabilă, deci Android poate refuza instalarea unei versiuni viitoare peste acel APK.

Pentru publicarea actualizărilor semnate:

1. Creează o cheie de semnare Release pe propriul calculator și păstrează o copie de rezervă sigură. Nu încărca fișierul keystore în repository și nu îl trimite nimănui.
2. În GitHub, deschide **Settings → Secrets and variables → Actions** și adaugă secretele:
   - `KEYSTORE_BASE64` — conținutul fișierului keystore codificat Base64;
   - `KEYSTORE_PASSWORD` — parola keystore-ului;
   - `KEY_ALIAS` — aliasul cheii;
   - `KEY_PASSWORD` — parola cheii.
3. La fiecare versiune nouă, actualizează `versionName` și `versionCode` în `app/build.gradle.kts`, apoi creează un tag Git de forma `vMAJOR.MINOR.PATCH` cu același număr de versiune.
4. Workflow-ul pentru tag va construi varianta Release, o va semna cu cheia configurată și va atașa APK-ul la GitHub Release.

Păstrează aceeași cheie de semnare pentru toate versiunile publicate. Dacă pierzi cheia, Android nu va permite actualizarea instalărilor semnate cu ea. Pentru instalarea primei versiuni Release peste APK-ul Debug existent poate fi necesară dezinstalarea versiunii vechi; dezinstalarea șterge datele locale ale aplicației.
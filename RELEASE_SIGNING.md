# Configurarea semnării APK-ului

APK-ul publicat în prezent a fost construit ca variantă Debug de GitHub Actions. Cheia Debug generată de runner nu este o cheie de publicare stabilă, deci Android poate refuza instalarea unei versiuni viitoare peste acel APK.

## Creează cheia de publicare

Pe calculatorul tău, execută:

```powershell
keytool -genkeypair -v -keystore ipnet-hosting-release.jks -alias ipnet-hosting -keyalg RSA -keysize 2048 -validity 10000
```

Alege și păstrează în siguranță parolele. Fă o copie de rezervă a fișierului keystore pe un dispozitiv sigur. Nu îl încărca în repository și nu îl trimite prin chat sau e-mail.

## Configurează secretele GitHub

În repository, deschide **Settings → Secrets and variables → Actions** și adaugă:

- `KEYSTORE_BASE64` — codifică fișierul keystore în Base64. În PowerShell poți pune valoarea în clipboard cu:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes(".\ipnet-hosting-release.jks")) | Set-Clipboard
  ```
- `KEYSTORE_PASSWORD` — parola keystore-ului;
- `KEY_ALIAS` — `ipnet-hosting`, dacă ai păstrat aliasul din comandă;
- `KEY_PASSWORD` — parola cheii.

Introdu valoarea copiată direct în secretul GitHub. Nu o publica într-un issue, commit sau log.

## Publică o versiune

Creează un tag Git de forma `vMAJOR.MINOR.PATCH`, de exemplu `v1.0.1`. Workflow-ul verifică formatul tag-ului, derivează din el numele și codul versiunii, construiește APK-ul Release, verifică semnătura și îl atașează la un GitHub Release.

Păstrează aceeași cheie pentru toate versiunile viitoare. Dacă pierzi cheia, Android nu va permite actualizarea instalărilor semnate cu ea. Pentru instalarea primei versiuni Release peste APK-ul Debug existent poate fi necesară dezinstalarea versiunii vechi; dezinstalarea șterge datele locale ale aplicației.

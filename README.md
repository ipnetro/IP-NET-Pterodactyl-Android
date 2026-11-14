# IP-NET Hosting

## 🇷🇴 RO


**Administrarea serverelor Pterodactyl, direct de pe Android.

IP-NET Hosting este o aplicație Android nativă, construită cu Kotlin și Jetpack Compose, pentru gestionarea serverelor din panoul IP-NET.

## Identitatea aplicației

| Element | Valoare |
|---|---|
| Nume | **IP-NET Hosting** |
| Application ID | `ro.ipnet` |
| Panou | [ptero.ip-net.ro](https://ptero.ip-net.ro) |
| Android minim | 8.0 (API 26) |
| Versiune publicată | 1.0.1 |

## Funcționalități

### 🇷🇴 (RO) Administrarea serverelor

- Listarea serverelor asociate contului.
- Monitorizarea utilizării CPU, memoriei și spațiului de stocare.
- Pornirea, oprirea și repornirea serverelor.
- Informații despre server și grafice cu istoricul resurselor.

### 🇷🇴 (RO) Consolă și fișiere

- Consolă live prin WebSocket și trimiterea comenzilor.
- Navigarea prin fișiere și directoare.
- Crearea directoarelor, editarea și salvarea fișierelor.
- Redenumirea, ștergerea, arhivarea și dezarhivarea fișierelor.
- Descărcarea fișierelor.

### 🇷🇴 (RO) Instrumente suplimentare

- Crearea și administrarea backup-urilor.
- Administrarea bazelor de date, în limitele permisiunilor contului.
- Crearea și gestionarea programărilor automate.
- Vizualizarea utilizatorilor și revocarea accesului subutilizatorilor.
- Vizualizarea alocărilor de rețea.

## Conectare securizată

1. Deschide panoul Pterodactyl și intră în contul tău.
2. Creează o cheie **Client API** din setările contului.
3. Introdu cheia în aplicația IP-NET Hosting.

Folosește cheia **Client API**, nu cheia **Application API**. După conectarea reușită, aplicația păstrează cheia criptată în spațiul privat al telefonului, cu Android Keystore, și încearcă reconectarea automat la redeschidere. Apasă **IEȘIRE** pentru a șterge cheia salvată. Aplicația transmite cheia prin HTTPS către panoul IP-NET. Nu publica cheia și nu o include într-un issue sau într-o captură de ecran.

Consultă [nota de confidențialitate](PRIVACY.md) și [instrucțiunile de securitate](SECURITY.md).

## Descărcare

Descarcă APK-ul oficial din [GitHub Releases](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/releases). Versiunea publicată în prezent este **1.0.1**.

Versiunea 1.0.1 este semnată Release. Aplicația păstrează cheia Client API criptată pe telefon și se reconectează la redeschidere. Actualizările publicate ulterior cu aceeași semnătură se instalează peste aplicație, fără dezinstalare.

## Compilare și publicare

Proiectul folosește JDK 17, Android SDK 35 și Gradle 8.9.

Compilează local varianta Debug:

```bash
```

GitHub Actions compilează sursa la schimbările proiectului și oferă un APK Debug ca artefact temporar. Publicarea unui APK Release semnat se face printr-un tag Git de forma `vMAJOR.MINOR.PATCH`. Înainte de primul tag Release, configurează cheia permanentă urmând [instrucțiunile de semnare](RELEASE_SIGNING.md). Nu încărca keystore-ul sau parolele în repository.

## Licență

Codul și aplicația sunt proprietare. Citește [LICENSE](LICENSE). Componentele terțe rămân sub licențele lor proprii.

## Feedback și idei

Ai o sugestie sau ai găsit o problemă? Deschide un [issue nou](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/issues/new) și alege formularul potrivit. Citește [ghidul de feedback și contribuții](.github/CONTRIBUTING.md). Te rugăm să nu publici parole, chei API sau date private.

---

**IP-NET Hosting**  
Administrare simplă. Control rapid. Identitate IP-NET.

---

## 🇬🇧 EN


Manage your Pterodactyl servers directly from Android.**

IP-NET Hosting is a native Android app built with Kotlin and Jetpack Compose for managing servers in the IP-NET panel.

## App details

| Field | Value |
|---|---|
| Name | **IP-NET Hosting** |
| Application ID | `ro.ipnet` |
| Panel | [ptero.ip-net.ro](https://ptero.ip-net.ro) |
| Minimum Android | 8.0 (API 26) |
| Published version | 1.0.1 |

## Features

### 🇬🇧 (EN) Server management

- View the servers associated with your account.
- Monitor CPU, memory, and storage usage.
- Start, stop, and restart servers.
- View server details and resource history charts.

### 🇬🇧 (EN) Console and files

- Use the live WebSocket console and send commands.
- Browse files and directories.
- Create directories, edit files, and save changes.
- Rename, delete, archive, and extract files.
- Download files.

### 🇬🇧 (EN) Additional tools

- Create and manage backups.
- Manage databases within your account permissions.
- Create and manage scheduled tasks.
- View users and revoke subuser access.
- View network allocations.

## Secure connection

1. Open the Pterodactyl panel and sign in.
2. Create a **Client API** key in your account settings.
3. Enter the key in the IP-NET Hosting app.

Use a **Client API** key, not an **Application API** key. After a successful connection, the app stores the key encrypted in the phone's private storage using Android Keystore and attempts to reconnect automatically when reopened. Tap **LOG OUT** to clear the saved key. The app sends the key to the IP-NET panel over HTTPS. Do not publish the key or include it in an issue or screenshot.

Read the [privacy notice](PRIVACY.md) and [security instructions](SECURITY.md).

## Download

Download the official APK from [GitHub Releases](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/releases). The current published version is **1.0.1**.

Version 1.0.1 is signed as a Release build. The app stores the Client API key encrypted on the phone and reconnects when reopened. Later updates signed with the same key can be installed over the existing app without uninstalling it.

## Build and release

The project uses JDK 17, Android SDK 35, and Gradle 8.9.

Build the Debug version locally:

```bash
gradle :app:assembleDebug
```

GitHub Actions builds the source when project changes are made and provides a Debug APK as a temporary artifact. A signed Release APK is published using a Git tag in the form `vMAJOR.MINOR.PATCH`. Before the first Release tag, configure the permanent signing key by following the [signing instructions](RELEASE_SIGNING.md). Do not upload the keystore or passwords to the repository.

## License

The app and its code are proprietary. Read [LICENSE](LICENSE). Third-party components remain under their respective licenses.

## Feedback and ideas

Have an idea or found a problem? Open a [new issue](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/issues/new) and choose the appropriate form. Read the [feedback and contribution guide](.github/CONTRIBUTING.md). Please do not post passwords, API keys, or private information.

---

**IP-NET Hosting**  
Simple management. Fast control. The IP-NET experience.

# IP-NET Hosting

🇷🇴 (RO) **Administrarea serverelor Pterodactyl, direct de pe Android. / 🇬🇧 (EN) Manage your Pterodactyl servers directly from Android.**

🇷🇴 (RO) IP-NET Hosting este o aplicație Android nativă, construită cu Kotlin și Jetpack Compose, pentru gestionarea serverelor din panoul IP-NET.

🇬🇧 (EN) IP-NET Hosting is a native Android app built with Kotlin and Jetpack Compose for managing servers in the IP-NET panel.

## 🇷🇴 (RO) Identitatea aplicației / 🇬🇧 (EN) App details

| 🇷🇴 (RO) Element / 🇬🇧 (EN) Field | 🇷🇴 (RO) Valoare / 🇬🇧 (EN) Value |
|---|---|
| 🇷🇴 (RO) Nume / 🇬🇧 (EN) Name | **IP-NET Hosting** |
| Application ID | `ro.ipnet.hosting` |
| Panou / Panel | [ptero.ip-net.ro](https://ptero.ip-net.ro) |
| 🇷🇴 (RO) Android minim / 🇬🇧 (EN) Minimum Android | 8.0 (API 26) |
| 🇷🇴 (RO) Versiune publicată / 🇬🇧 (EN) Published version | 1.0.1 |

## 🇷🇴 (RO) Funcționalități / 🇬🇧 (EN) Features

### 🇷🇴 (RO) Administrarea serverelor / 🇬🇧 (EN) Server management

- 🇷🇴 (RO) Listarea serverelor asociate contului. / 🇬🇧 (EN) View the servers associated with your account.
- 🇷🇴 (RO) Monitorizarea utilizării CPU, memoriei și spațiului de stocare. / 🇬🇧 (EN) Monitor CPU, memory, and storage usage.
- 🇷🇴 (RO) Pornirea, oprirea și repornirea serverelor. / 🇬🇧 (EN) Start, stop, and restart servers.
- 🇷🇴 (RO) Informații despre server și grafice cu istoricul resurselor. / 🇬🇧 (EN) View server details and resource history charts.

### 🇷🇴 (RO) Consolă și fișiere / 🇬🇧 (EN) Console and files

- 🇷🇴 (RO) Consolă live prin WebSocket și trimiterea comenzilor. / 🇬🇧 (EN) Use the live WebSocket console and send commands.
- 🇷🇴 (RO) Navigarea prin fișiere și directoare. / 🇬🇧 (EN) Browse files and directories.
- 🇷🇴 (RO) Crearea directoarelor, editarea și salvarea fișierelor. / 🇬🇧 (EN) Create directories, edit files, and save changes.
- 🇷🇴 (RO) Redenumirea, ștergerea, arhivarea și dezarhivarea fișierelor. / 🇬🇧 (EN) Rename, delete, archive, and extract files.
- 🇷🇴 (RO) Descărcarea fișierelor. / 🇬🇧 (EN) Download files.

### 🇷🇴 (RO) Instrumente suplimentare / 🇬🇧 (EN) Additional tools

- 🇷🇴 (RO) Crearea și administrarea backup-urilor. / 🇬🇧 (EN) Create and manage backups.
- 🇷🇴 (RO) Administrarea bazelor de date, în limitele permisiunilor contului. / 🇬🇧 (EN) Manage databases within your account permissions.
- 🇷🇴 (RO) Crearea și gestionarea programărilor automate. / 🇬🇧 (EN) Create and manage scheduled tasks.
- 🇷🇴 (RO) Vizualizarea utilizatorilor și revocarea accesului subutilizatorilor. / 🇬🇧 (EN) View users and revoke subuser access.
- 🇷🇴 (RO) Vizualizarea alocărilor de rețea. / 🇬🇧 (EN) View network allocations.

## 🇷🇴 (RO) Conectare securizată / 🇬🇧 (EN) Secure connection

1. 🇷🇴 (RO) Deschide panoul Pterodactyl și intră în contul tău. / 🇬🇧 (EN) Open the Pterodactyl panel and sign in.
2. 🇷🇴 (RO) Creează o cheie **Client API** din setările contului. / 🇬🇧 (EN) Create a **Client API** key in your account settings.
3. 🇷🇴 (RO) Introdu cheia în aplicația IP-NET Hosting. / 🇬🇧 (EN) Enter the key in the IP-NET Hosting app.

🇷🇴 (RO) Folosește cheia **Client API**, nu cheia **Application API**. După conectarea reușită, aplicația păstrează cheia criptată în spațiul privat al telefonului, cu Android Keystore, și încearcă reconectarea automat la redeschidere. Apasă **IEȘIRE** pentru a șterge cheia salvată. Aplicația transmite cheia prin HTTPS către panoul IP-NET. Nu publica cheia și nu o include într-un issue sau într-o captură de ecran.

🇬🇧 (EN) Use a **Client API** key, not an **Application API** key. After a successful connection, the app stores the key encrypted in the phone's private storage using Android Keystore and attempts to reconnect automatically when reopened. Tap **LOG OUT** to clear the saved key. The app sends the key to the IP-NET panel over HTTPS. Do not publish the key or include it in an issue or screenshot.

🇷🇴 (RO) Consultă [nota de confidențialitate](PRIVACY.md) și [instrucțiunile de securitate](SECURITY.md).

🇬🇧 (EN) Read the [privacy notice](PRIVACY.md) and [security instructions](SECURITY.md).

## 🇷🇴 (RO) Descărcare / 🇬🇧 (EN) Download

🇷🇴 (RO) Descarcă APK-ul oficial din [GitHub Releases](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/releases). Versiunea publicată în prezent este **1.0.1**.

🇬🇧 (EN) Download the official APK from [GitHub Releases](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/releases). The current published version is **1.0.1**.

🇷🇴 (RO) Versiunea 1.0.1 este semnată Release. Aplicația păstrează cheia Client API criptată pe telefon și se reconectează la redeschidere. Actualizările publicate ulterior cu aceeași semnătură se instalează peste aplicație, fără dezinstalare.

🇬🇧 (EN) Version 1.0.1 is signed as a Release build. The app stores the Client API key encrypted on the phone and reconnects when reopened. Later updates signed with the same key can be installed over the existing app without uninstalling it.

## 🇷🇴 (RO) Compilare și publicare / 🇬🇧 (EN) Build and release

🇷🇴 (RO) Proiectul folosește JDK 17, Android SDK 35 și Gradle 8.9.

🇬🇧 (EN) The project uses JDK 17, Android SDK 35, and Gradle 8.9.

🇷🇴 (RO) Compilează local varianta Debug: / 🇬🇧 (EN) Build the Debug version locally:

```bash
🇬🇧 (EN) gradle :app:assembleDebug
```

🇷🇴 (RO) GitHub Actions compilează sursa la schimbările proiectului și oferă un APK Debug ca artefact temporar. Publicarea unui APK Release semnat se face printr-un tag Git de forma `vMAJOR.MINOR.PATCH`. Înainte de primul tag Release, configurează cheia permanentă urmând [instrucțiunile de semnare](RELEASE_SIGNING.md). Nu încărca keystore-ul sau parolele în repository.

🇬🇧 (EN) GitHub Actions builds the source when project changes are made and provides a Debug APK as a temporary artifact. A signed Release APK is published using a Git tag in the form `vMAJOR.MINOR.PATCH`. Before the first Release tag, configure the permanent signing key by following the [signing instructions](RELEASE_SIGNING.md). Do not upload the keystore or passwords to the repository.

## 🇷🇴 (RO) Licență / 🇬🇧 (EN) License

🇷🇴 (RO) Codul și aplicația sunt proprietare. Citește [LICENSE](LICENSE). Componentele terțe rămân sub licențele lor proprii.

🇬🇧 (EN) The app and its code are proprietary. Read [LICENSE](LICENSE). Third-party components remain under their respective licenses.

## 🇷🇴 (RO) Feedback și idei / 🇬🇧 (EN) Feedback and ideas

🇷🇴 (RO) Ai o sugestie sau ai găsit o problemă? Deschide un [issue nou](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/issues/new) și alege formularul potrivit. Citește [ghidul de feedback și contribuții](.github/CONTRIBUTING.md). Te rugăm să nu publici parole, chei API sau date private.

🇬🇧 (EN) Have an idea or found a problem? Open a [new issue](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/issues/new) and choose the appropriate form. Read the [feedback and contribution guide](.github/CONTRIBUTING.md). Please do not post passwords, API keys, or private information.

---

**IP-NET Hosting**  
🇷🇴 (RO) Administrare simplă. Control rapid. Identitate IP-NET.  
🇬🇧 (EN) Simple management. Fast control. The IP-NET experience.

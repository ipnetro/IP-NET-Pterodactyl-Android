# IP-NET Hosting

**Administrarea serverelor Pterodactyl, direct de pe Android.**

IP-NET Hosting este o aplicație Android nativă, construită cu Kotlin și Jetpack Compose, pentru gestionarea serverelor din panoul IP-NET.

## Identitatea aplicației

| Element | Valoare |
|---|---|
| Nume | **IP-NET Hosting** |
| Application ID | `ro.ipnet.hosting` |
| Panou | [ptero.ip-net.ro](https://ptero.ip-net.ro) |
| Android minim | 8.0 (API 26) |
| Versiune publicată | 1.0.1 |

## Funcționalități

### Administrarea serverelor

- Listarea serverelor asociate contului.
- Monitorizarea utilizării CPU, memoriei și spațiului de stocare.
- Pornirea, oprirea și repornirea serverelor.
- Informații despre server și grafice cu istoricul resurselor.

### Consolă și fișiere

- Consolă live prin WebSocket și trimiterea comenzilor.
- Navigarea prin fișiere și directoare.
- Crearea directoarelor, editarea și salvarea fișierelor.
- Redenumirea, ștergerea, arhivarea și dezarhivarea fișierelor.
- Descărcarea fișierelor.

### Instrumente suplimentare

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
gradle :app:assembleDebug
```

GitHub Actions compilează sursa la schimbările proiectului și oferă un APK Debug ca artefact temporar. Publicarea unui APK Release semnat se face printr-un tag Git de forma `vMAJOR.MINOR.PATCH`. Înainte de primul tag Release, configurează cheia permanentă urmând [instrucțiunile de semnare](RELEASE_SIGNING.md). Nu încărca keystore-ul sau parolele în repository.

## Licență

Codul și aplicația sunt proprietare. Citește [LICENSE](LICENSE). Componentele terțe rămân sub licențele lor proprii.


## Feedback și idei

Ai o sugestie sau ai găsit o problemă? Deschide un [issue nou](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/issues/new) și alege formularul potrivit. Citește [ghidul de feedback și contribuții](.github/CONTRIBUTING.md). Te rugăm să nu publici parole, chei API sau date private.

---

**IP-NET Hosting**  
Administrare simplă. Control rapid. Identitate IP-NET.

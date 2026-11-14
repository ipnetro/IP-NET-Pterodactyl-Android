# IP-NET Hosting

**Administrarea serverelor Pterodactyl, direct de pe Android.**

IP-NET Hosting este o aplicație Android nativă, construită cu Kotlin și Jetpack Compose, pentru gestionarea serverelor din panoul IP-NET.

## Identitatea aplicației

| Element | Valoare |
|---|---|
| Nume | **IP-NET Hosting** |
| Application ID | `ro.ipnet.hosting` |
| Panou | [ptero.ip-net.ro](https://ptero.ip-net.ro) |
| Interfață | Temă IP-NET negru și roșu |
| Android minim | 8.0 (API 26) |

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

Folosește cheia **Client API**, nu cheia **Application API**. Nu publica cheia și nu o transmite altor persoane. În implementarea actuală, cheia este păstrată în memoria aplicației și nu este salvată pe disc.

## Verificare și compilare

Proiectul include un workflow GitHub Actions pentru verificarea compilării sursei Kotlin.

Pentru compilare locală sunt necesare:
- JDK 17
- Android SDK 35
- Gradle 8.9

Comanda pentru generarea variantei Debug:

```bash
gradle :app:assembleDebug
```

Workflow-ul de verificare a sursei nu generează automat APK-ul.

## APK

Versiunea **1.0.0** este disponibilă în secțiunea [Releases](https://github.com/ipnetro/IP-NET-Pterodactyl-Android/releases).

---

**IP-NET Hosting**  
Administrare simplă. Control rapid. Identitate IP-NET.

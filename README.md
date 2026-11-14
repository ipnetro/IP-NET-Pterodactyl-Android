# IP-NET Hosting

Aplicație Android nativă pentru administrarea serverelor din panoul Pterodactyl IP-NET.

- Nume aplicație: **IP-NET Hosting**
- Application ID: `ro.ipnet.hosting`
- Panou: `https://ptero.ip-net.ro`
- Interfață: branding IP-NET negru / roșu
- Min SDK: Android 8.0 (API 26)

## Funcționalități implementate în cod

- Conectare cu Client API Key Pterodactyl; cheia rămâne în memoria aplicației și nu este salvată pe disc.
- Listarea serverelor contului, inclusiv paginile următoare ale răspunsului API.
- Afișarea utilizării CPU, memorie și stocare, cu actualizare periodică.
- Comenzi de pornire, oprire și restart.
- Consolă live prin WebSocket și trimiterea comenzilor.
- Informații despre server și istoric grafic al resurselor.
- Manager de fișiere: navigare, creare dosare, editare/salvare, redenumire, ștergere, arhivare/dezarhivare și descărcare.
- Administrare backup-uri și baze de date, inclusiv afișarea parolei generate la creare sau rotire când permisiunile Client API permit; creare programări cu comandă executată automat, activare/dezactivare, executare imediată și ștergere programări; vizualizare utilizatori și alocări de rețea.

## Cheia API

În panoul Pterodactyl, creează o cheie **Client API** din contul utilizatorului. Nu folosi o cheie Application API. Nu publica cheia în GitHub sau în conversații.

## Validare

Workflow-ul `.github/workflows/native-check.yml` verifică compilarea sursei Kotlin și nu generează APK. APK-ul va fi construit separat, după finalizarea și verificarea aplicației.

## Build local

Necesită JDK 17, Android SDK 35 și Gradle 8.9:
```bash
gradle :app:assembleDebug
```

Aceasta este o implementare Android nativă independentă, care folosește API-ul Pterodactyl; nu împachetează aplicația QGS și nu include cod proprietar QGS.

# IP-NET Hosting

Aplicație Android nativă pentru administrarea serverelor din panoul Pterodactyl IP-NET.

- Nume aplicație: **IP-NET Hosting**
- Application ID: `ro.ipnet.hosting`
- Panou: `https://ptero.ip-net.ro`
- Branding: negru / roșu IP-NET

## Funcționalități inițiale
- Autentificare prin Client API Key Pterodactyl
- Listarea serverelor disponibile contului
- Afișarea statusului și limitelor RAM / disk / CPU
- Comenzi start, stop și restart
- Cheia API este păstrată doar în memoria procesului, nu este salvată pe disc

## Cheie API
În panoul Pterodactyl, creează o cheie **Client API** din contul utilizatorului. Nu folosi o cheie Application API. Introdu cheia direct în aplicație și nu o publica în GitHub sau în conversații.

## Build local
Necesită JDK 17 și Gradle 8.9:
```bash
gradle assembleDebug
```
APK-ul debug va fi în `app/build/outputs/apk/debug/app-debug.apk`.

## Notă
Aceasta este o implementare independentă, bazată pe API-ul oficial Pterodactyl; nu include cod sau resurse proprietare din QGS Hosting.

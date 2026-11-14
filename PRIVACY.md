# Notă de confidențialitate — IP-NET Hosting

Ultima actualizare: 14 noiembrie 2026

IP-NET Hosting este o aplicație Android pentru administrarea serverelor din panoul Pterodactyl IP-NET.

## Ce date folosește aplicația

Aplicația nu cere crearea unui cont separat și nu include, în codul verificat, SDK-uri de publicitate sau analiză. Cheia **Client API** introdusă de utilizator este salvată criptat în spațiul privat al aplicației, cu o cheie AES păstrată în Android Keystore. Aplicația nu trimite cheia către servicii de analiză. La redeschidere, aplicația încearcă reconectarea automată. Butonul **IEȘIRE** șterge cheia criptată și cheia AES din Android Keystore.

Pentru funcțiile pe care le alegi, aplicația trimite cereri prin HTTPS către `ptero.ip-net.ro` folosind cheia Client API. Aceste cereri pot include operațiuni de server, comenzi de consolă, căi și conținut de fișiere ori setări pe care le modifici. Informațiile afișate — precum lista serverelor, starea și utilizarea resurselor — vin din panoul Pterodactyl. Consola folosește conexiunea WebSocket furnizată de panou.

## Cine primește datele

Datele necesare funcționării sunt transmise direct către panoul IP-NET și serviciile sale Pterodactyl, nu către un serviciu de analiză al aplicației. Operatorul panoului poate procesa solicitările și păstra jurnale conform propriilor reguli și obligații. Aplicația nu controlează păstrarea datelor pe server.

Ca orice serviciu online, serverele contactate pot primi informații tehnice de conexiune, cum ar fi adresa IP, ora solicitării și informații despre conexiune.

## Păstrarea și ștergerea datelor

Cheia API rămâne criptată pe telefon până la folosirea butonului **IEȘIRE** sau dezinstalarea aplicației; datele aplicației sunt excluse din backup-ul Android. Închiderea aplicației păstrează cheia pentru reconectarea automată. Aplicația nu salvează istoricul serverelor ca istoric separat; datele și jurnalele păstrate de panoul Pterodactyl sunt administrate de operatorul panoului.

## Contact

Pentru întrebări despre această notă, contactează administratorul prin [profilul GitHub IP-NET](https://github.com/ipnetro). Nu publica niciodată cheia API într-un issue public sau într-un mesaj public.

Această notă descrie comportamentul versiunii curente a aplicației și va fi actualizată dacă se schimbă modul de folosire a datelor.
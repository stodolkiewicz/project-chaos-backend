# Propozycje ulepszeń

## Co bym poprawił

1. Menu taska zostaje otwarte po zamknięciu „Task Details”, więc trzeba drugi raz wcisnąć Escape. Robi to `onSelect={(e) => e.preventDefault()}` w `TaskMenu.tsx`.
2. „Move to Done” przenosi task do archiwum, a na tablicy jest też kolumna „Done”. Użytkownik nie odróżni tych dwóch miejsc. AI też nazwało task z archiwum „Done”.
3. AI pomyliło assignee. Zapytałem o taski z priorytetem High. AI odpowiedziało, że „Setup CI/CD Pipeline” i „Security Audit” nie mają assignee, a oba go mają. Zapytanie w `CustomBoardRepository` zwraca email assignee, więc przyczyna leży gdzie indziej. Tego jeszcze nie sprawdzałem.
4. Na tablicy trzech różnych userów ma ten sam avatar „PR”, bo inicjały biorą się z emaila. Na stronie projektu ci sami userzy mają „TU”, bo tam inicjały biorą się z imienia i nazwiska.
5. Archiwum ma przycisk „+”, który tworzy task od razu w archiwum. W backlogu „+” to mała ikona w rogu, a na tablicy jest pełny przycisk „Add task”.
6. Ikony na liście projektów (oko, wyjście, kosz) nie mają podpisu ani tooltipa.
7. „Task Details” pokazuje priorytet jako zwykły tekst bez koloru. Nie widać tam labeli ani kolumny.
8. Tablica nie ma filtrów, a backlog i archiwum je mają. Nigdzie nie da się wyszukać taska po tekście.
9. Członków projektu zaprasza się tylko z menu projektu na dashboardzie. Sekcja „Team Members” nie ma takiego przycisku.
10. Strona projektu pokazuje UUID projektu, który nic userowi nie mówi.
11. W ustawieniach nie da się zmienić kolejności priorytetów. Kolejność wynika z ukrytej cyfry.

## Nowe funkcje

1. Wyszukiwanie tasków po tekście, otwierane skrótem Ctrl+K.
2. Termin taska (due date) z wyróżnieniem po terminie. `TaskMenu.tsx` importuje już ikonę `Calendar`, ale jej nie używa.
3. Filtr „My tasks”, który pokazuje tylko moje taski.
4. Wzmianki @user w komentarzach z powiadomieniem.
5. Checklista w tasku, czyli lista podzadań do odhaczania.
6. Historia zmian taska, która pokazuje, kto co zmienił i kiedy.
7. AI, które przenosi taski i robi podsumowanie dnia, np. co ruszyło się od wczoraj.
8. Akcje na wielu taskach naraz. Endpointy przenoszenia już przyjmują listę `taskIds`.
9. Czat między członkami projektu, obok czatu z AI.

## Czasy tasków i statystyki

### Pomysł Dawida

- Domyślnie ostatnia kolumna to „Done”, a ADMIN może to zmienić.
- Przesunięcie taska do „Done” zapisuje czas ukończenia.
- Aplikacja zapisuje też czas dodania taska na board (domyślnie pierwsza kolumna) i czas rozpoczęcia pracy (domyślnie druga kolumna).
- Czasy służą do filtrowania w archiwum i do wykresów, np. średni czas życia taska i średni czas od wzięcia do ukończenia.

To dwie standardowe metryki Kanbana:
- **lead time** to czas od dodania taska na board do jego ukończenia;
- **cycle time** to czas od rozpoczęcia pracy do ukończenia.

### Zmiany w koncepcji

1. Czasy nie zależą od pozycji kolumny. ADMIN oznacza w Settings jedną kolumnę jako „start”, a drugą jako „done”. Domyślnie „start” to druga kolumna, a „done” to ostatnia. Wtedy przesunięcie albo dodanie kolumny nie psuje pomiaru.
2. Czas dodania na board zapisuje się przy przejściu taska z backlogu na board, niezależnie od kolumny. Task może trafić od razu do drugiej kolumny, a i tak liczy się moment wejścia na board.
3. Archiwum też zapisuje czas ukończenia, jeśli task go jeszcze nie ma. Dziś „Move to Done” przenosi task prosto do archiwum, z pominięciem kolumny „Done”. Bez tego część ukończonych tasków nie miałaby czasu.
4. Gdy task wraca z „done” do wcześniejszej kolumny, czas ukończenia się czyści. Czas startu zostaje pierwszy, bo praca faktycznie wtedy się zaczęła.

### Zapis

Tabela `tasks` dostaje trzy kolumny: `board_entered_at`, `started_at` i `completed_at`. Wystarczą do filtrów w archiwum i do średnich. Zapytania pozostaną proste.

Ten zapis nie pokaże, ile task siedział w każdej kolumnie. Do tego potrzebna jest tabela historii przejść, czyli wiersz z kolumną źródłową, kolumną docelową i czasem przy każdym przesunięciu. Taka tabela pokryłaby też punkt „historia zmian taska” z nowych funkcji. Warto ją dodać dopiero wtedy, gdy będą potrzebne takie wykresy.

### Wykresy

1. Wykresy pokazują medianę, czyli wartość środkową, zamiast średniej. Jeden task wiszący 3 miesiące zawyża średnią, a mediany nie rusza.
2. Dochodzi **throughput**, czyli liczba ukończonych tasków na tydzień. To najprostszy wykres, który pokazuje tempo zespołu.

### Ograniczenie

Istniejące taski nie mają historii, więc wykresy zaczną się od dnia wdrożenia. Uzupełnić da się tylko czas utworzenia taska, bo baza już go ma.

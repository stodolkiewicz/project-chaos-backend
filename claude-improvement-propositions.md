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

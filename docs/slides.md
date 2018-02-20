name: title
class: middle, center, inverse

# Vorstellung der Graph-Engine
Graph and Model Driven Engineering, Universität Kassel<br>
**Niklas Mollenhauer** &lt;redacted@uni-kassel.de&gt;

---
layout: true
.footer[Niklas Mollenhauer, G&MDE WS1718, 20. Februar 2018]

---
name: agenda

# Agenda
1. Unterschiede zur Vorlesung
1. Feature-Details
1. Tests
1. Demo

<!--
1. Feature-Überblick

# Feature-Überblick (1/2)
- API für Graphen und Nodes
- JSON-(De-)Serialisierung
- Pattern-Matching
- Regelausführung mit Prioritäten
- Graphisomorphie-Erkennung

-

# Feature-Überblick (2/2)
- Graph-Querys
- Graph-Statistiken
- Inferenz von Klassenmodellen
- Modell-Transformationen
- Visualisierungen
-->

---
## Unterschiede zur Vorlesung
- Verwendung von Java 8-Features:
	- Streams
	- `Optional<T>` für weniger NPEs

--
- Vermeidung von Seiteneffekten
	- Soll Wartbarkeit erhöhen

--
- Vorranging unveränderliche Datentypen
	- Threading-Synchronisationsaufwand entfällt
	- ➜ Einfacherere parallalelisierung (wenn später gewünscht)

--
- Mehr Parametervalidierung, `assert <expr>;` zur Fehlerfindung

--
- Begriffe: "Pattern" statt "Linke Regelseite"

???
- Streams: Einfacher
- `Optional<T>` zwingt dazu, sich Gedanken über nicht vorhandene Werte zu machen

---
## API für Graphen und Nodes
- YAGE-Graph
	- Mit Konvention für Label-Attribute

--
- Klassen:
	- `Node`
	- `Graph`

--
		- erbt von `Node`
			- ➜ States in LTS-Graphen können wie Nodes behandelt werden
			- ➜ Algorithmen können wiederverwendet werden

???
- Konvention: Label-Attrs immer "label", Konstante

---
## JSON-(De-)Serialisierung
- Verwendet Google GSON
	- Performante (De-)Serialisierung
	- Konfigurierbar für Verhalten bei Referenz-Zyklen
	- Mit und ohne Einrückung

--
- Unterstützt (De-)Serialisierung aus/in
	- Datei: `GraphSerializer.toJsonFile(Graph, Path)`
	- String: `GraphSerializer.toJson(Graph)`

--
- Unit Tests: Graph-Isomorphie für Asserts

???
- Wird in Demo gezeigt; JSON-Ausschnitt
- **DEMO**: `1`

---
## Pattern-Matching
- Sucht nach Mustern (Pattern) in einem Host-Graph

--
- Implementiert ähnlich wie in der Vorlesung:
	- `Rule`-Klasse, unveränderlicher Typ

???
- Wie in VL, bis auf einige Unterschiede (`Optional<T>`, Stream)
	- `Optional<SearchOperation> getNextSearchOperation()`
	- Übergabe von Pattern und Operation über Super-Constructor

--
	- Such-Status ist nur innerhalb der Funktion `findMatches(Graph)`
		- Thread-Safe, da kein objektweiter veränderbarer State
		- `findMatches` einer `Rule`-Instanz parallel möglich

--
	- RuleOperation-Func.-Interface hat mehr Parameter:
		- `Rule source, Graph host, Map<Node, Node> matches`
		- ➜ Benötigt keinen äußeren Scope

--
	- Full-Match-Mechanismus ➜ Isomorphietest

???
- Implementierung von Regeln möglich via:
	- `TransferRule extends Rule`
- **DEMO**: `2`

---
## Regelausführung mit Prioritäten
- Erzeugt Reachability Graph (Labelled Transition System, LTS)

--
- Mehrere Startgraphen möglich
	- Isomorphe Startgraphen werden herausgefiltert

--
- Unterstützt zusätzlich Prioritäten, ähnlich zu *Groove*
	- Priorität ist ein `Integer`
		- `0`: Höchste Priorität

--
	- Implementierung über eine sortierte Map:
		- `SortedMap<Integer, Set<Rule>>`

???
- sortierte Map: Schon bei Java vorhanden
- Einfach über die Map iterieren

--
	- Sobald eine Regel einer Prio.-Stufe greift:
		- Nur Regeln dieser Stufe können angewendet werden

--
- Regel ohne Operation ➜ Kante zu sich selbst

???
- Komme zum selben LTS wie Groove
- Mehr dazu in der Demo
- **DEMO**: `3`

---
## Graphisomorphie-Erkennung
- Zertifikatsmechanismus
	- Wie in Vorlesung gezeigt
	- `String`, einfache Implementierung

???
- Für LTS notwendig, da sonst Endlosschleife

--
- Für LTS-Erstellung:
	- `Map<String, Set<Graph>>`
	- Performanceoptimierung, evtl. keine volle Matchprüfung notwendig

--
- Bei Kollisionen:
	- Matching wird wiederverwendet
	- Auf den kompletten Graph mit eigener Regel (`fullMatch`)

???
- **DEMO**: `4`

---
## Graph-Querys
- Unterstützte Anfragen:
	- Always: Globally, Until, Next
	- Exist: Globally, Until, Next

???
- Always/Exist Next nicht gefordert

--
- Liefern einen `Optional<LtsPath>`
	- `LtsPath` ist ein Pfad innerhalb des LTS
		- Enthält Nodes und Übergänge mit Edge-Label
		- Kann visualisiert werden

--
	- Je nachdem ob *Always* oder *Exists* ein (Gegen)Beispiel
		- `boolean success` wäre nicht immer eindeutig

???
- Kein `success`, da nicht eindeutig, was genau gemeint ist
- **DEMO**: `5`

---
## Graph-Statistiken
- Durchschnittliche Anzahl ausgehender Kanten
	- Für alle Auftreten von Attribut-Wert-Paaren

???
- Wie in der Hausaufgabe
- Könnte man verwenden, um Graphsuchen zu optimieren

--
- Intern eine mehrfach verschaltete Map:
	- Attributname ➜ (Attributwert ➜ (Edgelabel ➜ avg(vorkommen)))
	- Gekapselt in einer Klasse

--
- `.toString()`:
```
name=Yin Yang: avg(at)=1.0
length=100m: avg(mounted-on)=2.0
duration=10min: avg(at)=1.0
label=expendable: avg(at)=1.0
label=expendable: avg(has)=0.25
```

???
- Weiterhin unbenutzt
- **DEMO**: `6`

---
## Inferenz von Klassenmodellen
- Spezielle Klassenmodelle aus generischen generieren:
	- `Node[label="Person", name="Peter"]` ➜ `class Person(name: String)`
	- Unterstützt verschiedene Kardinalitäten
	- "Type-Widening", definiert über eine Funktion: `(byte, int) -> int`

--
- Verzichtet komplett auf `java.lang.reflect.*`
- Verwendet SDMLib als Backend
	- Setzen von Klassen-Attributen mit SDMLib-Mechanismen
	- `SendableEntity(Creator)`

???
- Kein `java.lang.reflect` ➜ Einheitlich, stabil für SDMLib-Modelle

--
- Konvertiert Casing: `attribute-name` ➜ `attributeName`
	- Verwendet Google Guava

???
- Google Guava ➜ Verlässlich, gut getestet
- Casing-Rückrichtung funktioniert auch
- **DEMO**: `7`

---
## Modell-Transformationen
- Verwendet SDMLib-Klassenmodelle

--
- Überführt: `speziell ➜ generisch ➜ generisch ➜ speziell`
	- Bsp.: `Family ➜ Graph ➜ Graph ➜ PersonRegister`

???
- Unterschied zu VL: `Family ➜ Graph ➜ PersonRegister`

--
	- Der 1. generische Graph wird nicht verändert
	- Der 2. wird komplett neu erstellt
		- Eingangsmodell geändert ➜ keine unerwünschten Nebeneffekte
		- Der erste Graph könnte gleichzeitig in mehreren Transformationen verwendet werden

???
- Graphen werden neu erstellt: Falls Sachen hinzukommen, treten sie nicht plötzlich wo anders auf
- Demo: Roundrip mit Family -> PersonRegister -> Family
- **DEMO**: `8`
- **DEMO**: `9`

---
## Visualisierungen
- Setzt auf Graphviz
	- Einfach, gute Java-API
	- Export nach SVG, PNG

???
- Theoretisch mehr Formate möglich, aber auf die beschränkt

--
- Implementiert für:
	- Einzelne Graphen
	- Matches (Pattern + Host-Graph)
	- Erreichbarkeitsgraphen (LTS) mit Menge an Start-Graphen
	- Pfaden (im LTS)

???
- Wird in der Demo mit den einzelnen Features gezeigt

---
## Tests
- Verschiedene Szenarien, u. A.:
	- Kannibalenproblem
	- Wolf-Ziege-Kohlkopf-Problem
	- Simpson-Familie
- CI mit automatischen Builds + Tests (travis-ci.com)
- Code-Coverage: ca. 85%

---
class: middle, center, inverse
# Demo

Folien und Quelltexte:<br>https://github.com/nikeee/graph-engine

???
- Demo: Baubar mit Gradle: `./gradlew test`
- Wenn Ihr was bestimmtes sehen wollt -> sagen
- Folien erstellt mit Remark, NProgress und etwas JS/CSS/HTML
- Ausblick
	- Folgen könnte:
		- Refactorings (`LtsPath` -> `Path` )
		- Parallelisirung von Algorithmen

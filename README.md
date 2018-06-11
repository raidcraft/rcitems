# Custom Items Plugin & API

Das Custom Items Plugin wurde entwickelt um Spielern mehr Möglichkeiten zu geben mit der Welt und Items zu interagieren.

- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Authors](#authors)

## Getting Started

* [Project Details](https://git.faldoria.de/raidcraft/rcitems)
* [Source Code](https://git.faldoria.de/raidcraft/rcitems/tree/master)
* [Latest Stable Download](https://ci.faldoria.de/view/RaidCraft/job/RCItems/lastStableBuild)
* [Issue Tracker](https://git.faldoria.de/raidcraft/rcitems/issues)
* [Developer Documentation](docs/DEVELOPER.md)
* [Admin Documentation](docs/ADMIN.md)

### Prerequisites

Das `RCItems` Plugin ist nur von der [RaidCraft API](https://git.faldoria.de/raidcraft/raidcraft-api) abhängig und benötigt eine Verbindung zu einer MySQL Datenbank.

Optional: [Webinterface](https://git.faldoria.de/raidcraft/rcwebinterface) zur Erstellung und Verwaltung der Items.

### Installation

Beim ersten Start des Servers wird eine `database.yml` und eine `config.yml` angelegt. Am besten den Server direkt nochmal stoppen und die Angaben in der `database.yml` bearbeiten.

Die `config.yml` enthält folgende defaults:

```yml
defaults: {
    # hier können vanilla minecraft items mit custom items ersetzt werden
    wool: 1234 # datenbank id des custom items
}
# Chance in % ob das Item bei Benutztung Haltbarkeit verliert, z.B. 0.01 -> 1%
durability-loss-chance-on-use: 0.01
# Chance in % ob das Item Haltbarkeit verliert wenn der Spieler Schaden nimmt
durability-loss-chance-on-damage: 0.0001
# Steuert ob das Item Level angezeigt wird
hide-item-level: true
# Intervall in Sekunden wie oft eine Warnung angezeigt werden soll, wenn ein Spieler versucht ein seelengebundenes Item von einem anderen Spieler aufzuheben.
soulbound-item-warn-interval: 60.0
```

## Authors

* [**Michael (Silthus) Reichenbach**](https://git.faldoria.de/Silthus)

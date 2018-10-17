# RCItems Admin Documentation

Hier finden Server Admins alle wichtigen Informationen um das [RCItems](../README.md) Plugin zu konfigurieren und zu nutzen. Es wird beschrieben wie man Custom Items und Crafting Rezepte erstellt.

* [Commands](#commands)
    * [Item Commands](#item-commands)
    * [Custom Recipe Commands](#custom-recipe-commands)
    * [Book Commands](#book-commands)
    * [Storage Commands](#storage-commands)
    * [Lore Commands](#lore-commands)
* [Custom Items](#custom-items)
    * [Webinterface](#webinterface)
    * [Config Dateien](#config-dateien)
    * [Alias Items](#alias-items)
* [Item Categories and Types](#item-categories-and-types)
    * [Item Type](#item-type)
    * [Item Quality](#item-quality)
    * [Equipment Slot](#equipment-slot)
    * [Weapon Type](#weapon-type)
    * [Armor Type](#armor-type)
* [Item Level](#item-level)
* [Custom Crafting Rezepte](#custom-crafting-rezepte)

## Commands

### Item Commands

> Alle der folgenden Befehle beginnen mit `/rci`.

| Command | Arguments | Flags | Description | Permission |
| ------- | ----- | --------- | ----------- | ---------- |
| help    |       |           | Zeigt die Hilfe an | |
| reload  |       |           | Lädt alle Configs und Items neu. | `rcitems.reload` |
| give   | `<item_name>`  | -p `<player_name>`<br> -a `<item_amount>` | Gibt sich selbst oder einem anderen Spieler das entsprechende Custom Item mit der ID oder dem Namen. | `rcitems.give` |
| info |  | | Zeigt detailierte Informationen über das Item in der Hand an. | `rcitems.info` |
| fakeskull | `<url>`| | Erzeugt einen Totenkopf mit dem Skin aus der Web URL. | `rcitems.fakeskull` |

### Custom Recipe Commands

> Alle der folgenden Befehle beginnen mit `/recipe` oder `/rezept`.

| Command | Arguments | Flags | Description | Permission |
| ------- | ----- | --------- | ----------- | ---------- |
| help    |       |           | Zeigt die Hilfe an | |
| reload    |   |   | Lädt alle Crafting Rezepte neu. | `rcitems.reload` |
| create | `<displayName>` `<SHAPED|SHAPELESS|FURNACE>` | -p | Erstellt aus speziell angeordneten Items im Inventar ein [Custom Crafting Rezept](#custom-crafting-rezepte). Mit dem Flag `-p` wird festgelegt, dass man das Rezept nur mit der entsprechenden Permission (`rcitems.recipes.<recipe-displayName>`) craften kann. | `rcitems.recipe.create` |
| delete | `<displayName>` | | Löscht das angegebene Rezept aus der Datenbank. | `rcitems.recipe.remove` |

### Book Commands

> Mit `/book autor <displayName>` lässt sich der Autor von Büchern ändern die man in der Hand hält.

### Storage Commands

Mit den RCItems Storage Commands kann man Items 1:1 in die Datenbank speichern um sie dann z.B. in Quests zu verwenden. Dabei werden alle Werte des Items serialisiert, d.h. auch z.B. geschriebene Buch Seiten.

> Alle der folgenden Befehle beginnen mit `/storage`.

| Command | Arguments | Flags | Description | Permission |
| ------- | ----- | --------- | ----------- | ---------- |
| save    |     |   | Speichert das aktuelle Item in der Datenbank. | `rcitems.store` |
| delete | `<stored_item>` | | Löscht das angegebene Item aus der Datenbank. | `rcitems.delete` |
| give | `<stored_item>` | | Gibt dem ausführendem Spieler das gespeicherte Item. | `rcitems.give` |

### Lore Commands

Mit den `Lore` oder auch `Legende` Commands lassen sich die Beschreibungen von Custom Items anpassen. Damit kann man z.B. ein Quest Item mit einem coolen Text belegen.

> Alle der folgenden Befehle beginnen mit `/lore`.

| Command | Arguments | Flags | Description | Permission |
| ------- | ----- | --------- | ----------- | ---------- |
| addline | `<text>` | | Fügt dem Item eine Zeile mit dem Text hinzu. | `rcitems.lore` |
| remove | | | Löscht sämtliche Lore von dem Item. | `rcitems.lore` |
| displayName | `<displayName>` | | Gibt dem Item einen speziellen Namen | `rcitems.lore` |

## Custom Items

Jedes Custom Item das im Spiel im Umlauf ist ist einzigartig. Das bedeutet, dass Änderungen, wie z.B. das Anpassen der Lore, nur auf dem aktuellen Item angewandt werden. Will man das Item duplizieren muss es mit dem [`/storage store`](#storage-commands) Command in der Datenbank gespeichert werden.

> Alle konfigurierten Custom Items in der Datenbank und Dateien sind Blaupausen für das Erstellen von einzigartigen Custom Items.

### Webinterface

Am leichtesten ist es die Custom Items über das bereitgestellte [Webinterface](https://admin.faldoria.de/items) zu erstellen und anschließend mit `/rcitems reload` im Spiel zu laden.

### Config Dateien

Items können auch über Config Dateien angelegt werden. Das ist besonders für Quests nützlich. So hat man alles für eine Quest zentral an einem Ort.

> Item Configs können entweder im Quest Ordner mit der Endung `.item.yml` oder im Plugin Order unterhalb von `custom-items/` erstellt werden.

Folgendes ist ein Beispiel Item mit allen möglichen Eigenschaften. Alle Eigenschaften bis auf der Name und das Minecraft Vanilla Item sind optional und werden von dem spezifizierten Minecraft Item übernommen.

```yml
# der Name des Items wie ihn der Spieler zu sehen bekommt
displayName: Heu Lieferung
# der Item Type (siehe unten)
type: QUEST
# die Qualität des Items (siehe unten)
quality: COMMON
# die ID des Minecraft Items welches benutzt werden soll
item: HAY_BLOCK
# eine in goldener Schrift angezeigte Beschreibung
lore: Eine Lieferung Heu für den Pferdeknecht von Bravaria
# extra Item Data die von manchen Minecraft Items verwendet werden kann
item-data: 0
# die maximale Stack Größe für dieses item
max-stack-size: 64
# wird normalerweise automatisch berechnet, kann hier aber überschrieben werden
item-level: 1337
# der Verkaufspreis des Items
price: "1g22s3k"
# die Bindung des Items (siehe unten)
bind-type: SOULBOUND
# blockiert die Ausführung der Standard Vanilla Aktionen
block-usage: false
# lässt das Item verzaubert ausschauen
enchantment-effect: false
```

### Alias Items

Zusätzlich zu einer *normalen* Item Config kann man eine Alias Config anlegen die nur dafür da ist um auf eine Item ID in der Datenbank zu referenzieren. Das Item kann dann in der [Quest Config](https://git.faldoria.de/tof/plugin-configs/quests/blob/develop/docs/QUEST-CONFIG.md) anstatt der Id mit dem Alias Namen referenziert werden.

`foobar.item.yml`

```yml
id: 123
```

```yml
requirements:
  flow:
    - '?player.has-item this.foobar'
```

Damit bei einer Quest mit vielen Items nicht ein Dateien Wildwuchs entsteht, gibt es die Möglichkeit mehrere Item Aliase in einer `.items.yml` Datei zusammen zu fassen.

`foobar.items.yml`

```yml
mein-item: 123
zweites-item: 456
drittes-item: 111
```

Dabei können diese Items dann in einer Quest direkt mit dem Namen aus der Datei referenziert werden, so wenn es eigenständige `.item.yml` Dateien wären. Allerdings muss der Name der Datei vorne angehängt werden.

```yml
requirements:
  flow:
    - '?player.has-item this.foobar.mein-item'
```

## Item Categories and Types

Jedes Item kann verschiedene Eigenschaften annehmen und ist immer von einem bestimmten Typ der bestimmte Standardwerte für Eigenschaften vorgibt. Im folgenden wird beschrieben was es für Kategorien und Eigenschaften gibt.

### Item Type

> Jedes Item kann immer nur von einem Typ sein. Je nach Typ erhält das Item Zugriff auf bestimmte Eigenschaften, wie z.B. einen Rüstungswert.

| Item Type | Beschreibung | Eigenschaften |
| --------- | ------------ | ------------- |
| EQUIPMENT | Der Grund Typ für alle ausrüstbaren Items. | - [EquipmentSlot](#equipment-slot)<br>- Haltbarkeit |
| WEAPON    | Mit dem Item kann angegriffen werden | - [WeaponType](weapon-type)<br>- Angriffszeit<br>- Min. Damage<br>- Max. Damage |
| ARMOR | Schützt vor Angriffen | - [ArmorType](armor-type)<br>- Rüstungswert |
| USEABLE | Das Item kann benutzt werden. | - z.B. Ausführung eines Skills |
| QUEST | Ein Quest Item, dass im Quest Inventar landet. | |
| ENCHANTMENT | Eine Verzauberung die auf Items angewandt werden kann. | |
| GEM | Ein Edelstein, der in Sockel angebracht werden kann. | |
| ENHANCEMENT | Eine Verbesserung für den Spieler, z.B. ein Kleinod. | |
| CRAFTING | Ein Item was nur im [Custom Crafting](#custom-crafting) Verwendung findet. | |
| CONSUMEABLE | Ein benutztbares Item was aufgebraucht wird | |
| TRASH | Für nichts zu gebrauchen, außer zum Verkaufen. | |
| SPECIAL | Ein spezial Item. | |
| PROFESSION | Ein Item für einen bestimmten Beruf. | |
| CLASS | Ein Item was für eine bestimmte Klasse. | |
| UNDEFINED | Default Typ für alles andere. | |

### Item Quality

### Equipment Slot

### Weapon Type

### Armor Type

## Item Level

Basierend auf dem [Item Typ](#item-type), [Equipment Slot](#equipment-slot), [Qualität](#item-quality) und den [Attributen](#attributes) eines Items wird das Item Level berechnet. Das Item Level dient als Referenz für die Stärke eines Items und kann in [Loot-Tabellen](https://git.faldoria.de/tof/plugins/raidcraft/rcloot/blob/master/docs/ADMIN.md) zur Einschränkung von Drops genutzt werden.

Das Item Level wird wie folgt berechnet:

```math
Item Level = ((([(StatValue[1]*StatMod[1])^(1.7095) + (StatValue[2]*StatMod[2])^(1.7095) + ...]^(1/1.7095) / Slot Modifier ) * Quality Multiplier) + Quality Modifier
```

## Custom Crafting Rezepte
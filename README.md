# DrogaPlugin - Minecraft Drug System

![Version](https://img.shields.io/badge/version-1.0.0-blue)
[![Download](https://img.shields.io/badge/Download-JAR-blue)](https://github.com/youness998/Droga-systems/releases/latest)

Un plugin Minecraft che simula un sistema di traffico illegale con raccolta, consumo e vendita di marijuana in aree protette.

## Descrizione

DrogaPlugin è un plugin per server Minecraft che aggiunge un sistema di economia illegale basato sulla marijuana. I giocatori possono raccogliere erba dalle piante di Large Fern, consumarla per ottenere effetti potenzianti, e venderla in zone specifiche controllate dagli amministratori.

## Caratteristiche

### Sistema di Raccolta
- Raccogli marijuana dalle **Large Fern** (felci grandi)
- Processo di raccolta di 10 secondi con barra di progresso
- Quantità casuale tra 2-5 grammi per raccolta
- Controllo distanza dalla pianta durante la raccolta

### Sistema di Consumo
- Effetti positivi: Velocità e Salto potenziati
- Sistema di overdose: troppa droga causa nausea e cecità
- Limite di sicurezza: 3 consumazioni prima dell'overdose
- Cooldown automatico degli effetti

### Sistema di Vendita
- Zone di vendita create dagli amministratori
- Processo di vendita di 60 secondi con negoziazione
- Prezzo fisso: $15 per grammo
- Integrazione con economia del server

### Sistema di Sicurezza
- Solo gli OP possono creare zone di vendita
- Controllo automatico della posizione durante le transazioni
- Messaggi di errore per azioni non autorizzate

## Installazione

1. Compila il plugin:
   ```bash
   mvn clean package
   ```

2. Copia il file JAR generato in `target/DrogaPlugin-1.0.0.jar` nella cartella `plugins/` del tuo server Minecraft

3. Riavvia il server

## Comandi

### Comandi per Giocatori

| Comando | Descrizione |
|---------|-------------|
| `/droga` | Mostra il menu principale |
| `/droga raccogli` | Inizia la raccolta di marijuana |
| `/droga vendi` | Vendi marijuana in una zona autorizzata |
| `/droga info` | Mostra le tue statistiche |
| `/droga help` | Guida dettagliata |

### Comandi per Amministratori

| Comando | Descrizione |
|---------|-------------|
| `/droga claim venditore <x1> <y1> <z1> <x2> <y2> <z2>` | Crea zona di vendita |
| `/droga unclaim` | Rimuove la zona di vendita |

## Configurazione

Il plugin utilizza i file di configurazione in `src/main/resources/`:

- **config.yml**: Personalizza messaggi, tempi di raccolta, effetti e permessi.
- **plugin.yml**: Configurazione base del plugin con comandi e permessi.

## Come Giocare

1. Trova una Large Fern nel mondo
2. Clicca destro sulla pianta o usa `/droga raccogli`
3. Attendi 10 secondi senza allontanarti
4. Raccogli la marijuana
5. Vai in una zona di vendita
6. Usa `/droga vendi`
7. Attendi 60 secondi per completare la vendita

## Avvertenze

- Non allontanarti durante la raccolta o la vendita
- Rischio overdose: massimo 3 consumazioni
- Puoi vendere solo in aree autorizzate
- Questo sistema simula attività illegali in modo fittizio

## Requisiti Tecnici

- **Minecraft**: 1.21+
- **Spigot/Paper**: 1.21.1+
- **Java**: 17+
- **Maven**: Per la compilazione
- **Plugin Economia**: Vault o EssentialsX

## Statistiche Plugin

- **Linguaggio**: Java
- **Versione**: 1.0.0
- **Licenza**: Uso educativo
- **Dipendenze**: Spigot API 1.21.1

## Sviluppo

### Struttura del Progetto

```
DrogaPlugin/
├── src/main/java/Main.java          # Classe principale
├── src/main/resources/
│   ├── plugin.yml                   # Configurazione plugin
│   └── config.yml                   # Configurazioni personalizzabili
├── pom.xml                          # Dipendenze Maven
└── README.md                        # Documentazione
```

### Compilazione

```bash
mvn clean compile   # Solo compilazione
mvn clean package   # Crea il JAR finale
```

## Contributi

Questo plugin è stato creato per scopi educativi e di intrattenimento. Non promuove l'uso di sostanze illegali nella vita reale.

## Note

- La marijuana è rappresentata da Large Fern
- L’item droga è un Beetroot con metadati personalizzati
- Include effetti audio/visivi per il gameplay
- Barre di progresso e messaggi per un’esperienza realistica

---

*Plugin sviluppato per Minecraft Server - Versione 1.0.0*

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

- **Build**: `./gradlew build` - Creates the shadow JAR (final artifact with shaded dependencies)
- **Run test server**: `./gradlew runServer` - Downloads Paper/dependencies and starts a test server
- **Run Folia server**: `./gradlew runPaperFolia` - For Folia-specific testing
- **Clean**: `./gradlew clean` - Remove build artifacts

The project uses Kotlin 2.3.10 and Java 21 (toolchain requirement).

## Project Architecture

### Multi-Module Structure

- **`api/`**: Java module containing public interfaces
  - `DearU` - Main plugin interface
  - `Mailbox` - Mailbox interface with async operations (`CompletableFuture`)
  - `Mail` (sealed) - Base interface for `SingleMail` and `PackageMail`
  - `MailSender` - Represents who sent the mail
  - `MailboxManager` - Manages mailbox instances

- **`core/`**: Kotlin module containing implementations
  - `DearUPlugin` - Main plugin class
  - `DearUConfiguration` - Nested class-based config using ConfigLib
  - `Managerial` pattern - Lifecycle interface for managers (`start(Context)`, `end(Context)`)
  - Managers: `DatabaseManager`, `MailboxManagerImpl`

### Managerial Pattern

All managers implement `Managerial` and receive a `Context` object containing plugin and config references. Managers are initialized in `DearUPlugin.onEnable()` and shutdown in `onDisable()`.

```kotlin
private val managers = listOf(DatabaseManager, MailboxManagerImpl)
managers.forEach { it.start(Context(this, dearUConfig)) }
```

### Database Abstraction

The project uses `DatabaseLib` (compile-only dependency) which provides a unified interface for SQL operations. Supported databases:

- **SQLite** - Default, uses `database.db` in plugin data folder
- **MySQL** - Configured via `DearUConfiguration.database.sql.mysql.*`

Database selection is via enum `DatabaseManager.SQLDatabaseType`.

### Mail System

Mails are stored as JSON in the database with two types:
- `SingleMail` - Contains a single item
- `PackageMail` - Contains multiple items via `PackageMail.Body` builder

The `Mail` interface uses reflection-based deserialization via `Mail.deserialize()`.

### Configuration System

Configuration uses nested inner classes with `ConfigLib`:
- Primitive fields via `createPrimitiveField(key, defaultValue)`
- Enum fields via `createExtendedComplexField { EnumField(...) }`

### GUI System

`MailboxGui` implements `InventoryHolder` and registers as a `Listener`. Uses persistent data containers (`MAIL_ID_KEY`) to track mail items in the inventory.

## External Dependencies (Compile-Only)

These libraries are provided by the server/environment:

- **ConfigLib** (`com.github.bindglam:ConfigLib:1.0.0`) - Configuration system
- **DatabaseLib** (`com.github.bindglam:DatabaseLib:1.0.4`) - Database abstraction
- **semver4j** (`org.semver4j:semver4j:6.0.0`) - Version comparison

The project also depends on **Vault** (via modrinth plugin download) for economy integration.

## Shadow Plugin

The shadow task relocates Kotlin dependencies to `com.bindglam.dearu.shaded.kotlin` and excludes JetBrains annotations.

## Build Conventions

Gradle conventions are defined in `buildSrc/src/main/kotlin/`:
- `standard-conventions.gradle.kts` - Java + Kotlin setup, UTF-8 encoding, Java 21 toolchain
- `paper-conventions.gradle.kts` - Paper API dependency, plugin.yml generation via `resourceFactory-paper`

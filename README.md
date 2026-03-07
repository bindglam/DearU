<div align="center">

# 📬 DearU

### A modern mailbox plugin for Paper/Folia servers

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/Paper-API-green?style=flat-square&logo=papermc)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

</div>

## ✨ Features

- 📮 **Mailbox System** - Players can receive and manage their mail through a beautiful GUI
- 📦 **Package Support** - Send single items or multi-item packages to players
- 💾 **Database Abstraction** - Built-in support for SQLite and MySQL
- ⚡ **Async Operations** - Non-blocking mail operations using `CompletableFuture`
- 🎨 **Modern API** - Clean Java interface for plugin developers
- 🛠️ **Command Aliases** - Customizable command aliases

## 📁 Project Structure

```
DearU/
├── api/                    # Java module with public interfaces
│   └── DearU              # Main plugin interface
│   ├── Mailbox            # Async mailbox operations
│   ├── Mail               # Sealed interface for mail types
│   │   ├── SingleMail     # Single item mail
│   │   └── PackageMail    # Multi-item package
│   ├── MailSender         # Mail sender representation
│   └── MailboxManager     # Mailbox instance manager
│
└── core/                   # Kotlin module with implementations
    └── DearUPlugin        # Main plugin class
    ├── DearUConfiguration # Config system
    ├── DatabaseManager    # Database abstraction
    ├── MailboxManagerImpl # Mailbox implementation
    └── MailboxGui         # GUI implementation
```

## 🚀 Getting Started

### Requirements

- **Java 21** or higher
- **Paper/Folia** server (latest version)

### Installation

1. Build the plugin:
   ```bash
   ./gradlew build
   ```

2. Find the JAR in `core/build/libs/`

3. Place it in your server's `plugins/` folder

4. Restart your server

### Configuration

Edit `plugins/DearU/config.yml` to customize:

```yaml
# Database settings (SQLite/MySQL)
database:
  sql:
    type: SQLITE  # or MYSQL
    mysql:
      host: localhost
      port: 3306
      database: dearu
      username: root
      password: password

# Command aliases
commands:
  mailbox:
    aliases:
      - mail
      - 우편함
```

## 💻 Development

### Build Commands

```bash
# Build the project
./gradlew build

# Run test server (Paper)
./gradlew runServer

# Run Folia test server
./gradlew runFolia

# Clean build artifacts
./gradlew clean
```

### API Usage

#### Sending Mail

```java
import com.bindglam.dearu.DearU;
import com.bindglam.dearu.mail.*;

// Get plugin instance
DearU dearU = DearUProvider.get();

// Send single item mail
Mail singleMail = Mail.single(
    MailSender.server(),
    itemStack,
    "Welcome to our server!"
);
dearu.mailboxManager().getMailbox(playerUUID)
    .putMail(singleMail);

// Send package mail
PackageMail.Body body = PackageMail.bodyBuilder()
    .name("Welcome Package")
    .content(itemStack1)
    .content(itemStack2)
    .build();

Mail packageMail = Mail.packaged(
    MailSender.player(senderUUID),
    body,
    "Here's your starter kit!"
);
dearu.mailboxManager().getMailbox(playerUUID)
    .putMail(packageMail);
```

#### Reading Mail

```java
import com.bindglam.dearu.Mailbox;
import java.util.concurrent.CompletableFuture;

Mailbox mailbox = dearU.mailboxManager().getMailbox(playerUUID);

// Get all mail (async)
CompletableFuture<List<Mailbox.IdentifiedMail>> future = mailbox.mails();
future.thenAccept(mails -> {
    for (Mailbox.IdentifiedMail identified : mails) {
        Mail mail = identified.mail();
        // Process mail...
    }
});

// Get specific mail by ID
CompletableFuture<Mailbox.IdentifiedMail> mailFuture = mailbox.mail(mailId);
mailFuture.thenAccept(identified -> {
    if (identified != null) {
        Mail mail = identified.mail();
        // Process mail...
    }
});
```

## 🏗️ Architecture

### Managerial Pattern

All managers implement the `Managerial` interface:

```kotlin
interface Managerial {
    fun start(context: Context)
    fun end(context: Context)
}
```

Managers are initialized in plugin lifecycle:
```kotlin
val managers = listOf(DatabaseManager, MailboxManagerImpl)
managers.forEach { it.start(Context(this, dearUConfig)) }
```

### Database Abstraction

Uses `DatabaseLib` for unified SQL operations:

- **SQLite** - Default, uses `database.db` in plugin data folder
- **MySQL** - Configurable for production use

### Configuration System

Nested class-based configuration using `ConfigLib`:
```kotlin
class Commands {
    val mailbox = createExtendedComplexField { CommandField("mailbox") }
}
```

## 🔧 Dependencies

### Runtime Dependencies
- **ConfigLib** (1.0.0) - Configuration system
- **DatabaseLib** (1.0.4) - Database abstraction
- **semver4j** (6.0.0) - Version comparison

### Build Dependencies
- **Shadow Plugin** - JAR shading and relocation
- **RunPaper** - Test server management

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

- 🐛 Report bugs via [GitHub Issues](https://github.com/bindglam/DearU/issues)
- 💡 Feature requests welcome
- 📖 Check the code documentation for more details

---

<div align="center">

Made with ❤️ for the Minecraft community

</div>

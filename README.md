LIFX Maven Extension
====================

Turn your lights on red when a Maven build fails.

# Installation

Build the JAR:

```
mvn package
```

Copy to the JAR `target/lifx-maven-extension-1.0-SNAPSHOT.jar` to your Maven installation's extension directory `$M2_HOME/lib/ext`.

# Customising targets

By default all discovered lights are changed. To only target lights with a certain label, pass the following property to the build:

```
mvn <goals> -Dlifx.label="My Label"
```

To set this permanently, you can add it to the `MAVEN_OPTS` environment variable.



# Codexa Custom Rule Engine Plugin Architecture

## Overview
Codexa is architected as an open modular analysis pipeline. External organizations can author proprietary rule plugins without modifying core codebase logic.

## Plugin Interface (`RuleProvider`)

```java
package com.codexa.analysis.plugin;

import com.codexa.analysis.model.Finding;
import com.codexa.analysis.model.ParsedFile;
import java.util.List;

public interface RuleProvider {
    String getPluginId();
    String getVersion();
    List<Finding> evaluate(ParsedFile file);
}
```

## Plugin Discovery via Java ServiceLoader (SPI)
Plugins are packaged as standard JAR files dropped into `/opt/codexa/plugins`. Codexa automatically discovers active providers on startup:

```
META-INF/services/com.codexa.analysis.plugin.RuleProvider
```

## Security Sandboxing for Plugins
- Plugins execute with restricted Java SecurityManager / classloader permissions.
- Network access and arbitrary file system writes are strictly forbidden outside of temporary scan directories.

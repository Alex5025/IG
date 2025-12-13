---
description: 'develop Rule (English)'

applyTo: '**/*.java'

---

## Develop Rules (Java)

This document defines the structure, field ordering, and method arrangement for individual Java source files in the project. The goal is to improve readability, consistency, and maintainability and to provide a unified standard for team code reviews.

---

## 1. Overall File Structure (Required)

Each Java class file MUST follow the order below:
```
package

import

// -------------------------
// Class fields (fields / constants)
// -------------------------

// -------------------------
// Public methods
// -------------------------

// -------------------------
// Private methods
// -------------------------
```
---

## 2. Import Block

* `package` must be at the top
* All `import` statements immediately follow
* Group imports by type and separate groups with a blank line

Example:

```java
// Java standard library
import java.util.List;
import java.util.Map;

// Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Project internal
import com.example.service.UserService;
```

---

## 3. Class Fields

### Rules

* All fields must be declared at the top of the class
* Fields that serve the same purpose must be grouped together
* Separate groups of fields with a blank line
* Constants should be declared as `private static final`

### Example

```java
// -------------------------
// Class fields
// -------------------------

// Constants
private static final int DEFAULT_TIMEOUT = 5000;
private static final String CONFIG_FILE = "app.yml";

// Dependencies
private final UserService userService;

// State fields
private boolean initialized = false;
private Config config;
```

---

## 4. Public Methods

### Rules

* `public` methods must be placed before private methods
* Each method should follow the Single Responsibility Principle (SRP)
* Public APIs must be clear and stable

### Example

```java
// -------------------------
// Public methods
// -------------------------

public void init() {
    if (initialized) {
        return;
    }
    this.config = loadConfig();
    this.initialized = true;
}

public Config getConfig() {
    if (config == null) {
        throw new IllegalStateException("Config not initialized");
    }
    return config;
}
```

---

## 5. Private Methods

### Rules

* Private methods are for internal use only
* All `private` methods should be grouped at the bottom of the file
* Method names should clearly describe internal behavior

### Example

```java
// -------------------------
// Private methods
// -------------------------

private Config loadConfig() {
    // implementation detail
    return new Config();
}
```

---

## 6. Additional Guidelines (Strongly Recommended)

* ❌ Avoid magic numbers → promote them to constants
* ❌ Avoid declaring variables inside methods that are effectively global in scope
* ✅ Files should be readable top-down
* ✅ Consistent structure is more important than personal preference

---

## 7. Summary

* Java file structure must be consistent
* Group and manage class-level fields centrally
* Clearly separate public and private sections

This guideline applies to all Java source files in the project.

# John Wick's Adventure

A 2D side-scrolling action game built with **Java** and **libGDX** as a Semester 2 Object-Oriented Programming project.

## Features

- Multi-level gameplay (`LevelOneScreen`, `LevelTwoScreen`, ...)
- Screen-based architecture using libGDX `Screen` classes
- Object-oriented design: inheritance, polymorphism, encapsulation and abstraction

## Project Structure

```
core/      Game logic, screens and entities (platform-independent)
desktop/   Desktop (LWJGL) launcher
assets/    Textures, audio and other game resources
gradle/    Gradle wrapper
```

## Requirements

- JDK 8 or newer
- No separate Gradle install needed (wrapper included)

## Run

```bash
./gradlew desktop:run
```

On Windows:

```bash
gradlew.bat desktop:run
```

## Build a runnable JAR

```bash
./gradlew desktop:dist
```

## Author

Muhammad Rehan — [@Muhammad-Rehan20](https://github.com/Muhammad-Rehan20)

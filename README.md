# SimpleIDE

SimpleIDE is a from-scratch desktop editor written in Java and Kotlin. It is a small Swing application with a project tree, a syntax-highlighted Java editor, a status bar, and a JSON theme — not a replacement for IntelliJ or Eclipse.

## Requirements

- JDK 21 or newer (Maven may use a later JDK; Kotlin 2.4.20 is required to compile on JDK 26)
- Maven 3.x (`mvn` on your `PATH`)
- Make (optional; used for `make run`, `make test`, and `make vscode`)

There is no Maven wrapper in this repository.

## Build, test, and run

Run every command from the **repository root**. Theme and sample-project files are loaded as paths relative to the working directory.

```bash
# Tests (same command CI uses)
make test
# or: mvn --batch-mode test

# Run the Swing app
make run
```

In IntelliJ, run `com.adamkali.simpleide.App` with **Working directory** set to the repository root.

Do not use `java -jar target/SimpleIDE-1.0-SNAPSHOT.jar` as a complete launch path. The JAR manifest lists a classpath, but dependencies are not copied next to the artifact.

## VS Code

1. Install [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack). For Kotlin highlighting, also install [Kotlin](https://marketplace.visualstudio.com/items?itemName=fwcd.kotlin).
2. Open this repository folder in VS Code.
3. Generate launch configs:

   ```bash
   make vscode
   ```

   That writes `.vscode/launch.json`, `tasks.json`, `settings.json`, and `extensions.json`. These files are gitignored; regenerate them anytime with the same command.
4. Wait for the Java language server to import the Maven project.
5. Run and Debug → **Run SimpleIDE** (F5).

The launch config sets `cwd` to `${workspaceFolder}` and compiles with Maven first. If the working directory is wrong, the editor theme and sample project fail to load.

## Features

- Project tree (left) and code editor (right), with a line/column status bar
- Typing, Enter, Backspace, Tab (inserted as 4 spaces), arrow keys
- Mouse placement and drag selection
- Ctrl+D to duplicate the current line
- Java-like syntax highlighting via a custom lexer
- Current-line highlight from `src/main/resources/preferences/editor-theme.json`
- Sample project at `src/main/resources/testproject/` (`TestProject.proj`)

## Layout

| Path | Role |
|------|------|
| `window/` | Frame, editor panel, status panel |
| `editor/` | Code editor, coordinates, input |
| `editor/io/` | Document, lines, cursor, edit actions, theme loader |
| `project/` | `.proj` loading and source tree |
| `project/lang/` | Lexer and token types used for highlighting |
| `browser/` | Project tree UI |
| `preferences/` | Editor colors and theme data |

## Current limitations

- The app does not compile or run the open project.
- The project tree does not open files into the editor.
- `ProjectManager` can load a `.proj` file; it does not save.

## CI

GitHub Actions runs `mvn --batch-mode test` on pushes to `main` and on pull requests, with headless AWT.

## For contributors and coding agents

See [AGENTS.md](AGENTS.md) for commands, package map, and testing notes.

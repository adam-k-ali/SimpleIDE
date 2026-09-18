# SimpleIDE

SimpleIDE is a from-scratch desktop editor written in Java and Kotlin. It is a small Swing application with a project tree, a syntax-highlighted Java editor, a status bar, and a JSON theme — not a replacement for IntelliJ or Eclipse.

## Requirements

- JDK 21 or newer (Maven may use a later JDK; Kotlin 2.4.20 is required to compile on JDK 26)
- Maven 3.x (`mvn` on your `PATH`)
- Make (optional; used for `make run`, `make test`, and `make vscode`)

There is no Maven wrapper in this repository.

## Build, test, and run

Run every command from the **repository root**. The editor theme is loaded as a path relative to the working directory.

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

The launch config sets `cwd` to `${workspaceFolder}` and compiles with Maven first. If the working directory is wrong, the editor theme fails to load.

## Features

- Home Screen at startup: **Open Project** (choose a folder) or **New Project** (name + parent folder)
- Open Project initializes `.simple/{folderName}.proj` when the folder is not yet a SimpleIDE project
- New Project writes `Name/.simple/Name.proj` (`projectName` + `sourcePaths: ["src"]`) and an empty `src/` directory, then opens the editor
- Project tree (left) and code editor (right), with a line/column status bar
- Typing, Enter, Backspace, Tab (inserted as 4 spaces), arrow keys
- Click a file in the project tree to open it in the editor
- Right-click a folder in the project tree to create a **New File** or **New Folder**
- Ctrl/Cmd+S to save (Save As when the buffer is untitled), Ctrl/Cmd+Shift+S Save As, Ctrl/Cmd+R to reload from disk (Save / Discard / Cancel if there are unsaved edits)
- Mouse placement and drag selection
- Ctrl+D to duplicate the current line
- Java-like syntax highlighting via a custom lexer
- Current-line highlight from `src/main/resources/preferences/editor-theme.json`
- Sample project at `src/main/resources/testproject/` (`.simple/TestProject.proj` with `src` and `notes`); extra QA fixtures include a few-thousand-line file, highlighter kitchen, and nested packages. Open the folder from the Home Screen

## Layout

| Path | Role |
|------|------|
| `window/` | Frame, home screen, editor panel, status panel |
| `editor/` | Code editor, coordinates, input |
| `editor/io/` | Document, lines, cursor, edit actions, theme loader |
| `project/` | `.simple` project folders, `.proj` JSON, and source tree |
| `project/lang/` | Lexer and token types used for highlighting |
| `browser/` | Project tree UI |
| `preferences/` | Editor colors and theme data |

## Current limitations

- The app does not compile or run the open project.
- After a project is open, there is no File menu to open or create another one; restart the app to return to the Home Screen.

A sequenced plan to close these gaps (edit correctness, project session, undo/find, multi-file tabs, lexer, compile/run) is in [ROADMAP.md](ROADMAP.md).

## CI

GitHub Actions runs `mvn --batch-mode test` on pushes to `main` and on pull requests, with headless AWT.

## For contributors and coding agents

See [AGENTS.md](AGENTS.md) for commands, package map, and testing notes.

# AGENTS.md

SimpleIDE is a Maven Swing desktop editor (Java 21 bytecode + Kotlin 2.4). Entry point: `com.adamkali.simpleide.App` in `src/main/java/com/adamkali/simpleide/App.java`. Kotlin 1.9 cannot run on JDK 26; keep `kotlin.version` at 2.4.20+. Do not raise `maven.compiler.release` / `jvmTarget` to 25+ until Kotlin test-compile can load those class files.

Theme JSON is loaded as a filesystem path relative to the process working directory. Always run and debug from the **repository root**. The sample project is not auto-loaded; open its folder from the Home Screen or in tests via `ProjectManager.load`.

## Commands

```bash
make test          # mvn --batch-mode test (same as CI)
make run           # compile, then launch the GUI
make vscode        # regenerate local .vscode launch configs
mvn --batch-mode test
```

`make run` uses `exec-maven-plugin` with `-Dexec.mainClass=com.adamkali.simpleide.App`. Do not document or rely on `java -jar`; the packaged JAR is not self-contained.

## VS Code

`make vscode` writes gitignored files under `.vscode/`:

- Launch **Run SimpleIDE**: `mainClass` `com.adamkali.simpleide.App`, `cwd` `${workspaceFolder}`, `preLaunchTask` `mvn compile`
- Do **not** add `java.awt.headless` to the app launch (tests/CI are headless; the GUI must not be)
- Do **not** commit `.vscode/` (already gitignored, same as `.idea/`)

## Where to change things

| Change | Location |
|--------|----------|
| Keyboard/mouse editing | `editor/CodeEditor.java`, `editor/io/action/` (`ActionsList` + `Action` subclasses) |
| Document / cursor | `editor/io/Document`, `Line`, `EditorCursor` |
| Layout math (gutter, tabs, hit-testing) | `editor/EditorCoordinates.java` |
| Syntax highlighting | `project/lang/Lexer.java` and `project/lang/tokens/**` only |
| Project load / create / `.simple` + `.proj` JSON | `project/ProjectManager.kt`, `project/ProjectFile.kt` |
| Open / save / reload source files | `editor/io/OpenFile.kt`, `editor/io/Document.kt` |
| Project tree UI | `browser/`, `browser/components/` |
| Home Screen (open / new project) | `window/HomeScreen.kt`, `window/AppWindow.kt` |
| Window chrome | `window/AppWindow.kt`, `EditorPanel`, `StatusPanel` |
| Theme JSON | `src/main/resources/preferences/editor-theme.json`, `editor/io/theme/ThemeLoader.kt` |
| Shared cursor / font / theme | `Global.java` |

`Line.rewrite()` calls `project.lang.Lexer.lex()`. Highlighting lives only in `project.lang`. Add keywords/operators in `project.lang.tokens` and register them in `project.lang.Lexer`.

Functionality priorities and phase order: [ROADMAP.md](ROADMAP.md).

## Tests

- JUnit 5 under `src/test/java` (Jupiter). CI: `.github/workflows/ci.yml` on `main` and pull requests, Temurin 21, `JAVA_TOOL_OPTIONS=-Djava.awt.headless=true`.
- GUI tests paint off-screen via `src/test/java/com/adamkali/simpleide/testsupport/GuiRender.kt` (pixel/color assertions, synthetic mouse events).
- Reset shared state in `@BeforeEach`:

```kotlin
Global.setCursor(EditorCursor(Document(), 0, 0))
OpenFile.reset()
ProjectManager.reset()
Global.setTheme(ThemeData(ThemeLoader.load("src/main/resources/preferences/editor-theme.json")))
```

Theme load in tests also depends on CWD = repo root.

## Conventions

- Mixed Java and Kotlin live together under `src/main/java` (package `com.adamkali.simpleide`). New UI/project code tends to Kotlin; match the nearest files.
- No Checkstyle, ktlint, Spotless, or EditorConfig. No env files or secrets.
- Kotlin Maven plugin compiles `src/main/java` (`jvmTarget` 21, matching `maven.compiler.release`). Default `maven-compiler-plugin` compile executions are disabled in `pom.xml`; leave that hybrid setup alone unless you are changing the build.
- `ProjectManager` loads a **folder** that contains `.simple/` (creating `.simple` and `{folderName}.proj` if they are missing). The `.proj` JSON is `projectName` + `sourcePaths`; `rootPath` is the folder that contains `.simple` and is not persisted. New Project still scaffolds `src/` beside `.simple`. The Home Screen is shown at startup; `ProjectBrowser` does not auto-load the sample. Tests that need the sample tree should call `ProjectManager.load(Paths.get("src/main/resources/testproject"))`. Clicking a file in the project tree opens it in the editor (`OpenFile`). Ctrl/Cmd+S saves, Ctrl/Cmd+R reloads from disk.

## Sample data

- Theme: `src/main/resources/preferences/editor-theme.json`
- Sample project: `src/main/resources/testproject/` (`.simple/TestProject.proj` + `src/`)

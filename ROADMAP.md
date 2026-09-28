# SimpleIDE functionality roadmap

SimpleIDE is a from-scratch Swing editor, not a clone of IntelliJ or VS Code. This roadmap is ordered by **what unblocks daily use**, then by **what makes it an IDE**. Later phases assume earlier ones; skip ahead only when a later item has no dependency.

The north star is a small Java editor you can open a project folder in, edit several files without losing work, and compile/run from the same window.

## Where things stand today

Working today (as of `main` after Phase 0 edit-correctness work):

- Home Screen: open a folder (creates `.simple/{folderName}.proj` if needed) or create a new project (`src/` + `.simple/Name.proj`)
- Project tree, single-buffer editor, line/column status bar
- Typing, Enter (copies indent), Backspace, Delete, Tab as 4 spaces
- Mouse caret placement, drag selection, Shift+click extend, double-click token select
- Shift+arrows character selection; Alt or Ctrl/Cmd+Left/Right token motion; Ctrl/Cmd+Shift+Left/Right token selection; Alt+Up/Down line swap
- Typing, Backspace, and Delete replace the selection
- Home / End / Ctrl+Home / Ctrl+End, Page Up / Page Down, Ctrl/Cmd+A select all
- Copy / cut / paste, Ctrl+D duplicate line
- Ctrl/Cmd+S save, Ctrl/Cmd+R reload (with unsaved prompt)
- Caret-follow scrolling, current-line highlight, Java-like lexer highlighting
- VS Code-style chrome: activity bar (Explorer only), single cosmetic tab, dark theme JSON

Documented gaps (README):

- The app does not compile or run the open project
- After a project is open there is no way back to the Home Screen except restart

Architectural constraints that later features must respect or replace:

| Constraint | Why it matters |
|------------|----------------|
| One global cursor/document (`Global`) | Multi-file buffers cannot share one `EditorCursor` |
| `OpenFile` is a singleton | Tabs, split view, and "close file" have nowhere to live |
| `EditorTabBar` paints one title | Not a tab strip yet |
| `ActivityBar` has only Explorer | Search / output need new icons and panels |
| Lexer is per-line, one class per token | Block comments, strings with escapes, and Java 21 keywords do not fit cleanly |
| Theme path is CWD-relative | Wrong working directory = blank/broken colors |
| 60 fps full-document `repaint()` | Large files will hitch before any language feature does |

## Principles

1. **Stay from-scratch.** Prefer extending `editor/io/action/`, `Document`, `Lexer`, and the existing Swing chrome. Do not add LSP, a bundled JDK compiler UI framework, or a second editor widget.
2. **Edit actions stay the source of truth.** New keyboard behavior is an `Action` registered in `ActionsList`, with a JUnit test, same as Delete / Copy / Save.
3. **Tests before chrome.** Headless GUI tests via `GuiRender` for anything that paints or handles input. Reset `Global` / `OpenFile` / `ProjectManager` / theme in `@BeforeEach`.
4. **One language first.** Java highlighting and `javac` for the open project. Kotlin-in-the-IDE is out of scope until Java edit/run is solid.
5. **Do not grow `editor.lang`.** Highlighting lives in `project.lang`. The leftover Java token tree mentioned in `AGENTS.md` is already gone from the tree; delete the warning when convenient.

---

## Phase 0 — Land in-flight work and fix edit correctness

**Goal:** Selection and typing behave like a normal editor. No new surfaces.

| Step | Change | Why |
|------|--------|-----|
| 0.1 | Merge [#11](https://github.com/adam-k-ali/SimpleIDE/pull/11) | Shift-selection, token motion, and line swap are already implemented and tested |
| 0.2 | Typing, Backspace, and Delete **replace** a selection | `keyTyped` currently `clearSelection()` then inserts, so selected text survives. `TypeCharacterAction` / `BackspaceAction` never call `EditorCursor.deleteSelection()`. Paste already replaces; make the others match |
| 0.3 | Bind Home / End / Ctrl+Home / Ctrl+End | `EditorCursor` already has `moveToStartOfLine`, `moveToEndOfLine`, `moveToStartOfDocument`, `moveToEndOfDocument` — they are unused in `CodeEditor.KeyboardHandler` |
| 0.4 | Page Up / Page Down | Move caret by viewport line count and `ensureCursorVisible()` |
| 0.5 | Ctrl/Cmd+A select all | Selection model already supports multi-line ranges |
| 0.6 | Shift+click extends selection; double-click selects the token under the caret | Mouse can create a range but cannot grow one from the keyboard caret |

**Done when:** A dragged or Shift-selected range is replaced by the next character, deleted by Backspace/Delete, and Home/End/Select All work in `CodeEditorGuiTest`.

---

## Phase 1 — Project session and file lifecycle

**Goal:** Use the app for more than one file without restarting or losing edits.

Window and project:

| Step | Change | Where |
|------|--------|--------|
| 1.1 | File menu (or Home Screen command): New Project, Open Project, Close Project | `AppWindow`, `HomeScreen`. Close returns to the Home Screen instead of `EXIT_ON_CLOSE` only |
| 1.2 | Confirm discard on **window close** if `OpenFile.isDirty()` | `AppWindow` + `OpenFile.confirmIfDirty` (today the prompt only runs on open/reload) |
| 1.3 | Recent projects on the Home Screen | Persist a short list next to `.simple` (or in user prefs). Click to reopen |
| 1.4 | Save As; allow saving an untitled buffer | `OpenFile` today no-ops `save()` when `path == null` |

Project tree:

| Step | Change | Where |
|------|--------|--------|
| 1.5 | New file / new folder from the tree (context menu or toolbar) | `ProjectBrowser`, `ProjectManager`, then `rebuildTree()` |
| 1.6 | Rename and delete (with dirty-buffer check if the file is open) | Same |
| 1.7 | Refresh tree from disk | `ProjectManager.load` already rescans; expose it without requiring a full project reopen |

**Done when:** You can create a project, add `Hello.java`, edit, save, close the project, reopen it from Recents, and get a dirty prompt if you quit with unsaved changes.

---

## Phase 2 — Undo, find, and structured editing

**Goal:** Recover from mistakes and jump around a file. This is the last "editor" phase before multi-file.

| Step | Change | Notes |
|------|--------|--------|
| 2.1 | Undo / redo stack | Record document+caret snapshots (or inverse actions) on every `Action.execute`. Ctrl/Cmd+Z / Shift+Z. Clear the stack on `OpenFile.loadFromDisk`. This is the highest-leverage missing edit feature |
| 2.2 | Find in file (Ctrl/Cmd+F) | Highlight matches, next/previous, wrap. Status bar or a small bar above the editor — do not wait for a Search activity icon |
| 2.3 | Replace in file (Ctrl/Cmd+H) | Reuse find; each replace is one undo step |
| 2.4 | Tab / Shift+Tab indent or outdent the selection | Today Tab always inserts 4 spaces at the caret |
| 2.5 | Toggle line comment (Ctrl/Cmd+/) | Lexer already has `SLCommentToken` |
| 2.6 | Go to line (Ctrl/Cmd+G) | Status bar already shows `Ln n, Col n` |
| 2.7 | Highlight matching `()`, `{}`, `[]` | Use the existing operator tokens; no parser required |

**Done when:** Undo restores text and caret; Find/Replace works across lines; indent and comment operate on the selection.

---

## Phase 3 — Multi-file buffers

**Goal:** The tab strip becomes real. **Do not start this until Phase 0–1 dirty handling is solid.**

`OpenFile` and `Global.getCursor()` are singletons. Multi-file means introducing an explicit buffer:

1. A `Buffer` (path + `Document` + `EditorCursor` + dirty snapshot).
2. An `EditorSession` (list of buffers, active index) owned by `EditorPanel`, not `Global`.
3. `Global` keeps font/theme/metrics only; tests set the active buffer the same way they set the cursor today.
4. `EditorTabBar` lists buffers, dirty dots, click-to-activate, click-x to close (with the existing unsaved prompt).
5. Closing the last tab leaves an untitled buffer or an empty editor state — pick one and test it.
6. Optional: Ctrl/Cmd+W close, Ctrl/Cmd+Tab next tab.

Keep one `CodeEditor` instance and swap the active buffer. Split editors are out of scope.

**Done when:** Two files can be open, each with independent caret/selection/dirty state, and switching tabs does not lose unsaved text.

---

## Phase 4 — Language and editor engine

**Goal:** Highlighting and painting do not lie, and do not fall over on larger files.

Lexer (`project.lang.Lexer` only):

| Step | Change |
|------|--------|
| 4.1 | Finish block comments (`/* */`) — the code is present but commented out, and the loop condition is wrong |
| 4.2 | Character literals, string escapes, text blocks (`"""`) |
| 4.3 | Integer / long / hex / binary literals (`isInteger` exists and is unused) |
| 4.4 | Operators: `->`, `::`, `...`, `@` |
| 4.5 | Keywords the token map is missing: `var`, `record`, `yield`, `sealed`, `permits`, `non-sealed` |
| 4.6 | Stop double-lexing in `Line.rewrite()` (it calls `Lexer.lex` twice) |

Maintainability (can ship beside 4.x):

- Collapse the one-class-per-keyword/operator hierarchy into token **kinds** plus a small set of classes (`KeywordToken`, `OperatorToken`, …). Eighty-plus empty subclasses make every lexer change expensive.
- Load `editor-theme.json` from the classpath (with the repo-root file as a development override) so `make run` is not the only working CWD.
- Paint visible lines only; stop the 60 fps timer from expanding and repainting the whole document when nothing changed.
- Drop unused `pom.xml` weight: `maven-jar-plugin` as a **dependency**, JUnit 4 beside Jupiter, Gson `1.7.1`.

**Done when:** The sample project and a file with block comments, records, and text blocks highlight correctly; opening a few-thousand-line file stays usable.

---

## Phase 5 — Compile, run, and output

**Goal:** SimpleIDE becomes an IDE. README's first limitation is this phase.

| Step | Change |
|------|--------|
| 5.1 | Bottom **Output** panel, toggle from the activity bar (second icon next to Explorer) | Same pattern as `toggleSidebar()` in `EditorPanel` |
| 5.2 | Build: `javac` the `.java` files under `sourcePaths` into `out/` (or `target/classes`) | Parse stderr; no Maven invocation required for v1 |
| 5.3 | Run: `java -cp out <main-class>` with a simple "main class" field (status bar or a Run dialog) | Show stdout/stderr in Output |
| 5.4 | Click a `file:line` compiler error to open that buffer and move the caret | Depends on Phase 3 if the file is not already open; can v1 only jump in the current file |
| 5.5 | Status bar: `Ready` / `Build failed` / last run exit code | `StatusBar` today only paints `"SimpleIDE"` and `Ln, Col` |

Keep this to the JDK on `PATH`. Do not embed `javax.tools` UI or a debugger until Build/Run is boringly reliable.

**Done when:** The sample `TestProject` (or a new-project `src/Main.java`) compiles and runs from a keyboard shortcut, and a syntax error is clickable.

---

## Phase 6 — Search across the project

**Goal:** Find usages without leaving the window. Depends on Phase 3 (open from a hit) and Phase 5's activity-bar pattern.

- Activity bar Search icon: query box + results list over `sourcePaths`
- Click a hit → open buffer + select the match
- Optional later: filter by file name, regex, case sensitivity

This is deliberately after compile/run. Cross-file search is less valuable than being able to run the program.

---

## Out of scope (until the phases above are done)

These are real IDE features. They are the wrong next steps for this codebase:

- Debugger, breakpoints, variable watches
- Code completion, refactor rename, auto-import, go-to-definition
- Git integration
- Kotlin (or any second language) highlighting inside the editor
- Plugin system, LSP client, embedded terminal emulator
- Split editors, minimap, vim mode, remote development
- Shipping a self-contained `java -jar` fat JAR / `jpackage` installer (track separately as packaging, not functionality)

## Suggested issue breakdown

Open GitHub issues in this order so work can land as small PRs matching existing history (`feature/delete-action`, `feature/copy-cut-paste`, …):

1. Merge keyboard selection PR; make type/backspace/delete replace the selection
2. Home/End/PageUp/PageDown and Select All
3. File menu + Close Project + quit dirty prompt
4. New file/folder in the project tree
5. Undo/redo
6. Find (then Replace) in the current file
7. Buffer session + real tabs
8. Lexer: block comments, literals, modern keywords
9. Output panel + `javac` / `java`
10. Project-wide search

Each issue should name the packages from `AGENTS.md` (`editor/io/action`, `window/`, `project/`, `browser/`) and require tests in the matching `src/test/java` tree.

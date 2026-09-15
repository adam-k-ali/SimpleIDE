.PHONY: compile test run vscode

compile:
	mvn -q compile

test:
	mvn --batch-mode test

run: compile
	mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.adamkali.simpleide.App

# Writes local VS Code launch configs. .vscode/ is gitignored.
vscode:
	mkdir -p .vscode
	python3 -c 'import json, pathlib; \
p = pathlib.Path(".vscode"); \
p.mkdir(exist_ok=True); \
(p / "extensions.json").write_text(json.dumps({ \
    "recommendations": ["vscjava.vscode-java-pack", "fwcd.kotlin"] \
}, indent=2) + "\n"); \
(p / "settings.json").write_text(json.dumps({ \
    "java.configuration.updateBuildConfiguration": "automatic", \
    "java.configuration.runtimes": [{"name": "JavaSE-21", "default": True}] \
}, indent=2) + "\n"); \
(p / "tasks.json").write_text(json.dumps({ \
    "version": "2.0.0", \
    "tasks": [{ \
        "label": "mvn compile", \
        "type": "shell", \
        "command": "mvn -q compile", \
        "group": "build", \
        "problemMatcher": ["$$javac"], \
        "options": {"cwd": "$${workspaceFolder}"} \
    }] \
}, indent=2) + "\n"); \
(p / "launch.json").write_text(json.dumps({ \
    "version": "0.2.0", \
    "configurations": [{ \
        "type": "java", \
        "name": "Run SimpleIDE", \
        "request": "launch", \
        "mainClass": "com.adamkali.simpleide.App", \
        "projectName": "SimpleIDE", \
        "cwd": "$${workspaceFolder}", \
        "preLaunchTask": "mvn compile", \
        "console": "internalConsole" \
    }] \
}, indent=2) + "\n")'
	@echo "Wrote .vscode/launch.json, tasks.json, settings.json, and extensions.json"

#!/bin/bash
javac --module-path "$PATH_TO_FX" --add-modules "javafx.controls,javafx.fxml" ../src/main/java/project/*.java
java -cp "../src/main/java" --module-path "$PATH_TO_FX" --add-modules javafx.controls,javafx.fxml  project.ScatteringModel
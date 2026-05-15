# PNJava
A Jack-like language compiler that parses png images as Nand2Tetris Hack VM code

Getting Started
Please enter the full filepath of the folder containing your .png image files in launch.json in project11/.vscode/

"args": "Z:/ECS/Project11/Project11/src/IMGS"

(Replace the file path with your folder of choice)

Simply run Main.java to create your output - Filename.vm

Included Files
Main.java - runs the program and fileIO Symbol.java - object used for symbol table handling PNGParser - object used for code parsing, I was definitley not trying to spell XML. launch.json - please enter runtime arguments here IMGs - folder containing test programs PNJava Jack conversion sheet.pdf - conversions from Jack syntax to PNJava pixel information

Added features (detailed in conversion sheet)
for loops ++/-- operations minor rework of Strings to replace ""

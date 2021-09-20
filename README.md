# Notepad--

This notepad app will take you 200 years back to the Typewriter.

## Install requirements

```sh
sudo apt update
sudo apt install default-jre default-jdk -y
```

## Download the executable jar

Get it in the [releases section](https://github.com/donno2048/Notepad--/releases)

## Compile

```sh
javac com/elisha/notepad/Main.java
```

## After compiling

### Run

```sh
java com.elisha.notepad.Main
```

### Make JAR

```sh
jar cmf MANIFEST.MF Notepad--.jar com/elisha/notepad/*.class
```

### Run JAR

```sh
java -jar Notepad--.jar
```

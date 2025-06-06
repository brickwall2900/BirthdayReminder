# 🎂 BirthdayReminder
🍰🎉 Never forget any of your friends' birthdays with this one!

<img src="readme/notify.png" alt="notification">

---
## How to Use
After launching BirthdayReminder, a tray icon will appear on your taskbar.
Right-click the icon to open a pop-up menu, then click the `Open` option.
This will bring up the editor, where you can add, remove, or edit
people's birthdays. You can also configure it to play a sound when it's
someone's birthday!

<img src="readme/editor.png" alt="the editor">

---
## Building
Java 21 is required for building.

On your terminal, execute these commands:
1. `git clone https://github.com/brickwall2900/BirthdayReminder.git`
2. `cd BirthdayReminder`
3. `./gradlew build`

..or open this project in an IDE as a Gradle project.

If you want to create an executable JAR: `./gradlew fatJar`. 
The executable JAR should be at `./build/libs/BirthReminder-<version>.jar` 

---
## Start on Computer Boot 🚀
Currently, you need to manually set this application to auto-start 
with your operating system. BirthdayReminder checks if it is someone's 
birthday today when it is launched.
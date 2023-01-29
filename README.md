# Whatsapp-Tool

This is a command line tool for WhatsApp based on Java. You can use this tool to automate sending messages or sending messages from command line.

## Requirements

 - Java 11+
 - Browser (recommended Chromium or Google Chrome)

## Getting started
For your first launch, you have to start the JAR-File in a command-line shell with the following command on a PC with desktop environment.
```
java -jar WhatsAppTool.jar --withGui
```
This step is important, because it creates the directory structure in the folder where the JAR-File is and launches WhatsApp Web. You have to scan the QR-Code with your phone.

Alternatively, you can replace the `${pathToJar}/tmp/profileChrome/Default` Folder with an existing one, where you are already registered in WhatsApp Web when using Chromium or Google Chrome as browser.

After launch, the directory structure looks as follows:

```
./
│   WhatsAppTool.jar
│
└───config
│   │   settings.properties
│   │   notifications.example.xml
	|	notifications.xml
│   
└───logs
	|	log.txt
│   
└───tmp
    └───profileChrome
	    └───Default
		    |	...
	|	...
```
The `${pathToJar}/tmp/profileChrome/Default` folder is important after registering in WhatsApp Web. To use it in a non desktop environment you have to copy this Folder to the same location in reference to the folder structure above where your WhatsAppTool.jar is.
Then you can start it on the machine without Desktop Environment with:

    java -jar WhatsAppTool.jar

This will start the App and the Chromium or Google Chrome Browser in headless mode and start the command line interface. Enter `help` will show you the commands.
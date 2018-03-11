# Multi-Process Manager

# Table of Contents
1. [Long story short](#long-story-short)
2. [Features](#features)
3. [Download](#download)
4. [Screenshots](#screenshots)
5. [Launch configurations](#launch-configurations)

## Long story short

The Multi-Process Manager is a Java Swing application that helps managing multiple running applications. It was designed to be used by developers that need to start a lot of processes producing logs like Spring Boot applications, Tomcat-, JBoss- or any other server process. 

This application manages launch configurations for you and allows starting multiple applications simultaneously. Any output of the running applications is captured and presented in a neat window providing an overview over the log file so far as well as the log file itself. The view allows to easliy navigate through the running application at any time. 

## Features

The Multi-Process Manager comes with the following features:
* a new user interface making things super easy
* storing all launch configurations per user
* managing launch configurations within categories
* starting all applications within category
* starting all selected applications of a category
* stop, stop forcibly, restart applications
* save (or clear) the application output at any time
	* supports plain text
	* supports HTML
	* supports RTF
* use regular expressions (so called "counter expressions") to count matches while capturing the application output.
	* any number of counter expressions can be added
	* the first 6 counter expressios are shown in the output overview
* override environment variables in a per-application manner
* easily find your application in the overview by
	* defining a color for every launch configuration
	* defining a title for every launch configuration

## Download

You can [download the latest version of this application](https://bintray.com/schuettec/maven/download_file?file_path=com%2Fgithub%2Fschuettec%2Fmultiprocman%2Fmultiprocman%2F0.0.2%2Fmultiprocman-0.0.2-jar-with-dependencies.jar) as an executable JAR file from the Bintray repository.


## Screenshots

### The launch configuration editor

![alt text](etc/screenshots/01.png)

### The category editor

![alt text](etc/screenshots/02.png)

### The launch configuration editor

![alt text](etc/screenshots/03.png)

### The application manager

![alt text](etc/screenshots/04.png)

## Launch configurations

The launch configuration editor view enables you add all required information to run an application inside the Multi-Process Manager.


### Application  

![alt text](etc/screenshots/03.png)

In the tab "application" you can specify an icon for this launch configuration. It is recommended to use 24x24px images for application icons. Images with other dimensions will be scaled.

__Title__: The title is a short description of this application that is displayed in the overview window.

__Command__: The command is the full command line to start the application. The command must include the full path to the application/script/batch file (except the binary programs shipped with your OS). __Use the "Find..." button to select a__

__Charset__: The charset attribute is used to capture the application output using the correct encoding. For windows command line applications the charset "IBM850" should be used. In most cases (especially Java apps) UTF-8 may be suitable.

__Working directory__: The working directory is the path to the folder within the application's process will be started. For example the Git executables may be available in `C:\Program Files\Git\bin` so this will be the path that must be set in the command. But if you want to run Git in your repository folder the working directory must be `C:\git\myRepo`.

  
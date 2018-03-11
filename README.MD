# Multi process manager

# Table of Contents
1. [Long story short](#long-story-short)
5. [Features](#features)
2. [Download](#download)
3. [Screenshots](#screenshots)

## Long story short

The Multi-process manager is a Java Swing application that helps managing multiple running applications. It was designed to be used by developers that need to start a lot of processes producing logs like Spring Boot applications, Tomcat-, JBoss- or any other server process. 

This application manages launch configurations for you and allows starting multiple applications simultaneously. Any output of the running applications is captured and presented in a neat window providing an overview over the log file so far as well as the log file itself. The view allows to easliy navigate through the running application at any time. 

## Features

The Multi-process manager comes with the following features:
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
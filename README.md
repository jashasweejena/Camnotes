# CamNotes

First of all, a few screenshots:

First Screen!          
:-------------------------:|
![First](https://github.com/jashasweejena/GDG-HACKFEST/raw/master/screenshots/ss1.png)  

ViewFinder          |  Generated Pdf
:-------------------------:|:-------------------------: 
![ViewFinder](https://github.com/jashasweejena/GDG-HACKFEST/raw/master/screenshots/ss3.png)  |  ![Pdf](https://github.com/jashasweejena/GDG-HACKFEST/raw/master/screenshots/ss4.png) 


## What is this?

Presenting CamNotes : An app to help teachers, lecturers and professors to click a picture of their notes and upload it to the web for students to access. 


## Dependencies

The focus of this project lies on capturing an image and using libraries like OpenCV to enhance the image, crop it, etc. It uses the following dependencies:

- AppCompat Support Library
- Support Design Library
- Itextpdf Library
- ScanLibrary (A wrapper around OpenCV for android)

## Supported devices

The template support every device with a SDK level of at least 23 (Android Android 6.0+).


## Quick walkthrough

### Gradle

Nothing special here. Please note that the scanLibrary is added locally using *settings.gradle*:

```xml
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    implementation 'com.android.support:design:28.0.0'
    implementation 'com.itextpdf:itextg:5.5.10'
    implementation project(':scanlibrary')
```

### Manifest

Basically we have only one activity handling everything. We plan on making the app modular soon.

### Base classes

*MainActivity* is the parent class for every *Activity* inside this template. This class handles everything so far.

*UploadActivity* is the class to be used in the future to push the scanned documents to the cloud.

## About
We are a group of four people, Jashaswee Jena ([@jashasweejena](https://github.com/jashasweejena)), Subham Mohapatra([@sssubham90](https://github.com/sssubham90)), Manish Rath([@raikoz](https://github.com/raikoz)), Amrit Dash([@the_AoG_guy](https://github.com/the-AoG-guy)). This app is currently in early stages and, currently, just prototypes the idea. We plan on working on it actively in the near future.






 


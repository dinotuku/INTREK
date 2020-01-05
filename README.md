Group 2.C: Sports e-hiking
===
[![INTREK](https://img.shields.io/badge/INTREK-Slides-orange?labelColor=ff8800&color=0077ff)](https://docs.google.com/presentation/d/10PvcYlJNrxE2XCZsByf9MExi5w1P5duuJiOohyVXyRc/edit?usp=sharing)

> ### Track hikes with your phone, smartwatch and sensors

We developed an sport tracking app which allows the user to do some activites (as running, hiking, ...) while monitoring his/her performances. The application uses 3 sources of data inputs: the GPS data from the mobile, the measured data from a connected watch and the collected data from a connected board with some sensors. While the user is 'on recording', the application allows him/her to see his/her live perfomances either on the mobile or on the connected watch. The user can also see where he/she is and the traveled path. At the end of one activity, the user sees a recap of all the data with some statistics coming with it. He/She then has the possibility to share the hike with his friends and to save it on Firebase in order to retrieve it later on the application, in the 'History' part of the application, where users can see all their previous hikes. 

## Our Setup

#### Software
* `Android Studio == 3.5.2`

#### Hardware
* Huawei MediaPad T3 10
* Huawei Watch Sport 2
* STEVAL-STLKT01V1 STMicroelectronics development kit

## Java Code Overview

* `MainActivity` - The main interface, it contains three fragments controlled by SectionPagerAdapter.
* `WearService` - Manage all the interaction between the phone and smartwatch. We used the code on Moodle, remove the parts we didn't need and added cases we would use.

#### DataModel
* `Profile` - Store user information and statistics.
* `Recording` - Contain all the data of one recording and all the functions required to plot it on a analysis session. This class is the class that is saved on Firebase. 
* `RecordingData` - This class is implemented to provide an array and its time value. It is used to pass them as reference values for the rows in list view of statistics. One element of this class can be plotted on a plot of the analysis activity. 
* `XYPlotSeriesList` - This class is used to save all the data which needs to be plot by our graph. The class can be used to plot different series.

#### Interfaces
* `OnPositionUpdatedCallback` - Handle moments when a new point is computed by the GPS in order to update the path on the map. The class was used to have more comprenhensible code. 

#### Managers
All the inputs of data are handled using the 3 following classes. They are used to have clean code, comprenhensible and reusable in different classes without copy-pasting code between different classes. Indeed, data is collected on more than simply one activity. 
* `GPSManager` - Handle the GPS over an activity. It will handle all the receiving of new data by the GPS values. It will update the arrays containing the data, the TextViews to display them, eventually send it to the watch, and compute some statistics. 
* `HRManager`- Take care of receiving and displaying the data from the watch. It can also show the data on a plot, if the PlotView is provided. 
* `MicrocontrollerManager` - As for the two previous classes, it handles the receiving of new data from the connected board. Namely, it is here that all the bleutooth functionalities are implemented. 

#### ui.main
* `EmailActivity` - For user to enter email. It will get different exceptions, decides whether the user is registered or not, and send the result to PasswordActivity.
* `HistoryActivity` - Show activity history. It will fetch data on Firebase and show all the recordings in a list view.
* `InformationActivity` - For user to enter username and select profile picture. It will save these information to Firebase at the end.
* `LiveMapActivity` - Show a live map with the current location and some key statistics when user starts an activity.
* `LiveRecordingActivity` - Show statistics and heart rate plot when user starts an activity. It is the main activity for when using the app. 
* `LoginActivity` - The entry activity. It provides two login options, Facebook login and email/password login.
* `NewRecordingActivity` - The activity before a live recording. It provides user different types of activities.
* `PasswordActivity` - For user to enter password. It will validate the password and use FirebaseAuth to either sign in or register the user depending on the exceptions threw in EmailActivity.
* `ProfileFragment` - Show user information and statistics. It will fetch data on Firebase and do some simple calculation.
* `RecordingAnalysisActivity` - Show a summary of a completed hike. The summary will contain statistics and plots of sensor values. Then it takes care of dealing with the recording (User can save it, share it, or discard it).
* `SectionsPagerAdapter` - A FragmentPagerAdapter that returns a fragment corresponding to one of the sections/tabs/pages.
* `TypePickerPopUp` - Handle the picker in NewRecordingActivity.

## Team

| Arthur Bricq | Adrien Thirion | Kuan Tung |
| :---: |:---:| :---:|
| <img src="https://scontent.ftpe7-1.fna.fbcdn.net/v/t1.0-1/p320x320/58384228_2796106190430223_6880243506011439104_n.jpg?_nc_cat=100&_nc_ohc=IyTrcDxQazsAQnU_g7mzZJ8_bmezn-iPgOUXvfvHdbi46m4-ymBXmAFCw&_nc_ht=scontent.ftpe7-1.fna&oh=1c94d3eaa8f6e7059e75fd1cfd814509&oe=5EAA7F99" width=100> | <img src="https://scontent.ftpe7-4.fna.fbcdn.net/v/t1.0-1/p320x320/69374738_1666631240136834_9156000338036129792_n.jpg?_nc_cat=101&_nc_ohc=KFL8NJ_Nl4oAQmPUB1LYeaWJk233z0s3qBAM7zTzOp4BgQxWOXmuXImUQ&_nc_ht=scontent.ftpe7-4.fna&oh=9489c4736b58dcf438959d7c3d90b08e&oe=5E9F979D" width=100> | <img src="https://scontent.ftpe7-3.fna.fbcdn.net/v/t1.0-1/p320x320/44598597_2395336093814687_5861457721299042304_o.jpg?_nc_cat=108&_nc_ohc=S9RMSb64YhoAQkGyn-scFiV2xMyg6XZIv2dDWvzZXFz29QswtojFaU-Ww&_nc_ht=scontent.ftpe7-3.fna&oh=5f0d1fd5c995b718238bd81a7d123faf&oe=5E9D09A9" width=100>  |
| <a href="https://github.com/arthurBricq" target="_blank">`arthurBricq`</a> | <a href="https://github.com/AdrienThirion" target="_blank">`AdrienThirion`</a> | <a href="https://github.com/dinotuku" target="_blank">`dinotuku`</a> |

## References

* [Best Practices: Arrays in Firebase](https://firebase.googleblog.com/2014/04/best-practices-arrays-in-firebase.html)
* [FirebaseAuth](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth)
* [Firebase Auth Quickstart](https://github.com/firebase/quickstart-android/tree/5d87d878ea54daa2a3987d00724af28d605eab1d/auth)

## License

This project is licensed under the MIT License - see the `LICENSE.md` file for details

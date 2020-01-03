Group 2.C: Sports e-hiking
===
[![INTREK](https://img.shields.io/badge/-INTREK-orange)](https://github.com/dinotuku/INTREK)

> ### Track your hikes with your phone, smartwatch and sensors

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
* `Recording` - Contain all the data of one recording and all the functions required to plot it on a analysis session.
* `RecordingData` - This class is implemented to provide an array and its time value. It is used to pass them as reference values for the rows in list view of statistics. One element of this class can be plotted on a plot of the analysis activity
* `XYPlotSeriesList` - This class is used to save all the data which needs to be plot by our graph. The class can be used to plot different series.

#### Interfaces
* `OnPositionUpdatedCallback` - Handle moments when a new point is computed in order to update the path on the map.

#### Managers
* `GPSManager` - Handle the GPS over an activity. It will handle all the receiving of new data by the GPS values and the updating of the appropriate TextView. 
* `HRManager`- Take care of receiving and displaying the data from the watch.

#### ui.main
* `EmailActivity` - For user to enter email. It will get different exceptions, decides whether the user is registered or not, and send the result to PasswordActivity.
* `HistoryActivity` - Show activity history. It will fetch data on Firebase and show all the recordings in a list view.
* `InformationActivity` - For user to enter username and select profile picture. It will save these information to Firebase at the end.
* `LiveMapActivity` - Show a live map with the current location and some key statistics when user starts an activity.
* `LiveRecordingActivity` - Show statistics and heart rate plot when user starts an activity.
* `LoginActivity` - The entry activity. It provides two login options, Facebook login and email/password login.
* `NewRecordingActivity` - The activity before a live recording. It provides user different types of activities.
* `PasswordActivity` - // For user to enter password. It will validate the password and use FirebaseAuth to either sign in or register the user depending on the exceptions threw in EmailActivity.
* `ProfileFragment` - // Show user information and statistics. It will fetch data on Firebase and do some simple calculation.
* `RecordingAnalysisActivity` - // Show a summary of a completed hike. The summary will contain statistics and plots of sensor values.
* `SectionsPagerAdapter` - A FragmentPagerAdapter that returns a fragment corresponding to one of the sections/tabs/pages.
* `TypePickerPopUp` - Handle the picker in NewRecordingActivity.

## Team

| Arthur Bricq | Adrien Thirion | Kuan Tung |
| :---: |:---:| :---:|
| <img src="https://scontent.ftpe7-1.fna.fbcdn.net/v/t1.0-1/p320x320/58384228_2796106190430223_6880243506011439104_n.jpg?_nc_cat=100&_nc_ohc=IyTrcDxQazsAQnU_g7mzZJ8_bmezn-iPgOUXvfvHdbi46m4-ymBXmAFCw&_nc_ht=scontent.ftpe7-1.fna&oh=1c94d3eaa8f6e7059e75fd1cfd814509&oe=5EAA7F99" width=100> | <img src="https://scontent.ftpe7-4.fna.fbcdn.net/v/t1.0-1/p320x320/69374738_1666631240136834_9156000338036129792_n.jpg?_nc_cat=101&_nc_ohc=KFL8NJ_Nl4oAQmPUB1LYeaWJk233z0s3qBAM7zTzOp4BgQxWOXmuXImUQ&_nc_ht=scontent.ftpe7-4.fna&oh=9489c4736b58dcf438959d7c3d90b08e&oe=5E9F979D" width=100> | <img src="https://scontent.ftpe7-3.fna.fbcdn.net/v/t1.0-1/p320x320/44598597_2395336093814687_5861457721299042304_o.jpg?_nc_cat=108&_nc_ohc=S9RMSb64YhoAQkGyn-scFiV2xMyg6XZIv2dDWvzZXFz29QswtojFaU-Ww&_nc_ht=scontent.ftpe7-3.fna&oh=5f0d1fd5c995b718238bd81a7d123faf&oe=5E9D09A9" width=100>  |
| <a href="https://github.com/arthurBricq" target="_blank">`arthurBricq`</a> | <a href="https://github.com/AdrienThirion" target="_blank">`AdrienThirion`</a> | <a href="http://github.com/dinotuku" target="_blank">`dinotuku`</a> |

## License

This project is licensed under the MIT License - see the `LICENSE.md` file for details

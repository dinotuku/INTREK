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

#### SensorTile
This class are use for implement the microcontroller sensorTile in our application. 
* `BluetoothLeService` - Service for connect a device in BLE (bluetooth Low Energy). The device use the value in SampleGattAtributes for find the characteristic who contains the temperature and the pressure. The datas are then convert thanks to NumberConversion.
* `DeviceScanActivity` - Activity who scan all of the device available. Return the device choose by the user. 
* `SampleGattAtributes`- Some attributes of the sensorTile allow to recognise some services and characteristics
* `NumberConversion` - Methods use for convert the data in little endian to integer or short.

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
| <img src="https://avatars3.githubusercontent.com/u/36046994?s=460&u=834398861e545b697279cb240e139f25d6f2b8aa&v=4" width=100> | <img src="https://avatars3.githubusercontent.com/u/55891808?s=460&u=3422f68f50e59ae08d698add6854e3d6c7f144ac&v=4" width=100> | <img src="https://avatars3.githubusercontent.com/u/23370352?s=460&u=a3cae29e291984fc8a7533252653ea1b4b121f1c&v=4" width=100>  |
| <a href="https://github.com/arthurBricq" target="_blank">`arthurBricq`</a> | <a href="https://github.com/AdrienThirion" target="_blank">`AdrienThirion`</a> | <a href="https://github.com/dinotuku" target="_blank">`dinotuku`</a> |

## References

* [Best Practices: Arrays in Firebase](https://firebase.googleblog.com/2014/04/best-practices-arrays-in-firebase.html)
* [FirebaseAuth](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth)
* [Firebase Auth Quickstart](https://github.com/firebase/quickstart-android/tree/5d87d878ea54daa2a3987d00724af28d605eab1d/auth)

## License

This project is licensed under the MIT License - see the `LICENSE.md` file for details

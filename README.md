# DataLoggerApp
Accelerometer and GPS tracking and storage via a foreground service

## Installation
```bash
git clone git@github.com:CarlosAlmeida4/DataLoggerApp.git
```

## What it does
The app store accelerometer and gps data in 2 file types that are just basically xml files
The GPS data is stored in .gpx files while the accelerometer data is stored in .apx files, insipered on the .gpx format
The GPS is stored with a 1s time interval

### Interface
The interface is quite simple, just a start/stop button combination, A roller to choose the accelerometer time interval and a share button to share the folder with the results

![image](https://user-images.githubusercontent.com/12225819/145729741-ea5774d6-b37f-49ed-a76a-563a3e4679ea.png)

The foreground service runs with a notification that only shows the location and has two buttons, one stops the service the other opens the application

![image](https://user-images.githubusercontent.com/12225819/145730078-d6b45150-386a-4def-9b97-67dadca5c0eb.png)

### stored data

As mentioned before the accelerometer data is stored in .apx files
Four different accelerometer measurements are stored:
  - in phone axis
  - in phone axis and the earth accel removed
  - in earth axis 
  - in earth axis and earth accel removed (using the magnetic field)

## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
5. Push your branch (git push origin my-new-feature)
6. Create a new Pull Request

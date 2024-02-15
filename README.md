[![Build Status](https://travis-ci.org/qqq3/good-weather.svg?branch=master)](https://travis-ci.org/qqq3/good-weather)
[![Release](https://img.shields.io/github/release/qqq3/good-weather.svg)](https://github.com/qqq3/good-weather/releases)
[![License](https://img.shields.io/badge/license-GNU_GPLv3-orange.svg)](https://raw.githubusercontent.com/qqq3/good-weather/HEAD/LICENSE)

# Good Weather
Good Weather is an ad-free open source weather app for Android. It allows users to view the weather conditions for a specified location anywhere in the world over a 7 day time period. 

## Getting started
The app can be downloaded and navigated through F-Droid:

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/repository/browse/?fdid=org.asdtm.goodweather)

To contribute, get a copy of the repository and work off of the master branch. Android Studio with Intellij works well for Java development. Android emulators (provided through Android Studio) will show the changes you make to the app. 

If you find any bugs or missing features that should be considered, please clearly specify the problem in a GitHub issue. Provide a guide on how to reproduce the bug so that it can be investigated.

For pull requests, please provide a clear title and description that showcases the changes that were made. Code changes must match the quality and style of existing code. 

## User guide
[<img src="http://i.imgur.com/FntbN8S.png" alt="Good Weather preview" width="200">](http://i.imgur.com/FntbN8S.png)
[<img src="http://i.imgur.com/WVFXEoo.png" alt="Good Weather preview" width="200">](http://i.imgur.com/WVFXEoo.png)
[<img src="http://i.imgur.com/Ccg6rKf.png" alt="Good Weather preview" width="200">](https://i.imgur.com/Ccg6rKf.png)

Good Weather contains a multitude of features. The app opens up to the weather page of the last location that was searched. The page shows the current temperature (in Celsius), a description of the current weather condition, and the last time the page was refreshed. The bottom panel contains other information, such as the wind speed. Next to the location name, there are buttons for refreshing the page, searching for a new location, and showing the weather for the user's current location (if geolocation settings are turned on). Location search results will update in real time and display places from anywhere in the world. The hamburger menu on the main page shows other pages with more weather information.

[<img src="http://i.imgur.com/AYvBZMY.png" alt="Good Weather preview" width="200">](http://i.imgur.com/AYvBZMY.png)
[<img src="http://i.imgur.com/XqdR22W.png" alt="Good Weather preview" width="200">](http://i.imgur.com/XqdR22W.png)
[<img src="http://i.imgur.com/gZSBxQW.png" alt="Good Weather preview" width="200">](http://i.imgur.com/gZSBxQW.png)

On the Graphs page, there are line charts for temperature, wind, rain, and snow in the specified location over the next 7 days. Using the 3 dots icon, the numbers on the y-axis and the graph itself can be toggled on and off.

[<img src="http://i.imgur.com/LH8qfzd.png" alt="Good Weather preview" width="200">](http://i.imgur.com/LH8qfzd.png)
[<img src="http://i.imgur.com/fvtEfX3.png" alt="Good Weather preview" width="200">](http://i.imgur.com/fvtEfX3.png)

The Daily Forecast page shows an overview of the weather conditions in the location over the next 7 days. Clicking on a specific day will show an overview of the day's predicted weather, including temperatures throughout the day, wind speed, humidity percentage, and more.

[<img src="http://i.imgur.com/QxUkuYB.png" alt="Good Weather preview" width="200">](http://i.imgur.com/QxUkuYB.png)
[<img src="http://i.imgur.com/SyX9F6C.png" alt="Good Weather preview" width="200">](http://i.imgur.com/SyX9F6C.png)
[<img src="http://i.imgur.com/sdzVzFb.png" alt="Good Weather preview" width="200">](http://i.imgur.com/sdzVzFb.png)

The Settings page contains 3 pages: General settings, Widget settings, and the About section. The General settings page allows toggling between Celsius and Fahrenheit, language toggling, and notification options. Currently, Good Weather supports 10 languages: Basque, Belarusian, Czech, English, French, German, Japanese, Spanish, Polish, and Russian. The Widget settings page allows for geolocation toggling so the location used will always be the user's current location. Finally, the About section contains links to the repository, licenses, and app download locations. 

## To-Do
- [ ] Add weather map
- [ ] Improve accessibility for people with disabilities

# Translations
[https://hosted.weblate.org/projects/good-weather/strings/](https://hosted.weblate.org/projects/good-weather/strings/)

The strings are translated using [Weblate](https://weblate.org/en/). Follow
[these instructions](https://hosted.weblate.org/engage/good-weather/) if you would like to contribute 
[here](https://hosted.weblate.org/projects/good-weather/strings/).

## Donations
If you would like to help, you can donate Bitcoin on ```1FV8m1MKqZ9ZKB8YNwpsjsuubHTznJSiT8``` address.
Thanks!

## List of contributors
[berian](https://github.com/beriain), [mahula](https://github.com/mahula), [naofum](https://github.com/naofum), 
[thuryn](https://github.com/thuryn), [monolifed](https://github.com/monolifed), [marcoM32](https://github.com/marcoM32),
[Zagur](https://github.com/Zagur)

## License
```
Good Weather. App displays weather information.
Copyright (C) 2015-2017 Eugene Kislyakov <aiqcms@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

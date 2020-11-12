cd app
cd release
adb install -r app-release.apk
adb shell dpm set-device-owner com.fragtest.android.pa/.AdminReceiver
cd..
cd..

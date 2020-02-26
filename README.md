# app-attachment

![Release](https://jitpack.io/v/clickapps-android/app-attachment.svg)


App-Attachment is an easy,flexible library for you Android attachment feature, you can use it 
as dialog or just api, you can get images, different files from camera,internal storage or google drive.


# add dependence
in project level build.gradle

```gradle
allprojects {
  repositories {
                  google()
                  jcenter()
                    //...
                  maven { url 'https://jitpack.io' }
  }
}
```
in app level build.gradle
```gradle
dependencies {

        implementation 'com.github.clickapps-android:app-attachment:1.0.0'
}
```

#How to Ues
to use as dialog .. you need to provide your layout design.& you should use appAttachment ids for each button.
```xml
<item name="appAttachmentDialogCameraBtn" type="id" />
<item name="appAttachmentDialogGalleryBtn" type="id" />
<item name="appAttachmentDialogOtherBtn" type="id" />
```

for example here a design with all attachment options {camera,gallery,other}, the ids using from **app-attachment**
**R.layout.attachment_dialog_layout**
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:tools="http://schemas.android.com/tools"
              xmlns:app="http://schemas.android.com/apk/res-auto"
              android:orientation="vertical"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:paddingStart="20dp"
              android:paddingTop="30dp"
              android:paddingBottom="30dp"
              android:paddingEnd="20dp"
              android:background="#fff"
        >


    <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="18sp"
            android:fontFamily="sans-serif"
            android:textStyle="bold"
            android:textColor="#222222"
            android:letterSpacing="0.03"
            android:lineSpacingExtra="12sp"
            android:text="Add attachment"
            />


    <Button
            android:id="@id/appAttachmentDialogCameraBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="18sp"
            android:fontFamily="sans-serif"
            android:textStyle="normal"
            android:textColor="#3e3f42"
            android:lineSpacingExtra="6sp"
            android:text="take_photo"
            android:layout_marginTop="40dp"
            android:layout_marginStart="5dp"
            />
    <Button
            android:id="@id/appAttachmentDialogGalleryBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="18sp"
            android:fontFamily="sans-serif"
            android:textStyle="normal"
            android:textColor="#3e3f42"
            android:lineSpacingExtra="6sp"
            android:text="open_gallery"
            android:layout_marginTop="20dp"
            android:layout_marginStart="5dp"
            />
    <Button
            android:id="@id/appAttachmentDialogOtherBtn"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="18sp"
            android:fontFamily="sans-serif"
            android:textStyle="normal"
            android:textColor="#3e3f42"
            android:lineSpacingExtra="6sp"
            android:text="other_type"
            android:layout_marginTop="20dp"
            android:layout_marginStart="5dp"
            />

</LinearLayout>
```

# code side

```kotlin
AppAttachmentDialog(R.layout.attachment_type_dialog_layout
      ,AppAttachmentType.ALL)
      .onResult { code, file ->
          displasyImage(file)
      }
      .prepare {
          //Optional:you can pass you authority  
          //this.authority = ""
          //Optional:you can pass you file
          //this.cameraPictureFile = File("...")
      }
      .onExplainRequired { permission, reTry ->
          //to show user an explain, then call reTry()
          reTry()
      }
      .show(supportFragmentManager,"AppAttachmentDialog")
```

# 

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    AppAttachmentDialog.onActivityResult(requestCode,resultCode,data,this) { code, file ->
        displasyImage(file)
}
```

# 
```kotlin
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    AppAttachmentDialog.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
}
```

# 
Enjoy using app-attachment library,report any bugs you found, or even drop me an email gg.goo.mobile@gmail.com



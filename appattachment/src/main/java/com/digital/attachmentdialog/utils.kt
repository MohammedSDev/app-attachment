package com.digital.attachmentdialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ext.SdkExtensions.getExtensionVersion
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.*
import java.lang.Exception
import java.net.URLConnection
import java.util.*


//region Gallery,Camera & files

/**
 * try get file mime type
 * */
fun getMimeType(url: String): String? {
  return runCatching {
    if (url.isEmpty()) return null
    val file = File(url)
    val ins = BufferedInputStream(FileInputStream(file))
    val mimeType: String? = URLConnection.guessContentTypeFromStream(ins)

    if (!mimeType.isNullOrEmpty())
      return mimeType

    var type: String? = null

    val extension: String? = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
      type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
    } else {
//		type = "*/*"
      type = null
    }
    return type
  }.getOrNull()

  //val uri = Uri.parse(url)
  /*if (context != null
      && uri.scheme?.equals(ContentResolver.SCHEME_CONTENT) == true
  ) {
      val cr = context.applicationContext.contentResolver
      type = cr.getType(uri)
  }
  else*/
}

fun getMimeType(context: Context?, uri: Uri?): String? {
  context ?: return null
  uri ?: return null
  return runCatching {
    val fileDes = context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
    if (fileDes != null) {
      val ins = BufferedInputStream(FileInputStream(fileDes))
      val mimeType: String? = URLConnection.guessContentTypeFromStream(ins)

      if (!mimeType.isNullOrEmpty())
        return mimeType

    }
    var type: String? = null

    val extension: String? = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    if (extension != null) {
      type =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ENGLISH))
    } else {
      type = null
    }
    return type
  }.getOrNull()
}


/**
 * This method is used to call inbuild camera of device
 *
 * @param context     the context
 * @param requestCode the request code
 * @param imagePath   the image path
 */

fun openFileManager(context: Activity, requestCode: Int, fragment: Fragment? = null) {
//	openDocumentManager(context, requestCode, fragment)
//	return
  val intent = Intent(Intent.ACTION_GET_CONTENT)
  intent.type = "application/*"
  intent.addCategory(Intent.CATEGORY_OPENABLE)

  try {
    if (fragment != null)
      fragment.startActivityForResult(intent, requestCode)
    else
      context.startActivityForResult(intent, requestCode)
  } catch (ex: Exception) {
    Toast.makeText(
      context,
      "Kindly, install any File Manager application first",
      Toast.LENGTH_LONG
    ).show()
  }

}

fun openDocumentManager(context: Activity, requestCode: Int, fragment: Fragment? = null) {
  val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
  intent.type = "application/*"
  intent.addCategory(Intent.CATEGORY_OPENABLE)

  try {
    if (fragment != null)
      fragment.startActivityForResult(intent, requestCode)
    else
      context.startActivityForResult(intent, requestCode)
  } catch (ex: Exception) {
    Toast.makeText(
      context,
      "Kindly, install any File Manager application first",
      Toast.LENGTH_LONG
    ).show()
  }

}

fun openCamera(
  context: Activity,
  requestCode: Int,
  authority: String,
  imagePath: File,
  fragment: Fragment? = null
) {
//    val imagePath = imagePath ?: File(context.cacheDir, "camera.png")
  val intent = Intent(
    MediaStore.ACTION_IMAGE_CAPTURE
  )

  val mUri = FileProvider.getUriForFile(context, authority, imagePath)
  //context.grantUriPermission(context.packageName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
  intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
  intent.putExtra("return-data", true)
  //intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
  //https://stackoverflow.com/questions/24467696/android-file-provider-permission-denial
  if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
    try {
      intent.clipData = ClipData.newRawUri("", mUri)
      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  if (fragment != null)
    fragment.startActivityForResult(intent, requestCode)
  else
    context.startActivityForResult(intent, requestCode)
}

/**
 * Gallery.
 *
 * @param context     the context
 * @param requestCode the request code
 * @param fragment the host fragment if you call from fragment
 */
//resources
//https://medium.com/tech-takeaways/android-13-photo-picker-with-the-activity-result-api-b4a74572e354
//https://stackoverflow.com/questions/74468281/read-external-storage-is-always-denied-on-android-13-device
@SuppressLint("InlinedApi")
fun openGallery(
  context: Activity,
  requestCode: Int,
  fragment: Fragment? = null,
  isMultiSelection: Boolean = false
) {
  val intent = if (isPhotoPickerAvailable())
    Intent(MediaStore.ACTION_PICK_IMAGES)
  else Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//  val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//  intent.addCategory(Intent.CATEGORY_OPENABLE)
  intent.type = "image/*"
  if (isMultiSelection)
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
  if (fragment != null)
    fragment.startActivityForResult(intent, requestCode)
  else
    context.startActivityForResult(intent, requestCode)
}


//this function check if file is from GoogleDrive.. get its path, otherwise call FileUtils.getPath()/
fun getDrivePath(context: Context, uri: Uri?, parentFile: File): AppAttachModel? {
  val file: File
  if (uri != null) {
    //Google apps'Google Drive'
    if (uri.authority?.startsWith("com.google.android.apps.") == true) {
      val info = getPathInfo(context, uri)
      try {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {

          file = File(
            parentFile,
            "file_" + System.currentTimeMillis() + "." + MimeTypeMap.getSingleton()
              .getExtensionFromMimeType(
                info?.mimeType ?: ""
              )
          )
          val outs: OutputStream = FileOutputStream(file)
          val bytes = ByteArray(1024)
          var length = -1
          while (true) {
            length = inputStream.read(bytes)
            if (length != -1)
              outs.write(bytes, 0, length);
            else
              break
          }
          //close Streams
          inputStream.close();
          outs.close();
//                    println(text = "log_file, getPath: Done Get File From GoogleApp");
          return info?.copy(path = file.absolutePath) ?: AppAttachModel(file.absolutePath)
        } else
          return info
      } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
      } catch (e: IOException) {
        e.printStackTrace()
        return null
      }
    } else
      return getPath(context, uri)
  } else
    return null
}


/**
 * Get a file path from a Uri. This will get the the path for Storage Access
 * Framework Documents, as well as the _data field for the MediaStore and
 * other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri     The Uri to query.
 * @return the path
 * @author paulburke
 */

@SuppressLint("NewApi")
internal fun getPathOldVersion(context: Context, uri: Uri?): String? {
  if (uri == null) return null
  val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
  val isRorHigher = Build.VERSION.SDK_INT >= 30//11
  val isQ = Build.VERSION.SDK_INT == 29//10

  //scooped storage enable(os Q & higher)
  if (isRorHigher) return null
  else if (isQ && !Environment.isExternalStorageLegacy()) return null
  else
  /*if (isQ && !Environment.isExternalStorageLegacy()) {
  //TODO support reset file types ,currently : image is supported.
  val des = context!!.contentResolver.openFileDescriptor(uri, "r")//r:Read
  val bitmap = BitmapFactory.decodeFileDescriptor(des?.fileDescriptor)
  des?.close()
  //convert bitmap to file
  val outputStream = ByteArrayOutputStream()
  val temporaryFile =
    File.createTempFile(System.currentTimeMillis().toString(), ".png")
  bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
  FileOutputStream(temporaryFile).run {
    write(outputStream.toByteArray())
    flush()
    close()
  }
  return temporaryFile.absolutePath
} else*/
  // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      // ExternalStorageProvider
      if (isExternalStorageDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val type = split[0]

        if ("primary".equals(type, ignoreCase = true)) {
          return Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
        } else {
          // TODO handle non-primary volumes
          //https://stackoverflow.com/questions/11281010/how-can-i-get-external-sd-card-path-for-android-4-0
          //https://stackoverflow.com/questions/32413305/how-to-get-sdcardsecondary-storage-path-in-android

          println("-------------------------------getPath()")
          println("-------------------------------getPath()")
          println("-------------------------------getPath()")
          println(type)
          println(split[0])
          println(split[1])
          println(split)
          println(split.toString())
          println("-------------------------------getPath()")
        }

      } else if (isDownloadsDocument(uri)) {

        val id = DocumentsContract.getDocumentId(uri)
        val contentUri = ContentUris.withAppendedId(
          Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
        )

        return getDataColumn(context, contentUri, null, null)
      } else if (isMediaDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val type = split[0]

        var contentUri: Uri? = null
        if ("image" == type) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if ("video" == type) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if ("audio" == type) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "_id=?"
        val selectionArgs = arrayOf<String>(split[1])
        return getDataColumn(context, contentUri, selection, selectionArgs)
      }// MediaProvider
      // DownloadsProvider
    } else if ("content".equals(uri.getScheme()!!, ignoreCase = true)) {
      return getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.getScheme()!!, ignoreCase = true)) {
      return uri.getPath()
    }// File
  // MediaStore (and general)

  return null
}

data class AppAttachModel(
  val path: String? = null,
  val name: String? = null,
  val mimeType: String? = null,
  val extension: String? = null,
  val size: String? = null,
  val isMulti: Boolean = false,
  val models: List<AppAttachModel>? = null
)

fun getPath(context: Context, uri: Uri?): AppAttachModel? {
  uri ?: return null
  val info = getPathInfo(context, uri)

  val data = ""// getDataColumn(context, uri, null, null)
  val data1 by lazy { runCatching { getPathOldVersion(context, uri) }.getOrNull() }
  val data2 by lazy { runCatching { getInputStreamCopyFilePath(context, uri, info) }.getOrNull() }
  return if (data?.isNotEmpty() == true)
    info?.copy(path = data) ?: AppAttachModel(data)
  else if (data1?.isNotEmpty() == true)
    info?.copy(path = data1) ?: AppAttachModel(data1)
  else {
    info?.copy(path = data2) ?: AppAttachModel(data2)
  }

}

private fun getPathInfo(context: Context, uri: Uri): AppAttachModel? {

  return runCatching {
    val name: String?
    val size: String?
    val mimeType: String?
    val extension: String?
//get file path of open document (pdf):
    val c = context.contentResolver.query(uri, null, null, null, null)
    return if (c?.moveToFirst() == true) {
      name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
      val sizei = c.getColumnIndex(OpenableColumns.SIZE)
      val mimei = c.getColumnIndex("mime_type")
      size = if (c.isNull(sizei)) null else c.getString(sizei)
      mimeType = if (!c.isNull(mimei)) c.getString(mimei) else getMimeType(context, uri)
        ?: getMimeType(uri.toString())
      c.close()
      extension = if (!mimeType.isNullOrEmpty())
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
      else
        null
      AppAttachModel(null, name, mimeType, extension, size)
    } else {
      c?.close()
      null
    }
  }.getOrNull()
}

private fun getInputStreamCopyFilePath(
  context: Context,
  uri: Uri,
  info: AppAttachModel?
): String? {

  val str = context.contentResolver.openInputStream(uri) ?: return null
  val suffix = if (info?.extension.isNullOrEmpty() || info?.extension?.startsWith(".") == true)
    "t_fi" + info?.extension
  else
    "t_fi." + info?.extension
  val outFile = File.createTempFile("pre_", suffix, context.cacheDir)
  val fStream = FileOutputStream(outFile)
  val buffer = ByteArray(1024)
  var result = 0
  while (true) {
    result = str.read(buffer)
    if (result != -1) {
      fStream.write(buffer, 0, result)
    } else
      break
  }
  str.close()
  fStream.close()
  return if (outFile.length() <= 0)
    null
  else outFile.absolutePath
}
private fun getInputStreamCopyFilePath2(
  context: Context,
  uri: Uri,
  info: AppAttachModel?
): String? {
  val str = context.contentResolver.openInputStream(uri) ?: return null
  val suffix = if (info?.extension.isNullOrEmpty() || info?.extension?.startsWith(".") == true)
    "t_fi" + info?.extension
  else
    "t_fi." + info?.extension
  val outFile = File.createTempFile("pre_", suffix, context.cacheDir)
  val inBuffer = BufferedInputStream(str)
  val outBuffer = BufferedOutputStream(FileOutputStream(outFile))
  val buffer = ByteArray(1024)
  var result = 0
  while (true) {
    result = inBuffer.read(buffer)
    if (result == -1) {
      outBuffer.flush()
      break
    }
    outBuffer.write(buffer)
    outBuffer.flush()

  }
  str.close()
  inBuffer.close()
  outBuffer.close()

  return if (outFile.length() <= 0)
    null
  else outFile.absolutePath
}


/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
private fun getDataColumn(
  context: Context, uri: Uri?, selection: String?,
  selectionArgs: Array<String>?
): String? {

  var cursor: Cursor? = null
  val column = "_data"
  val column2 = "path"
  val projection = arrayOf(column, column2)

  try {
    cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
    if (cursor != null && cursor.moveToFirst()) {
      var columnIndex = cursor.getColumnIndex(column)
      if (columnIndex == -1) {
        columnIndex = cursor.getColumnIndex(column2)
      }
      return cursor.getString(columnIndex)
    }
  } catch (e: Exception) {
    return null
  } finally {
    cursor?.close()
  }
  return null
}


/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean {
  return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
  return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean {
  return "com.android.providers.media.documents" == uri.authority
}


/**
 * compress bitmap & reduce size
 * @param compress : output bitmap quality.(max:100)
 * */
fun compressBitmapFile(filePath: String?, context: Context?, compress: Int): File? {
  if (filePath.isNullOrEmpty()) return null
  context ?: return null
  runCatching {
    val bitmap = BitmapFactory.decodeFile(filePath)
    val b3 = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, compress, b3)

    val des = File(context.cacheDir, System.currentTimeMillis().toString())
    des.createNewFile()
    val fos = FileOutputStream(des)
    fos.write(b3.toByteArray())
    fos.flush()
    fos.close()
    return des
  }
  return null
}

/**
 * @param targetH
 * @param targetW
 * the dimensions of the View
 * */
fun scaleDonwImage(imagePath: String, targetW: Int, targetH: Int): Bitmap? {
  val bmOptions = BitmapFactory.Options().apply {
    // Get the dimensions of the bitmap
    inJustDecodeBounds = true

    val photoW: Int = outWidth
    val photoH: Int = outHeight

    // Determine how much to scale down the image
    val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

    // Decode the image file into a Bitmap sized to fill the View
    inJustDecodeBounds = false
    inSampleSize = scaleFactor
    //inPurgeable = true
  }
  return BitmapFactory.decodeFile(imagePath, bmOptions)
}

//endregion

/**
 * request open gallery with request required permissions
 *
 * @param activity: the host activity context.
 * @param hostFragment: the host fragment if you call inside fragment.
 * @param explainRequired: Lamda will be called when runtime permission should show explain
 * */
fun openGallery(
  activity: Activity,
  hostFragment: Fragment? = null,
  explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null
) {
  if (Build.VERSION.SDK_INT >= 29)
    openGallery(activity, AppAttachmentDialog.OPEN_GALLARY_REQUEST, hostFragment)
  else
    checkPermission(
      activity,
      arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
      AppAttachmentDialog.GALLERY_PERMISSION_REQUEST_CODE,
      explainRequired,
      hostFragment
    ) {
      openGallery(activity, AppAttachmentDialog.OPEN_GALLARY_REQUEST, hostFragment)
    }
}


/**
 * request open camera with request required permissions
 *
 * @param activity: the host activity context.
 * @param hostFragment: the host fragment if you call inside fragment.
 * @param file: custom file to save image (optional)
 * @param overwrite: false to use newPath every time. if @param file not null..this param has no effect.
 * @param authority: custom authority (optional)
 * @param requestStorageRunTimePermission set true if you pass file in shared storage area.
 * @param explainRequired: Lamda will be called when runtime permission should show explain
 * */
fun openCamera(
  activity: Activity,
  hostFragment: Fragment? = null,
  file: File? = null,
  overwrite: Boolean = false,
  authority: String? = null,
  requestStorageRunTimePermission: Boolean = false,
  explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null
) {

  if (file != null)
    AppAttachmentDialog.cameraPictureFile = file
  else if (AppAttachmentDialog.cameraPictureFile == null || !overwrite)
    AppAttachmentDialog.cameraPictureFile = File(
      activity.cacheDir,
      "pic_${System.currentTimeMillis()}.jpg"
    )

  if (!authority.isNullOrEmpty())
    AppAttachmentDialog.authority = authority
  else if (AppAttachmentDialog.authority.isEmpty())
    AppAttachmentDialog.authority = activity.packageName + ".fileprovider"

  val permissions = if (requestStorageRunTimePermission)
    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
  else
    arrayOf(Manifest.permission.CAMERA)
  checkPermission(
    activity,
    permissions,
    AppAttachmentDialog.CAMERA_PERMISSION_REQUEST_CODE,
    explainRequired,
    hostFragment
  ) {

    openCamera(
      activity!!,
      AppAttachmentDialog.OPEN_CAMERA_REQUEST,
      AppAttachmentDialog.authority,
      AppAttachmentDialog.cameraPictureFile!!,
      hostFragment
    )
  }

}


/**
 * request open File with request required permissions
 *
 * @param activity: the host activity context.
 * @param hostFragment: the host fragment if you call inside fragment.
 * @param explainRequired: Lamda will be called when runtime permission should show explain
 * */
fun openFileManager(
  activity: Activity,
  hostFragment: Fragment? = null,
  explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null
) {

  checkPermission(
    activity,
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
    AppAttachmentDialog.OTHER_PERMISSION_REQUEST_CODE, explainRequired, hostFragment
  ) {

    openFileManager(activity, AppAttachmentDialog.OPEN_OTHER_REQUEST, hostFragment)
  }
}

//@RequiresApi(Build.VERSION_CODES.M)
//@TargetApi(Build.VERSION_CODES.M)
@SuppressLint("NewApi")
internal fun checkPermission(
  mContext: Activity,
  permission: Array<String>,
  permissionCode: Int,
  explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null,
  hostFragment: Fragment? = null,
  callback: () -> Unit
) {

//	val explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null
  var deniedPermission: String = ""
  permission.forEach {
    if (ContextCompat.checkSelfPermission(mContext, it)
      != PackageManager.PERMISSION_GRANTED
    ) {
      deniedPermission = it
      return@forEach
    }
  }
  if (deniedPermission.isNotEmpty()) {

    // Permission is not granted
    // Should we show an explanation?

    if (ActivityCompat.shouldShowRequestPermissionRationale(mContext, deniedPermission)) {
      // Show an explanation to the user *asynchronously* -- don't block
      // this thread waiting for the user's response! After the user
      // sees the explanation, try again to request the permission.

      explainRequired?.invoke(deniedPermission) {
        hostFragment?.requestPermissions(
          permission,
          permissionCode
        ) ?: mContext.requestPermissions(
          permission,
          permissionCode
        )
      } ?: hostFragment?.requestPermissions(
        permission,
        permissionCode
      ) ?: mContext.requestPermissions(
        permission,
        permissionCode
      )

    } else {
      // No explanation needed, we can request the permission.

      hostFragment?.requestPermissions(
        permission,
        permissionCode
      ) ?: mContext.requestPermissions(
        permission,
        permissionCode
      )
      // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
      // app-defined int constant. The callback method gets the
      // result of the request.
    }
  } else {
    callback()
  }

}

//check if the current device supports the Photo Picker
//https://developer.android.com/reference/android/provider/MediaStore#ACTION_PICK_IMAGES
private const val ANDROID_R_REQUIRED_EXTENSION_VERSION = 2
//
//@SuppressLint("NewApi")
fun isPhotoPickerAvailable(): Boolean {
  return when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> true
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
      getExtensionVersion(Build.VERSION_CODES.R) >= ANDROID_R_REQUIRED_EXTENSION_VERSION
    }
    else -> false
  }
}
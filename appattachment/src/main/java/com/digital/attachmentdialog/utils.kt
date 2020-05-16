package com.digital.attachmentdialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.*
import java.lang.Exception
import java.net.URLConnection


//region Gallery,Camera & files

/**
 * try get file mime type
 * */
fun getMimeType(url: String): String? {
	if (url.isEmpty()) return null
	val file = File(url)
	val ins = BufferedInputStream(FileInputStream(file))
	val mimeType = URLConnection.guessContentTypeFromStream(ins)

	if (mimeType.isNotEmpty())
		return mimeType

	var type: String? = null

	val extension = MimeTypeMap.getFileExtensionFromUrl(url)
	if (extension != null) {
		type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
	} else {
		type = "*/*"
	}
	return type

	//val uri = Uri.parse(url)
	/*if (context != null
			&& uri.scheme?.equals(ContentResolver.SCHEME_CONTENT) == true
	) {
			val cr = context.applicationContext.contentResolver
			type = cr.getType(uri)
	}
	else*/
}


/**
 * This method is used to call inbuild camera of device
 *
 * @param context     the context
 * @param requestCode the request code
 * @param imagePath   the image path
 */

fun openFileManager(context: Activity, requestCode: Int, fragment: Fragment? = null) {
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
 */
fun openGallery(context: Activity, requestCode: Int, fragment: Fragment? = null) {
	val intent =
//        Intent()
		Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	//startActivityForResult(intentGallery, 1);
	intent.type = "image/*"
//    intent.action = Intent.ACTION_GET_CONTENT
	if (fragment != null)
		fragment.startActivityForResult(intent, requestCode)
	else
		context.startActivityForResult(intent, requestCode)
}


//this function check if file is from GoogleDrive.. get its path, otherwise call FileUtils.getPath()/
fun getDrivePath(context: Context, uri: Uri?, parentFile: File): String? {
	val file: File
	if (uri != null) {
		//Google apps'Google Drive'
		if (uri.authority?.startsWith("com.google.android.apps.") == true) {
			try {
				val inputStream = context.getContentResolver().openInputStream(uri);
				if (inputStream != null) {
					val query = context.getContentResolver().query(uri, null, null, null, null);
					var mimeType: String? = null;
					if (query != null) {
						query.moveToFirst();
						mimeType = query.getString(query.getColumnIndex("mime_type"));
					}
					file = File(
						parentFile,
						"file_" + System.currentTimeMillis() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(
							mimeType
						)
					);
					val outs: OutputStream = FileOutputStream(file);
					val bytes = ByteArray(1024)
					var length = -1;
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
					return file.getPath();
				} else
					return null;
			} catch (e: FileNotFoundException) {
				e.printStackTrace();
				return null;
			} catch (e: IOException) {
				e.printStackTrace();
				return null;
			}
		} else
			return getPath(context, uri);
	} else
		return null;
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
fun getPath(context: Context, uri: Uri?): String? {
	if (uri == null) return null
	val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
	val isQorHigher = Build.VERSION.SDK_INT >= 29

	//scooped storage enable(os Q & higher)
	if (isQorHigher && !Environment.isExternalStorageLegacy()) {
		//TODO support reset file types ,currently : image is supported.
		val des = context!!.contentResolver.openFileDescriptor(uri, "r")//r:Read
		val bitmap = BitmapFactory.decodeFileDescriptor(des?.fileDescriptor)
		des?.close()
		//convert bitmap to file
		val outputStream = ByteArrayOutputStream()
		val temporaryFile =
			File.createTempFile(System.currentTimeMillis().toString(), "image")
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
		FileOutputStream(temporaryFile).run {
			write(outputStream.toByteArray())
			flush()
			close()
		}
		return temporaryFile.absolutePath
	} else
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
	val projection = arrayOf(column)

	try {
		cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
		if (cursor != null && cursor.moveToFirst()) {
			val column_index = cursor.getColumnIndexOrThrow(column)
			return cursor.getString(column_index)
		}
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
package com.digital.attachmentdialog

import android.content.Context
import android.graphics.*
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
//https://gist.github.com/vipulasri/0cd97d012934531f1266
private val maxHeight = 1024.0f
private val maxWidth = 1024.0f
fun compressImage(context: Context, imagePath: String): String? {
  var scaledBitmap: Bitmap?

  val options = BitmapFactory.Options()
  options.inJustDecodeBounds = true
  @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
  var bmp: Bitmap? = BitmapFactory.decodeFile(imagePath, options)

  var actualHeight = options.outHeight
  var actualWidth = options.outWidth

  var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
  val maxRatio = maxWidth / maxHeight

  if (actualHeight > maxHeight || actualWidth > maxWidth) {
    if (imgRatio < maxRatio) {
      imgRatio = maxHeight / actualHeight
      actualWidth = (imgRatio * actualWidth).toInt()
      actualHeight = maxHeight.toInt()
    } else if (imgRatio > maxRatio) {
      imgRatio = maxWidth / actualWidth
      actualHeight = (imgRatio * actualHeight).toInt()
      actualWidth = maxWidth.toInt()
    } else {
      actualHeight = maxHeight.toInt()
      actualWidth = maxWidth.toInt()

    }
  }

  options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
  options.inJustDecodeBounds = false
  options.inDither = false
  options.inPurgeable = true
  options.inInputShareable = true
  options.inTempStorage = ByteArray(16 * 1024)

  try {
    bmp = BitmapFactory.decodeFile(imagePath, options)
  } catch (exception: OutOfMemoryError) {
    exception.printStackTrace()
    return null
  }

  try {
    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)
  } catch (exception: OutOfMemoryError) {
    exception.printStackTrace()
    return null
  }

  val ratioX = actualWidth / options.outWidth.toFloat()
  val ratioY = actualHeight / options.outHeight.toFloat()
  val middleX = actualWidth / 2.0f
  val middleY = actualHeight / 2.0f

  val scaleMatrix = Matrix()
  scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

  val canvas = Canvas(scaledBitmap)
  canvas.setMatrix(scaleMatrix)
  canvas.drawBitmap(bmp, middleX - bmp!!.width / 2, middleY - bmp.height / 2, Paint(FILTER_BITMAP_FLAG))

  bmp.recycle()

  val exif: ExifInterface
  try {
    exif = ExifInterface(imagePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    val matrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }
    scaledBitmap = Bitmap.createBitmap(scaledBitmap!!, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
  } catch (e: IOException) {
    e.printStackTrace()
  }

  val out: FileOutputStream?
  val filepath = getFilename(context)
  try {
    out = FileOutputStream(filepath)
    //write the compressed bitmap at the destination specified by filename.
    scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)

  } catch (e: FileNotFoundException) {
    e.printStackTrace()
  }

  return filepath
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
  val height = options.outHeight
  val width = options.outWidth
  var inSampleSize = 1

  if (height > reqHeight || width > reqWidth) {
    val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
    val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
    inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
  }
  val totalPixels = (width * height).toFloat()
  val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

  while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
    inSampleSize++
  }

  return inSampleSize
}

private fun getFilename(context: Context): String {
//  val mediaStorageDir = File("${Environment.getExternalStorageDirectory()}/Android/data/${context.applicationContext.packageName}/Files/Compressed")
  val mediaStorageDir = File(context.cacheDir,"Compressed")
  // Create the storage directory if it does not exist
  if (!mediaStorageDir.exists()) {
    mediaStorageDir.mkdirs()
  }

  val mImageName = "IMG_" + System.currentTimeMillis().toString() + ".jpg"
  return mediaStorageDir.absolutePath + "/" + mImageName
}
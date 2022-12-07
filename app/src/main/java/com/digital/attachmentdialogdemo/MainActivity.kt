package com.digital.attachmentdialogdemo

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.digital.attachmentdialog.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)



    tvOne.setOnClickListener {

    }

    tvTwo.setOnClickListener {
      val f = File(filesDir, "two.jpg")//Okay
      openAttachmentDialog(f)
//            }
    }
    tvThree.setOnClickListener {
      val f = File(getExternalFilesDir(null), "three.jpg")//Okay
      openAttachmentDialog(f)
    }

  }

  private fun openAttachmentDialog(file: File? = null, requestPermission: Boolean = false) {
    AppAttachmentDialog(
      R.layout.attachment_type_dialog_layout, AppAttachmentType.ALL
    )
      .prepare {
        //Optional:you can pass you authority
        //this.authority = ""
        //Optional:you can pass you file
        this.isMultiSelection = false
        this.cameraPictureFile = file
        this.requestStorageRunTimePermission = requestPermission
      }
      .onExplainRequired { permission, reTry ->
        //to show user an explain, then call reTry()
        reTry()
      }
      .show(supportFragmentManager, "AppAttachmentDialog")
  }

  private fun printFilePath() {
    val path = "Android/data/.nomedia"
    val f = File(path)
    println("file: ${f.name}")
    println("file: ${f.exists()}")
    println("file: ${f.length()}")
    println("file: ${f.absolutePath}")
  }

  fun saveToInternalStorage(bitmapImage: Bitmap ,name:String? = null): File? {
    val cw = ContextWrapper(this)
    // path to /data/data/yourapp/app_data/imageDir
//    val directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
    val directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)


    // Create imageDir
    val mypath = File(directory, name?:"profile.jpg")
    Log.d("file", "path to save : $mypath")

    var fos: FileOutputStream? = null

    try {
      fos = FileOutputStream(mypath);
      // Use the compress method on the BitMap object to write image to the OutputStream
      bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
    } catch (e: Exception) {
      e.printStackTrace();
    } finally {
      try {
        fos?.close();
      } catch (e: IOException) {
        e.printStackTrace();
      }
    }
    return directory
  }


  fun displasyImage(file: File?, uri: Uri? = null) {
    println("file")
    println("$file")
    println("length: ${file?.length()}")
    println("name: ${file?.name}")
    println("isAbsolute: ${file?.isAbsolute}")
    println("exists: ${file?.exists()}")
    println("canWrite: ${file?.canWrite()}")
    println("canRead: ${file?.canRead()}")
    println("setReadOnly: ${file?.setReadOnly()}")
    println("canWrite: ${file?.canWrite()}")
    println("file.absolutePath: ${file?.absolutePath}")
    println("file")
    println("file")
    file ?: return
    if (file.exists()) {
      println("size before: ${file.length()}")
//      val myBitmap = BitmapFactory.decodeFile(file.absolutePath);
//      saveToInternalStorage(myBitmap)
      val comp = compressBitmapFile(file.absolutePath, this, 25)
      val comp2 = File(compressImage(this,file.absolutePath))
      println("size after compress: ${comp?.length()}")
      println("size after compress 2: ${comp2?.length()}")


//      val myBitmapCompressed = BitmapFactory.decodeFile(comp!!.absolutePath);
//      saveToInternalStorage(myBitmapCompressed,"comp.jpg")
      Glide.with(this).load(comp2?.absolutePath).into(image)
//          Glide.with(this).load(file.absolutePath).into(image)//failed
      return
      Handler().postDelayed({
        val myBitmap = BitmapFactory.decodeFile(file.absolutePath);
        image.setImageBitmap(myBitmap)
        Toast.makeText(this, "image BitmapFactory", Toast.LENGTH_LONG).show()

      }, 5000)

      Handler().postDelayed({

        val comp = compressBitmapFile(file.absolutePath, this, 70)
        val bb = BitmapFactory.decodeFile(comp?.absolutePath)
        image.setImageBitmap(bb)
        Toast.makeText(this, "image after compress ", Toast.LENGTH_LONG).show()

      }, 8000)
    } else {
      Toast.makeText(this, "image file not exist.", Toast.LENGTH_LONG).show()

    }
  }


  @SuppressLint("NewApi")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    println("ACtivity.onActivityResult")
    if (isFinishing) return
    AppAttachmentDialog.onActivityResult(requestCode, resultCode, data, this) { code, file, a ->
      val isQ = Build.VERSION.SDK_INT >= 29
      Log.d("file_", "onActivityResult: size:${a?.size}")
      Log.d("file_", "onActivityResult: size:${file?.length()}")

//            if (isQ && !Environment.isExternalStorageLegacy()) {
//                println("isQ :$isQ ")
//                val des = contentResolver.openFileDescriptor(data?.data!!, "r")//r:Read
//
//                val f = BitmapFactory.decodeFileDescriptor(des?.fileDescriptor)
//                des?.close()
//                image.setImageBitmap(f)
//                return@onActivityResult
//            }

//            val des = contentResolver.openFileDescriptor(data?.data!!, "r")//r:Read
//            val bitmap = BitmapFactory.decodeFileDescriptor(des?.fileDescriptor)
//            des?.close()
//                image.setImageBitmap(bitmap)
//            //convert bitmap to file
//            val outputStream = ByteArrayOutputStream()
//            val temporaryFile = File.createTempFile(System.currentTimeMillis().toString(),"image")
//            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
//            FileOutputStream(temporaryFile).run {
//                write(outputStream.toByteArray())
//                flush()
//                close()
//            }
//            //path = temporaryFile.absolutePath
//            displasyImage(File(temporaryFile.absolutePath))
//            return@onActivityResult
      displasyImage(file ?: a?.path?.let { File(it) })
    }

  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    println("ACtivity.onRequestPermissionsResult")
    AppAttachmentDialog.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
  }


  private fun writeToFile(file: File) {
    Thread {
      log("writeToFile:file:${file.absolutePath}")
      log("writeToFile:start")
      val text = "hello From Android2"
      val writter = FileOutputStream(file)
      writter.write(text.toByteArray())
      writter.flush()
      writter.close()
      log("writeToFile:done")
    }.start()
  }

  private fun readFromFile(file: File, wait: Long = 0) {

    log("readFromFile:file:${file.absolutePath}")
    Thread {
      if (wait > 0)
        Thread.sleep(wait)
      log("readFromFile:start")
      val reader = FileInputStream(file)
      val container = ByteArray(1024)
      while (reader.read(container) != -1) {
        log(String(container))//StandardCharsets.UTF_8
      }
      reader.close()
      log("readFromFile:Done")
    }.start()
    //openFileInput(file.name)
  }


}

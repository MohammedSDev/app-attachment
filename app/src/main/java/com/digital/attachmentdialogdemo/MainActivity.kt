package com.digital.attachmentdialogdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import com.digital.attachmentdialog.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        supportFragmentManager.beginTransaction().add(R.id.root,TestFragment()).commit()

        tvOne.setOnClickListener {

        }

        tvTwo.setOnClickListener {
            val f = File(filesDir, "two.jpg")//Okay
            openAttachmentDialog(f)
        }
        tvThree.setOnClickListener {
            val f = File(getExternalFilesDir(null), "three.jpg")//Okay
            openAttachmentDialog(f)
        }

    }

    private fun openAttachmentDialog(file:File? = null,requestPermission:Boolean = false) {
        AppAttachmentDialog(
            R.layout.attachment_type_dialog_layout
            , AppAttachmentType.ALL
        )
            .onResult { code, file ->
                println("onResult called")
                displasyImage(file)
            }
            .prepare {
                //Optional:you can pass you authority
                //this.authority = ""
                //Optional:you can pass you file
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


    fun displasyImage(file: File?) {
        println("file")
        println("$file")
        println("${file?.length()}")
        println("${file?.name}")
        println("file")
        println("file")
        file ?: return
        if (file.exists()) {
            println("size before: ${file.length()}")
            val myBitmap = BitmapFactory.decodeFile(file.absolutePath);
            image.setImageBitmap(myBitmap)


            Handler().postDelayed({

                val comp = compressBitmapFile(file.absolutePath, this, 70)
                val bb = BitmapFactory.decodeFile(comp?.absolutePath)
                image.setImageBitmap(bb)
                Toast.makeText(this, "image after compress ", Toast.LENGTH_LONG).show()

            }, 5000)
        }
    }


    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("ACtivity.onActivityResult")
        if (isFinishing) return
        AppAttachmentDialog.onActivityResult(requestCode, resultCode, data, this) { code, file ->
            val isQ = Build.VERSION.SDK_INT >= 29

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
            displasyImage(file)
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

package com.digital.attachmentdialogdemo

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.digital.attachmentdialog.AppAttachmentDialog
import com.digital.attachmentdialog.AppAttachmentType
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        supportFragmentManager.beginTransaction().add(R.id.root,TestFragment()).commit()

//        tvOne.setOnClickListener {
//
//            val path = "Android/data/.nomedia"
//            val f = File(path)
//            println("file: ${f.name}")
//            println("file: ${f.exists()}")
//            println("file: ${f.length()}")
//            println("file: ${f.absolutePath}")
//
////            ActivityCompat.requestPermissions(
////                this,
////                arrayOf(Main),
////                10235
////            )
//            AppAttachmentDialog(R.layout.attachment_type_dialog_layout
//                ,AppAttachmentType.ALL)
//                .onResult { code, file ->
//                    println("onResult called")
//                    displasyImage(file)
//                }
//                .prepare {
//                    //Optional:you can pass you authority
//                    //this.authority = ""
//                    //Optional:you can pass you file
//                    //this.cameraPictureFile = File("...")
//                }
//                .onExplainRequired { permission, reTry ->
//                    //to show user an explain, then call reTry()
//                    reTry()
//                }
//                .show(supportFragmentManager,"AppAttachmentDialog")
//        }

    }


    fun displasyImage(file:File?){
        println("file")
        println("$file")
        println("${file?.length()}")
        println("${file?.name}")
        println("file")
        println("file")
        val file = file?:return
        if(file.exists()){

            val myBitmap = BitmapFactory.decodeFile(file.absolutePath);

            image.setImageBitmap(myBitmap)

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isFinishing) return
//        AppAttachmentDialog.onActivityResult(requestCode,resultCode,data,this) { code, file ->
////            if()
////            displasyImage(file)
//            displasyImage(file)
//        }

        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
//        AppAttachmentDialog.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}

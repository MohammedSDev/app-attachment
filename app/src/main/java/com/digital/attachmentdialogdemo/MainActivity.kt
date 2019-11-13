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



        tvOne.setOnClickListener {

//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Main),
//                10235
//            )
            AppAttachmentDialog(R.layout.attachment_type_dialog_layout
                ,AppAttachmentType.ALL)
                .onResult { code, file ->
                    addImage(file)
                }
                .prepare {

                }
                .onExplainRequired { permission, reTry ->
                    reTry()
                }
                .show(supportFragmentManager,"AppAttachmentDialog")
        }

    }


    fun addImage(file:File?){
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
        super.onActivityResult(requestCode, resultCode, data)
        AppAttachmentDialog.onActivityResult(requestCode,resultCode,data,this) { code, file ->
            addImage(file)
        }

        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
        println("ACtivity.onActivityResult")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("onRequestPermissionsResult----------$requestCode")
        AppAttachmentDialog.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}

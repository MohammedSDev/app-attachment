package com.digital.attachmentdialogdemo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.digital.attachmentdialog.AppAttachmentDialog
import com.digital.attachmentdialog.AppAttachmentType
import java.io.File

class TestFragment : Fragment(){


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(R.layout.activity_main,container,false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.tvOne).setOnClickListener {

            val path = "Android/data/.nomedia"
            val f = File(path)
            println("file: ${f.name}")
            println("file: ${f.exists()}")
            println("file: ${f.length()}")
            println("file: ${f.absolutePath}")

//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Main),
//                10235
//            )

            AppAttachmentDialog(R.layout.attachment_type_dialog_layout,
                this, AppAttachmentType.ALL)
                .onResult { code, file ,a->
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
                .show(childFragmentManager,"AppAttachmentDialog")
        }

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

            view?.findViewById<ImageView>(R.id.image)?.setImageBitmap(myBitmap)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppAttachmentDialog.onActivityResult(requestCode,resultCode,data,context) { code, file,a ->
            //            if()
            displasyImage(file)
            displasyImage(file)
        }

        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
        println("Fragment.onActivityResult")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")
        println("Fragment.onRequestPermissionsResult")

        AppAttachmentDialog.onRequestPermissionsResult(requestCode,permissions,grantResults,activity,this)
    }


}
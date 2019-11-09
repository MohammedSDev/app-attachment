package com.digital.attachmentdialog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.io.File
import com.digital.attachmentdialog.AppAttachmentType.*

enum class AppAttachmentType {
    CAMERA, GALLERY, OTHER,ALL
}

class AppAttachmentDialogConfig{
    var cameraPictureFile: File? = null
    var authority = ""
    var requestCode: Int = -1
    var dismissAfterClick = true

}

class AppAttachmentDialog(@LayoutRes private val layoutRes: Int, private vararg val attachmentTypes: AppAttachmentType) :
    DialogFragment() {


    private fun View.visible() {
        visibility = View.VISIBLE
    }

    private fun View.gone() {
        visibility = View.GONE
    }


    private val cameraPictureFile: File
        get() {
            if (AppAttachmentDialog.cameraPictureFile == null)
                AppAttachmentDialog.cameraPictureFile = config.cameraPictureFile ?:
                    File(activity?.cacheDir, "pic_${System.currentTimeMillis()}.jpg")
            return AppAttachmentDialog.cameraPictureFile!!
        }
    private val authority
        get() = if (config.authority.isEmpty()) context?.packageName + ".fileprovider" else config.authority

//    var requestCode: Int = -1
//    var dismissAfterClick = true

    private val config = AppAttachmentDialogConfig()
    private var onResultCB: ((code: Int, file: File?) -> Unit)? = null
    private var explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null

    fun prepare(block:AppAttachmentDialogConfig.()->Unit):AppAttachmentDialog{
        block(config)
        return this
    }

    fun onResult(onResult: (code: Int, file: File?) -> Unit): AppAttachmentDialog {
        onResultCB = onResult
        return this
    }

    fun onExplainRequired(onExplainRequired: (permission: String, reTry: () -> Unit) -> Unit): AppAttachmentDialog {
        explainRequired = onExplainRequired
        return this
    }

    companion object {
        private val OPEN_CAMERA_REQUEST = 1010
        private val OPEN_GALLARY_REQUEST = 2020
        private val OPEN_OTHER_REQUEST = 3020
        private val CAMERA_PERMISSION_REQUEST_CODE = 9901
        private val GALLERY_PERMISSION_REQUEST_CODE = 9902
        private val OTHER_PERMISSION_REQUEST_CODE = 9903


        private var cameraPictureFile: File? = null

        fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
            context: Context?,
            onResult: (code: Int, file: File?) -> Unit
        ) {
            val activityReqCodeMask = 0x0000ffff
            if (resultCode == AppCompatActivity.RESULT_OK) {
                when {

                    requestCode == OPEN_CAMERA_REQUEST ||
                            requestCode and activityReqCodeMask == OPEN_CAMERA_REQUEST ->
                        onResult.invoke(requestCode,cameraPictureFile)
                    requestCode == OPEN_GALLARY_REQUEST ||
                            requestCode and activityReqCodeMask == OPEN_GALLARY_REQUEST -> {
                        if (data == null) {
                            onResult.invoke(requestCode, null)
                            return
                        }
                        val fileAvatar = File(getPath(context!!, data.data))
                        onResult.invoke(requestCode, fileAvatar)
                    }
                    requestCode == OPEN_OTHER_REQUEST ||
                            requestCode and activityReqCodeMask == OPEN_OTHER_REQUEST -> {
                        if (data == null) {
                            onResult.invoke(requestCode, null)
                            return
                        }

                        Thread {
                            val fileAvatar =
                                File(getDrivePath(context!!, data.data, context!!.cacheDir))
                            Handler(Looper.getMainLooper()).post {
                                onResult.invoke(requestCode, fileAvatar)
                            }
                        }.start()
                    }
                }


            }


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println()
        println()
        println()
        println(context?.packageName)
        println()
        println()
//        setStyle(STYLE_NO_TITLE,R.style.Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
            .also {
                val cameraBtn = it.findViewById<View?>(R.id.appAttachmentDialogCameraBtn)
                val galleryBtn = it.findViewById<View?>(R.id.appAttachmentDialogGalleryBtn)
                val otherBtn = it.findViewById<View?>(R.id.appAttachmentDialogOtherBtn)

                if (attachmentTypes.contains(ALL) || attachmentTypes.contains(CAMERA)) cameraBtn?.visible() else cameraBtn?.gone()
                if (attachmentTypes.contains(ALL) || attachmentTypes.contains(GALLERY)) galleryBtn?.visible() else galleryBtn?.gone()
                if (attachmentTypes.contains(ALL) || attachmentTypes.contains(OTHER)) otherBtn?.visible() else otherBtn?.gone()
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cameraBtn = view.findViewById<View?>(R.id.appAttachmentDialogCameraBtn)
        val galleryBtn = view.findViewById<View?>(R.id.appAttachmentDialogGalleryBtn)
        val otherBtn = view.findViewById<View?>(R.id.appAttachmentDialogOtherBtn)
        galleryBtn?.setOnClickListener {
            checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                GALLERY_PERMISSION_REQUEST_CODE
            ) {
                openGallery(activity!!, OPEN_GALLARY_REQUEST, this)
            }
            if (config.dismissAfterClick) dismiss()
        }

        cameraBtn?.setOnClickListener {
            checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                CAMERA_PERMISSION_REQUEST_CODE
            ) {

                openCamera(activity!!, OPEN_CAMERA_REQUEST, authority, cameraPictureFile!!, this)
            }
            if (config.dismissAfterClick) dismiss()
        }

        otherBtn?.setOnClickListener {
            checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                OTHER_PERMISSION_REQUEST_CODE
            ) {

                openFileManager(activity!!, OPEN_OTHER_REQUEST, this)
            }
            if (config.dismissAfterClick) dismiss()
        }
    }


    private fun checkPermission(permission: String, permissionCode: Int, callback: () -> Unit) {
        val mContext = activity!!
        if (ContextCompat.checkSelfPermission(mContext, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            println("to req")
            println("to req")
            println("to req")
            println(permission)
            println("to req")
            println("to req")


            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(permission)) {
                println("shouldShowRequestPermissionRationale")
                println("shouldShowRequestPermissionRationale")
                println("shouldShowRequestPermissionRationale")
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                explainRequired?.invoke(permission) {
                    requestPermissions(
                        arrayOf(permission),
                        permissionCode
                    )
                } ?: requestPermissions(
                    arrayOf(permission),
                    permissionCode
                )

            } else {
                println("just request permission")
                println("just request permission")
                println("just request permission")
                println("just request permission")
                // No explanation needed, we can request the permission.
                requestPermissions(
                    arrayOf(permission),
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


    override fun onStart() {
        super.onStart()

        dialog?.let {
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(it.window!!.attributes)
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.width = (context!!.resources.displayMetrics.widthPixels * 0.91).toInt()
            //set dialog bottom
            lp.gravity = Gravity.CENTER
            it.window?.attributes = lp
//            it.window?.setBackgroundDrawable(ColorDrawable(Color.YELLOW))
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    fun showDialog(
        manager: FragmentManager,
        requestCode: Int = -1,
        tag: String? = javaClass.simpleName
    ) {
        config.requestCode = requestCode
        super.show(manager, tag)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        println("onRequestPermissionsResult")
        println("onRequestPermissionsResult")
        println("onRequestPermissionsResult")
        println("onRequestPermissionsResult")
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openCamera(
                        activity!!,
                        OPEN_CAMERA_REQUEST,
                        authority,
                        cameraPictureFile!!,
                        this
                    )
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            GALLERY_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openGallery(activity!!, OPEN_GALLARY_REQUEST, this)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            OTHER_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openFileManager(activity!!, OPEN_OTHER_REQUEST, this)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activity?.isFinishing != false) return
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode, resultCode, data, context, onResultCB ?: { code, file -> })


    }

    /*
    *         if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                OPEN_CAMERA_REQUEST -> onResultCB?.invoke(requestCode, cameraPictureFile)
                OPEN_GALLARY_REQUEST -> {
                    if (data == null) {
                        onResultCB?.invoke(requestCode, null)
                        return
                    }
                    val fileAvatar = File(getPath(activity!!, data.data))
                    onResultCB?.invoke(requestCode, fileAvatar)
                }
                OPEN_OTHER_REQUEST -> {
                    if (data == null) {
                        onResultCB?.invoke(requestCode, null)
                        return
                    }

                    Thread {
                        val fileAvatar =
                            File(getDrivePath(activity!!, data.data, context!!.cacheDir))
                        Handler(Looper.getMainLooper()).post {
                            onResultCB?.invoke(requestCode, fileAvatar)
                        }
                    }.start()
                }
            }


        }
    * */
}

operator fun AppAttachmentDialogConfig.invoke(function: () -> Unit) {
}
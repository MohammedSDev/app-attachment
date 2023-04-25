package com.digital.attachmentdialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.*
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.io.File
import com.digital.attachmentdialog.AppAttachmentType.*
import java.io.Serializable

sealed class AppAttachmentType(val value: Int) : Serializable {
  object CAMERA : AppAttachmentType(1)
  object GALLERY : AppAttachmentType(2)
  object OTHER : AppAttachmentType(3)
  object ALL : AppAttachmentType(4)

  companion object {
    fun getByValue(value: Int): AppAttachmentType {
      return when (value) {
        CAMERA.value -> CAMERA
        GALLERY.value -> GALLERY
        OTHER.value -> OTHER
        else -> ALL
      }
    }
  }
}

class AppAttachmentDialogConfig {
  var cameraPictureFile: File? = null
  var authority: String? = null
  var requestCode: Int? = null
  var isMultiSelection: Boolean = false
//    internal set
  /**
   * dismiss dialog after user choose.
   * */
  var dismissAfterClick = true

  /**
   * make dialog with 0.91% of screen.
   * */
  var requestWithAtMost = true

  /**
   * check & request storage runtime permission.
   * note: if you specific custom file path in shared storage. set this to true
   *
   * */
  var requestStorageRunTimePermission = false
  var requestStorageRunTimePermissionForGallery = true

}

class AppAttachmentDialog() :
  DialogFragment() {

  private val LAYOUT_RES_BUNDLE_KEY = "bun_lay"
  private val ATTACHMENT_TYPES_INT_ARRAY_BUNDLE_KEY = "bun_typ"
  private val layoutRes: Int get() = getLayoutFromBundle()
  private val attachmentTypes: Array<out AppAttachmentType>
    get() = getAppAttachmentTypesFromBundle() ?: arrayOf(ALL)


  constructor(@LayoutRes layoutRes: Int, vararg attachmentTypes: AppAttachmentType) : this() {
    //save in argument
    arguments = arguments ?: Bundle()
    arguments?.putInt(LAYOUT_RES_BUNDLE_KEY, layoutRes)
    arguments?.putIntArray(
      ATTACHMENT_TYPES_INT_ARRAY_BUNDLE_KEY,
      attachmentTypes.map { it.value }.toIntArray()
    )
  }

  constructor(
    @LayoutRes layoutRes: Int, frag: Fragment, vararg attachmentTypes: AppAttachmentType
  ) : this(layoutRes, *attachmentTypes) {
    hostFragment = frag
  }

  private fun View.visible() {
    visibility = View.VISIBLE
  }

  private fun View.gone() {
    visibility = View.GONE
  }


  private var hostFragment: Fragment? = null
  private val cameraPictureFile: File
    get() {
      if (AppAttachmentDialog.cameraPictureFile == null)
        AppAttachmentDialog.cameraPictureFile = config.cameraPictureFile ?: File(
          activity?.cacheDir,
          "pic_${System.currentTimeMillis()}.jpg"
        )
      return AppAttachmentDialog.cameraPictureFile!!
    }
  private val authority: String
    get() {
      if (AppAttachmentDialog.authority.isEmpty())
        AppAttachmentDialog.authority =
          config.authority ?: context?.packageName + ".fileprovider"
      return AppAttachmentDialog.authority
    }

//    var requestCode: Int = -1
//    var dismissAfterClick = true

  private val config = AppAttachmentDialogConfig()
  private var onResultCB: ((code: Int, file: File?, info: AppAttachModel?) -> Unit)? = null
  private var explainRequired: ((permission: String, reTry: () -> Unit) -> Unit)? = null

  fun prepare(block: AppAttachmentDialogConfig.() -> Unit): AppAttachmentDialog {
    block(config)
    return this
  }

  fun onResult(onResult: (code: Int, file: File?, info: AppAttachModel?) -> Unit): AppAttachmentDialog {
    onResultCB = onResult
    return this
  }

  fun onExplainRequired(onExplainRequired: (permission: String, reTry: () -> Unit) -> Unit): AppAttachmentDialog {
    explainRequired = onExplainRequired
    return this
  }

  companion object {
    internal val OPEN_CAMERA_REQUEST = 1010
    internal val OPEN_GALLARY_REQUEST = 2020
    internal val OPEN_OTHER_REQUEST = 3020
    internal val CAMERA_PERMISSION_REQUEST_CODE = 9901
    internal val GALLERY_PERMISSION_REQUEST_CODE = 9902
    internal val OTHER_PERMISSION_REQUEST_CODE = 9903


    internal var cameraPictureFile: File? = null
    internal var authority: String = ""

    fun onActivityResult(
      requestCode: Int,
      resultCode: Int,
      data: Intent?,
      context: Context?,
      onResult: (code: Int, file: File?, info: AppAttachModel?) -> Unit
    ) {
      onActivityResult(
        requestCode,
        resultCode,
        data,
        context,
        null,
        onResult
      )
    }

    @SuppressLint("NewApi")
    fun onActivityResult(
      requestCode: Int,
      resultCode: Int,
      data: Intent?,
      context: Context?,
      config: AppAttachmentDialogConfig? = null,
      onResult: (code: Int, file: File?, info: AppAttachModel?) -> Unit
    ) {
      val activityReqCodeMask = 0x0000ffff
      if (resultCode == AppCompatActivity.RESULT_OK) {
        //isProperlyPickImage: is hot handling/fix. as in andorid 33. requestCode of fragment is different and
        // 'activityReqCodeMask' mask not work for it.
        val isProperlyPickImage = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
          && data?.data.toString().contains("content://media/picker"))
        val isProperlyCameraImage = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
          && data?.data == null)
        when {
          requestCode == OPEN_CAMERA_REQUEST ||
              requestCode and activityReqCodeMask == OPEN_CAMERA_REQUEST
              || isProperlyCameraImage->
            onResult.invoke(requestCode, cameraPictureFile, null)
          requestCode == OPEN_GALLARY_REQUEST ||
              requestCode and activityReqCodeMask == OPEN_GALLARY_REQUEST
              || isProperlyPickImage-> {
            if (data == null) {
              onResult.invoke(requestCode, null, null)
              return
            }
            Thread {
              if (data.clipData != null) {
                val models = mutableListOf<AppAttachModel?>()
                for (i in 0 until data.clipData!!.itemCount) {
                  val model = getPath(context!!, data.clipData?.getItemAt(i)?.uri)
                  models.add(model)
                }
                val isMulti = models.size > 1
                val file = if(isMulti) null else runCatching { File(models.firstOrNull()?.path) }.getOrNull()
                val model = if(isMulti) AppAttachModel(null,null,null,null,null,isMulti,models.filterNotNull())
                else runCatching { models.firstOrNull() }.getOrNull()
                Handler(Looper.getMainLooper()).post {
                  onResult.invoke(requestCode, file, model)
                }
              } else {
                val model = getPath(context!!, data.data)
                val fileAvatar =
                  if (model?.path.isNullOrEmpty()) null else File(model?.path!!)
                Handler(Looper.getMainLooper()).post {
                  onResult.invoke(requestCode, fileAvatar, model)
                }
              }
            }.start()
          }
          requestCode == OPEN_OTHER_REQUEST ||
              requestCode and activityReqCodeMask == OPEN_OTHER_REQUEST -> {
            if (data == null) {
              onResult.invoke(requestCode, null, null)
              return
            }

            Thread {
              val model = getDrivePath(context!!, data.data, context!!.cacheDir)
              val fileAvatar =
                if (model?.path.isNullOrEmpty()) null else File(model?.path!!)
              Handler(Looper.getMainLooper()).post {
                onResult.invoke(requestCode, fileAvatar, model)
              }
            }.start()
          }
        }


      }


    }


    fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray,
      activity: Activity?,
      fragment: Fragment? = null
    ) {
      onRequestPermissionsResult(
        requestCode,
        permissions,
        grantResults,
        activity,
        null,
        fragment
      )
    }

    fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray,
      activity: Activity?,
      config: AppAttachmentDialogConfig? = null,
      fragment: Fragment? = null
    ) {
      //mask: 0xffff
      when {
        requestCode == CAMERA_PERMISSION_REQUEST_CODE ||
            requestCode and 0xffff == CAMERA_PERMISSION_REQUEST_CODE -> {
          //for camera we have two permission..
          if ((grantResults.isNotEmpty()
                && grantResults.first() == PackageManager.PERMISSION_GRANTED
                && grantResults.last() == PackageManager.PERMISSION_GRANTED)
          ) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            if (activity == null) return
            if (cameraPictureFile == null || cameraPictureFile?.exists() == true)
              AppAttachmentDialog.cameraPictureFile = File(
                activity?.cacheDir,
                "pic_${System.currentTimeMillis()}.jpg"
              )
            openCamera(
              activity,
              config?.requestCode ?: OPEN_CAMERA_REQUEST,
              authority,
              cameraPictureFile!!,
              fragment
            )
          } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
          }
          return
        }
        requestCode == GALLERY_PERMISSION_REQUEST_CODE ||
            requestCode and 0xffff == GALLERY_PERMISSION_REQUEST_CODE -> {
          if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            if (activity == null) return
            openGallery(
              activity,
              config?.requestCode ?: OPEN_GALLARY_REQUEST,
              fragment,
              config?.isMultiSelection ?: false
            )
          } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
          }
          return
        }
        requestCode == OTHER_PERMISSION_REQUEST_CODE ||
            requestCode and 0xffff == OTHER_PERMISSION_REQUEST_CODE -> {
          if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            if (activity == null) return
            openFileManager(
              activity,
              config?.requestCode ?: OPEN_OTHER_REQUEST,
              fragment
            )
          } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
          }
          return
        }
      }
    }

  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
//        setStyle(STYLE_NO_TITLE,R.style.Dialog)
    //call mCameraFile & authority to update companion object key values
    cameraPictureFile
    authority
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

  //reset current layoutRes& attachmentType in new bundle
  //skip update newBundle if args from system, and getLayoutFromBundle,getAttachmentsTypeFromBundle null
  override fun setArguments(args: Bundle?) {
    val newBundle = args ?: Bundle()
    if (getLayoutFromBundle() != -1)
      newBundle.putInt(LAYOUT_RES_BUNDLE_KEY, getLayoutFromBundle())
    if (getAppAttachmentTypesFromBundle() != null)
      newBundle.putIntArray(
        ATTACHMENT_TYPES_INT_ARRAY_BUNDLE_KEY,
        getAppAttachmentTypesFromBundle()?.map { it.value }?.toIntArray()
      )
    super.setArguments(newBundle)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val cameraBtn = view.findViewById<View?>(R.id.appAttachmentDialogCameraBtn)
    val galleryBtn = view.findViewById<View?>(R.id.appAttachmentDialogGalleryBtn)
    val otherBtn = view.findViewById<View?>(R.id.appAttachmentDialogOtherBtn)
    galleryBtn?.setOnClickListener {
      if (Build.VERSION.SDK_INT >= 29 && !config.requestStorageRunTimePermissionForGallery)
        openGallery(
          activity!!,
          config?.requestCode ?: OPEN_GALLARY_REQUEST,
          hostFragment ?: this,
          config.isMultiSelection
        )
      else
        checkPermission(
          arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
          GALLERY_PERMISSION_REQUEST_CODE
        ) {
          openGallery(
            activity!!,
            config?.requestCode ?: OPEN_GALLARY_REQUEST,
            hostFragment ?: this,
            config.isMultiSelection
          )
        }
      if (config.dismissAfterClick) dismiss()
    }

    cameraBtn?.setOnClickListener {
      val permissions = if (config.requestStorageRunTimePermission)
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
      else
        arrayOf(Manifest.permission.CAMERA)
      checkPermission(
        permissions,
        CAMERA_PERMISSION_REQUEST_CODE
      ) {
        AppAttachmentDialog.cameraPictureFile = config.cameraPictureFile ?: File(
          activity?.cacheDir,
          "pic_${System.currentTimeMillis()}.jpg"
        )
        openCamera(
          activity!!,
          config?.requestCode ?: OPEN_CAMERA_REQUEST,
          authority,
          cameraPictureFile!!,
          hostFragment ?: this
        )
      }
      if (config.dismissAfterClick) dismiss()
    }

    otherBtn?.setOnClickListener {
      checkPermission(
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        OTHER_PERMISSION_REQUEST_CODE
      ) {

        openFileManager(
          activity!!,
          config?.requestCode ?: OPEN_OTHER_REQUEST,
          hostFragment ?: this
        )
      }
      if (config.dismissAfterClick) dismiss()
    }
  }


  private fun checkPermission(
    permission: Array<String>,
    permissionCode: Int,
    callback: () -> Unit
  ) {
    val mContext = activity!!
    var deniedPermission: String = ""

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permission.size == 1 && permission.first() == Manifest.permission.READ_EXTERNAL_STORAGE){
      callback()
      return
    }
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
      if (shouldShowRequestPermissionRationale(deniedPermission)) {
        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

        explainRequired?.invoke(deniedPermission) {
          (hostFragment ?: this).requestPermissions(
            permission,
            permissionCode
          )
        } ?: (hostFragment ?: this).requestPermissions(
          permission,
          permissionCode
        )

      } else {
        // No explanation needed, we can request the permission.
        (hostFragment ?: this).requestPermissions(
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


  override fun onStart() {
    super.onStart()

    if (config.requestWithAtMost)
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
//        config.requestCode = requestCode
    super.show(manager, tag)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    Companion.onRequestPermissionsResult(
      requestCode,
      permissions,
      grantResults,
      activity,
      hostFragment ?: this
    )
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (activity?.isFinishing != false) return
    super.onActivityResult(requestCode, resultCode, data)
    onActivityResult(
      requestCode,
      resultCode,
      data,
      context,
      config,
      onResultCB ?: { code, file, model -> })


  }


  /**
   * getAppAttachmentTypesFromBundle or null
   * */
  private fun getAppAttachmentTypesFromBundle() =
    arguments?.getIntArray(ATTACHMENT_TYPES_INT_ARRAY_BUNDLE_KEY)
      ?.map { AppAttachmentType.getByValue(it) }?.toTypedArray()

  /**
   * getLayoutFromBundle or default value `-1`
   * */
  private fun getLayoutFromBundle() = arguments?.getInt(LAYOUT_RES_BUNDLE_KEY, -1) ?: -1

}

operator fun AppAttachmentDialogConfig.invoke(function: () -> Unit) {
}

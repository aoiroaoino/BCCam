package com.applicative.goldrush.bccam

import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer
import android.view.ViewGroup
import android.view.SurfaceHolder
import android.content.Context
import android.view.SurfaceView
import android.hardware.Camera
import android.util.Log
import android.graphics.PixelFormat
import android.view.MotionEvent
import java.io.FileOutputStream
import com.applicative.goldrush.bccam
import android.app.Activity
import java.io.FileInputStream
import java.io.ByteArrayInputStream
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Toast
import android.preference.Preference

class Preview(context: Context) extends SurfaceView(context) with SurfaceHolder.Callback {  
  private var mCamera: Camera = _
  private var previewSize: Camera#Size = _

  private val mHolder = getHolder
  mHolder.addCallback(this)
  mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
  
  private val mSutterListener = new Camera.ShutterCallback() {
	def onShutter(): Unit = {}    
  }
  
  private val mPictureListener = new Camera.PictureCallback() {
    def onPictureTaken(data: Array[Byte], camera: Camera): Unit = {
      val pref = PreferenceManager.getDefaultSharedPreferences(context)
      
      val picture = new ByteArrayInputStream(data)
      val url = pref.getString("destination", "https://dev.applicative.jp/api/import_photo")
      val userName = pref.getString("user_name", "John Doe")
      
      Log.i("User Name", userName)
      Log.i("URL", url)
      
      new PostPictureAsyncTask(context.asInstanceOf[Activity]).execute(new PostPictureParams(picture, url, userName));
      
      // カメラの再スタート
	  camera.startPreview()
	}    
  }
  
  // Take a picture
  override def onTouchEvent(event: MotionEvent): Boolean = {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (mCamera != null) {
        mCamera.takePicture(mSutterListener, null, mPictureListener)
      }
    }
    true
  }

  def surfaceCreated(holder: SurfaceHolder): Unit = {
    try {
      mCamera = Camera.open()
      mCamera.setPreviewDisplay(holder)
    } catch {
      case e: Exception => Log.e("error", e.getMessage)
    }

    val supportedSizes: Buffer[Camera#Size] = mCamera.getParameters.getSupportedPreviewSizes
    previewSize = supportedSizes.head
  }

  def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = {
    mCamera.stopPreview()
    val params = mCamera.getParameters
    // 横向き固定のため、params変更しない
    // params.setPreviewSize(previewSize.width, previewSize.height)
    // params.setPreviewFormat(PixelFormat.JPEG)
    try {
      mCamera.setParameters(params)
    } catch {
      case e: Exception => Log.e("NullPo", e.getMessage)
    }
    mCamera.startPreview()
  }

  def surfaceDestroyed(holder: SurfaceHolder): Unit = {
    mCamera.stopPreview()
    mCamera.release()
  }
  
}

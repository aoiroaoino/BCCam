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
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Surface
import android.content.res.Configuration
import android.graphics.Color

class Preview(context: Context) extends SurfaceView(context)
  with SurfaceHolder.Callback
{
  var mCamera: Camera = _
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
  
  private val autoFocusListener = new Camera.AutoFocusCallback() {
    def onAutoFocus(success: Boolean, camera: Camera): Unit = {
      if (success) mCamera.takePicture(mSutterListener, null, mPictureListener)
    }
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
    // 横向きを基準とした現在の角度
    val degree = getCameraDisplayOrientation(this.getContext.asInstanceOf[Activity])
    Log.e("degree", degree.toString)
    
    mCamera.stopPreview()
    
    // プレビュー表示の向きを決める
    mCamera.setDisplayOrientation(degree)

    // 撮影された画像の向きを決める
    val params = mCamera.getParameters
    params.setRotation(degree)
    Log.e("size", s"width: $width , height: $height")
    
    // 端末でサポートされている最大サイズを用いる
    val size = params.getSupportedPreviewSizes.head
    Log.e("set support size", s"width: ${size.width} , height: ${size.height}")
    params.setPreviewSize(size.width, size.height)
    
    // アスペクト比を保ちつつ、最大表示
    val layoutParams = this.getLayoutParams
    var layoutH: Double = 1.0
    var layoutW: Double = 1.0
    
    if (isPortrate) {
      layoutW = size.height
      layoutH = size.width
    } else {
      layoutW = size.width
      layoutH = size.height
    }
    
    val ratioW: Double = width / layoutW
    val ratioH: Double = height / layoutH
    val ratio = if (ratioW < ratioH) ratioW else ratioH
    Log.i("ratio", s"width: $ratioW , height: $ratioH")
    
    layoutParams.width = (layoutW * ratio).toInt
    layoutParams.height = (layoutH * ratio).toInt
    
    // 各パラメータをセット
    mCamera.setParameters(params)
    mCamera.startPreview()
  }

  def surfaceDestroyed(holder: SurfaceHolder): Unit = {
    mCamera.stopPreview()
    mCamera.release()
  }
  
  def takePicture(): Unit = {
    if (mCamera != null) {
      // オートフォーカスして撮影
      mCamera.autoFocus(autoFocusListener)
//      mCamera.takePicture(mSutterListener, null, mPictureListener)
    }
  }
  
  private def getCameraDisplayOrientation(activity: Activity): Int = {
    val degree = activity.getWindowManager.getDefaultDisplay.getRotation match {
      case Surface.ROTATION_0 => 0
      case Surface.ROTATION_90 => 90
      case Surface.ROTATION_180 => 180
      case Surface.ROTATION_270 => 270
    }
    (90 + 360 - degree) % 360
  }
  
  private def isPortrate(): Boolean = {
    this.getContext.asInstanceOf[Activity].getResources.getConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT
  }
}

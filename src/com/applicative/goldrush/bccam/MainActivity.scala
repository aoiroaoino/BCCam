package com.applicative.goldrush.bccam

import android.os.Bundle
import android.app.Activity
import android.view.Menu
import android.view.View
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.Toast
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.graphics.BitmapFactory
import android.view.Window
import android.view.WindowManager
import android.view.MenuItem
import android.util.Log
import android.content.SharedPreferences
import android.content.Context
import android.view.SurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener

class MainActivity extends Activity {
  
  private var gestureDetector: GestureDetector = _
  private var preview: Preview = _
  
  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    
    
    preview = new Preview(this)
    gestureDetector = new GestureDetector(this, gestureListener)
    
    setContentView(preview)
  }
  
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.main, menu)
    return true
  }
  
  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_settings => {
        startActivity(new Intent(this, classOf[SettingActivity]))
        true
      }
      case _ => {
        false
      }
    }
  }
  
  override def onTouchEvent(event: MotionEvent): Boolean = {
    gestureDetector.onTouchEvent(event)
    super.onTouchEvent(event)
  }
  
  private val gestureListener = new SimpleOnGestureListener() {
    override def onDoubleTap(event: MotionEvent): Boolean = {
      Log.e("event", "Double Touch")
      preview.takePicture
      true
    }
  }
}
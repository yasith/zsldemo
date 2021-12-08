/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hadrosaur.zsldemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.hadrosaur.zsldemo.CameraController.camera2OpenCamera
import com.hadrosaur.zsldemo.CameraController.closeCamera
import com.hadrosaur.zsldemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA_PERMISSION = 1
    private val REQUEST_FILE_WRITE_PERMISSION = 2

    lateinit var binding: ActivityMainBinding

    companion object {
        private val LOG_TAG = "ZSLDemo"
        private val ENABLE_LOG = true
        lateinit var camViewModel:CamViewModel

        fun Logd(message: String) {
            if (ENABLE_LOG)
                Log.d(LOG_TAG, message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        camViewModel = ViewModelProviders.of(this).get(CamViewModel::class.java)

        if (checkCameraPermissions()) {
            setupCameraParams(this, camViewModel.getCameraParams())

            binding.buttonCapture.setOnClickListener {
                camViewModel.getZSLCoordinator().capturePhoto(this, camViewModel.getCameraParams())
            }
        }
    }

    fun checkCameraPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
            return false

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_FILE_WRITE_PERMISSION)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //We now have permission, restart the app
                    val intent = getIntent()
                    finish()
                    startActivity(intent)
                } else {
                }
                return
            }
            REQUEST_FILE_WRITE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //We now have permission, restart the app
                    val intent = getIntent()
                    finish()
                    startActivity(intent)
                } else {
                }
                return
            }
        }
    }

    private fun startBackgroundThread(params: CameraParams) {
        if (params.backgroundThread == null) {
            params.backgroundThread = HandlerThread(LOG_TAG).apply {
                this.start()
                params.backgroundHandler = Handler(this.getLooper())
            }
        }
    }


    private fun stopBackgroundThread(params: CameraParams) {
        params.backgroundThread?.quitSafely()
        try {
            params.backgroundThread?.join()
            params.backgroundThread = null
        } catch (e: InterruptedException) {
            Logd( "Interrupted while shutting background thread down: " + e.message)
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread(camViewModel.getCameraParams())
        if (camViewModel.getCameraParams().previewTextureView?.isAvailable == true
            && !camViewModel.getCameraParams().isOpen) {
            camera2OpenCamera(this, camViewModel.getCameraParams())
        }
    }

    override fun onPause() {
        closeCamera(this, camViewModel.getCameraParams())
        stopBackgroundThread(camViewModel.getCameraParams())
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete_photos -> {
                deleteTestPhotos(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

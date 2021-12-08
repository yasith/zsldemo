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

package com.hadrosaur.zsldemo.CameraController

import android.hardware.camera2.CameraDevice
import androidx.annotation.NonNull
import com.hadrosaur.zsldemo.CameraParams
import com.hadrosaur.zsldemo.MainActivity

class CameraDeviceStateCallback(internal val activity: MainActivity, internal val params: CameraParams) : CameraDevice.StateCallback() {
    override fun onOpened(@NonNull cameraDevice: CameraDevice) {
        MainActivity.Logd("In CameraStateCallback onOpened: " + cameraDevice.id)
        params.device = cameraDevice
        createCameraCaptureSession(activity, cameraDevice, params)
    }

    override fun onClosed(camera: CameraDevice) {
        MainActivity.Logd("In CameraStateCallback onClosed. Camera is closed.")
        super.onClosed(camera)
    }

    override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
        MainActivity.Logd("In CameraStateCallback onDisconnected")
    }

    override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
        MainActivity.Logd("In CameraStateCallback onError: " + cameraDevice.id + " and error: " + error)

        if (CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE == error) {
            //Let's try to close an open camera and re-open this one
            MainActivity.Logd("In CameraStateCallback too many cameras open, closing one...")
        } else if (CameraDevice.StateCallback.ERROR_CAMERA_DEVICE == error) {
            MainActivity.Logd("Fatal camera error, close device.")
        } else if (CameraDevice.StateCallback.ERROR_CAMERA_IN_USE == error) {
            MainActivity.Logd("This camera is already open... doing nothing")
        } else if (CameraDevice.StateCallback.ERROR_CAMERA_DISABLED == error) {
            MainActivity.Logd("Camera " + params.id + " is disabled due to a device policy")
        }
    }
}

package com.veys.takingimagescamera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.veys.takingimagescamera.databinding.ActivityMainBinding
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val permission_code = 1000
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    var vFilename: String =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

    }

    fun captureImage(view:View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                openCameraForPhoto()
            }else{
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA),permission_code)
            }
        }else{
            Toast.makeText(this@MainActivity, "Sorry, your version of Android is not supported.", Toast.LENGTH_SHORT).show()
        }
    }

    fun recordVideo(view:View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                openCameraForVideo()
            }else{
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA),permission_code)
            }
        }else{
            Toast.makeText(this@MainActivity, "Sorry, your version of Android is not supported.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun openCameraForPhoto(){
        //ContentValues sınıfı kullanarak yeni bir medya dosyası için bilgiler oluşturur
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the Camera")

        val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        //her dosya için belirsiz bir ad oluşturması sağlanır
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        vFilename = "PHOTO_$timeStamp.jpg"

        //fotoğrafın nereye kaydedileceği belirlenir ve güvenli erişim sağlanır
        val filedir = getExternalFilesDir(null)
        val file = File(filedir,vFilename)

        //dosya oluşma durumu kontrol edilir
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        val image_uri = FileProvider.getUriForFile(this@MainActivity,"${applicationContext.packageName}.provider", file)

        intentToCamera.putExtra(MediaStore.EXTRA_OUTPUT,image_uri)//çekilen fotoğrafın belirlenen dosya yoluna kaydedilmesi sağlanır
        activityResultLauncher.launch(intentToCamera)

    }
    fun openCameraForVideo(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"New Video")
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the Camera")

        val intentToCamera = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        vFilename = "VIDEO_$timeStamp.jpg"

        val filedir = getExternalFilesDir(null)
        val file = File(filedir,vFilename)

        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        val video_uri = FileProvider.getUriForFile(this@MainActivity,"${applicationContext.packageName}.provider", file)

        intentToCamera.putExtra(MediaStore.EXTRA_OUTPUT,video_uri)
        intentToCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)

        activityResultLauncher.launch(intentToCamera)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permission_code ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCameraForPhoto()
                }else{
                    Toast.makeText(this@MainActivity, "permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val file = File(getExternalFilesDir(null), vFilename)
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${applicationContext.packageName}.provider",
                        file
                    )
                    if (vFilename.contains("PHOTO")) {
                        binding.imageview.setImageURI(uri)
                    } else if (vFilename.contains("VIDEO")) {
                        binding.videoView.setVideoURI(uri)
                        binding.videoView.start()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "failed to capture image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

    }

}
package com.harunkor.mediaedittransformer

import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.ScaleToFitTransformation
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.github.file_picker.FileType
import com.github.file_picker.ListDirection
import com.github.file_picker.showFilePicker
import com.harunkor.mediaedittransformer.databinding.ActivityMainBinding
import com.wada811.databindingktx.dataBinding
import java.io.File
import java.lang.Float.min

@UnstableApi class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var transformer: Transformer

    private val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path+ File.separator + "MYVIDEO.mp4"

    private val binding: ActivityMainBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initListener()

    }

    private fun init() {

        val grayScaleEffect: Effect = RgbFilter.createGrayscaleFilter()

        val rotateEffect: Effect = ScaleToFitTransformation.Builder()
            .setRotationDegrees(30f)
            .build()

        val zoomOutEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val scale = 2 - min(1f, presentationTimeUs / 1_000_000f) // Video will zoom from 2x to 1x in the first second
            transformationMatrix.postScale(/* sx= */ scale, /* sy= */ scale)
            transformationMatrix // The calculated transformations will be applied each frame in turn
        }


        // Create a TransformationRequest and set the output format to H.265
        val transformationRequest = TransformationRequest.Builder()
            .setVideoMimeType(MimeTypes.VIDEO_H265)
            .build()


        // Create a Transformer
        transformer = Transformer.Builder(this)
            .setTransformationRequest(transformationRequest) // Pass in TransformationRequest
            .setVideoEffects(listOf(grayScaleEffect,rotateEffect,zoomOutEffect))
            .setRemoveAudio(true) // Remove audio track
            .addListener(transformerListener) // transformerListener is an implementation of Transformer.Listener
            .build()

    }

    private fun initListener() = with(binding) {
        btFilePicker.setOnClickListener {
            showFilePicker(
                gridSpanCount = 3,
                limitItemSelection = 1,
                listDirection = ListDirection.RTL,
                fileType =  FileType.VIDEO
            ) {
               startTransformation(it[0].file.path)
            }
        }
    }

    private fun startTransformation(path: String) {
        // Start the transformation
        binding.progressBar.visibility = View.VISIBLE
        val inputMediaItem = MediaItem.fromUri(path)
        transformer.startTransformation(inputMediaItem,outPath)

    }


    private var transformerListener: Transformer.Listener = object : Transformer.Listener {
        override fun onTransformationCompleted(inputMediaItem: MediaItem) {
            binding.progressBar.visibility = View.GONE
            binding.vvTransform.apply {
               setVideoPath(outPath)
               setOnPreparedListener { mp ->
                    binding.vvTransform.start()
                    mp.isLooping = true
                }
            }
            binding.vvOrginal.apply {
                setVideoPath(inputMediaItem.localConfiguration?.uri?.path)
                setOnPreparedListener { mp ->
                    binding.vvOrginal.start()
                    mp.isLooping = true
                }
            }
        }

        override fun onTransformationError(inputMediaItem: MediaItem, e: Exception) {
            Toast.makeText(this@MainActivity, "ERROR: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


}
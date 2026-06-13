package com.example.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.Toast
import com.example.MicTabApplication
import com.example.R
import com.example.data.models.AiModelConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MicTabAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var currentFocusedNode: AccessibilityNodeInfo? = null
    
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient()

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingWidget()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (floatingView == null) {
            createFloatingWidget() // Try creating it again if it failed previously
        }
        
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED || event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val node = event.source
            if (node != null && node.isEditable) {
                currentFocusedNode = node
            } else {
                currentFocusedNode = null
            }
        }
        
        checkStateAndToggleWidget()
    }

    private fun checkStateAndToggleWidget() {
        if (currentFocusedNode != null && isKeyboardShowing()) {
            showFloatingWidget()
        } else {
            hideFloatingWidget()
        }
    }

    private fun isKeyboardShowing(): Boolean {
        var keyboardIsVisible = false
        val interactiveWindows = windows
        if (interactiveWindows != null) {
            for (window in interactiveWindows) {
                if (window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD) {
                    keyboardIsVisible = true
                    break
                }
            }
        }
        return keyboardIsVisible
    }

    override fun onInterrupt() {
        hideFloatingWidget()
        stopRecordingSafely()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun createFloatingWidget() {
        if (!android.provider.Settings.canDrawOverlays(this)) {
            return
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_mic, null)
        
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        val micIcon = floatingView?.findViewById<ImageView>(R.id.mic_icon)
        
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var lastClickTime = 0L

        micIcon?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val Xdiff = Math.abs((event.rawX - initialTouchX).toInt())
                    val Ydiff = Math.abs((event.rawY - initialTouchY).toInt())

                    if (Xdiff < 10 && Ydiff < 10) {
                        val clickTime = System.currentTimeMillis()
                        if (clickTime - lastClickTime < 300) {
                            // Double tap or Triple tap logic could go here
                            performAction()
                        } else {
                            toggleRecording()
                        }
                        lastClickTime = clickTime
                    }
                    view.performClick()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun showFloatingWidget() {
        if (floatingView != null && floatingView?.parent == null && android.provider.Settings.canDrawOverlays(this)) {
            val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }
            windowManager?.addView(floatingView, params)
        }
    }

    private fun hideFloatingWidget() {
        if (floatingView != null && floatingView?.parent != null) {
            windowManager?.removeView(floatingView)
        }
    }

    private fun toggleRecording() {
        val icon = floatingView?.findViewById<ImageView>(R.id.mic_icon)
        
        if (!isRecording) {
            startRecording()
            if (isRecording) {
                icon?.setImageResource(R.drawable.ic_mic_active)
                Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
            }
        } else {
            stopRecordingSafely()
            icon?.setImageResource(R.drawable.ic_mic_idle)
            Toast.makeText(this, "Processing audio...", Toast.LENGTH_SHORT).show()
            processAudio()
        }
    }
    
    private fun startRecording() {
        try {
            audioFile = File(cacheDir, "dictation_audio.m4a")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
            isRecording = false
        }
    }
    
    private fun stopRecordingSafely() {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
            }
        }
    }

    private fun processAudio() {
        val fileToUpload = audioFile ?: return
        if (!fileToUpload.exists() || fileToUpload.length() == 0L) {
            Toast.makeText(this, "Audio file is empty", Toast.LENGTH_SHORT).show()
            return
        }

        serviceScope.launch {
            val repository = (application as MicTabApplication).repository
            val activeConfig = repository.getEnabledModelsByTypeSync("STT").firstOrNull()
            
            if (activeConfig == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MicTabAccessibilityService, "No active STT provider found. Check settings.", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileToUpload.name, fileToUpload.asRequestBody("audio/m4a".toMediaType()))
                    .addFormDataPart("model", activeConfig.modelName)
                    .build()

                val request = Request.Builder()
                    .url(activeConfig.baseUrl.trimEnd('/') + "/audio/transcriptions")
                    .addHeader("Authorization", "Bearer ${activeConfig.apiKey}")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val text = json.optString("text", "")
                    
                    withContext(Dispatchers.Main) {
                        if (text.isNotBlank()) {
                            pasteTextIntoFocusedNode(" " + text.trim())
                        } else {
                            Toast.makeText(this@MicTabAccessibilityService, "Could not understand audio", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MicTabAccessibilityService, "API Error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MicTabAccessibilityService, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performAction() {
        if (currentFocusedNode == null || currentFocusedNode?.text.isNullOrBlank()) {
            Toast.makeText(this, "No text to polish.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentText = currentFocusedNode?.text.toString()
        Toast.makeText(this, "Polishing text...", Toast.LENGTH_SHORT).show()

        serviceScope.launch {
            val repository = (application as MicTabApplication).repository
            val activeConfig = repository.getEnabledModelsByTypeSync("LLM").firstOrNull()
            
            if (activeConfig == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MicTabAccessibilityService, "No active LLM provider found.", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            try {
                val jsonBody = JSONObject().apply {
                    put("model", activeConfig.modelName)
                    val messages = org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are an assistant that polishes text for clarity, grammar, and tone. Output ONLY the polished text without any conversational filler or quotes.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Polish this text:\n\n$currentText")
                        })
                    }
                    put("messages", messages)
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url(activeConfig.baseUrl.trimEnd('/') + "/chat/completions")
                    .addHeader("Authorization", "Bearer ${activeConfig.apiKey}")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val choices = json.optJSONArray("choices")
                    val polishedText = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                    
                    withContext(Dispatchers.Main) {
                        if (!polishedText.isNullOrBlank()) {
                            replaceTextInFocusedNode(polishedText)
                        } else {
                            Toast.makeText(this@MicTabAccessibilityService, "Could not polish text", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MicTabAccessibilityService, "LLM API Error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MicTabAccessibilityService, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun replaceTextInFocusedNode(text: String) {
        if (currentFocusedNode != null) {
            val arguments = android.os.Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            currentFocusedNode?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }

    private fun pasteTextIntoFocusedNode(text: String) {
        if (currentFocusedNode != null) {
            val arguments = android.os.Bundle()
            val currentText = currentFocusedNode?.text?.toString() ?: ""
            val newText = if (currentText.isEmpty()) text.trimStart() else currentText + text
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
            currentFocusedNode?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecordingSafely()
        hideFloatingWidget()
        serviceScope.cancel()
    }
}

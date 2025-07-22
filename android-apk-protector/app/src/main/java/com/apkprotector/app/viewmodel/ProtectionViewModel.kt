package com.apkprotector.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apkprotector.app.model.ProtectionConfig
import com.apkprotector.app.protector.ApkProtector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProtectionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _selectedApk = MutableLiveData<Uri?>()
    val selectedApk: LiveData<Uri?> = _selectedApk
    
    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress
    
    private val _status = MutableLiveData("Ready")
    val status: LiveData<String> = _status
    
    private val _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean> = _isProcessing
    
    private val protector = ApkProtector(application)
    
    private var trialDays = 14
    private var isOwner = false
    
    fun selectApk(uri: Uri) {
        _selectedApk.value = uri
        _status.value = "APK selected: ${uri.lastPathSegment}"
    }
    
    fun setProtectionConfig(trialDays: Int, isOwner: Boolean) {
        this.trialDays = trialDays
        this.isOwner = isOwner
    }
    
    fun protectApk() {
        val uri = _selectedApk.value ?: return
        
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _status.value = "Starting protection..."
                _progress.value = 0
                
                val apkPath = getPathFromUri(uri)
                val config = ProtectionConfig(
                    apkPath = apkPath,
                    trialDays = trialDays,
                    isOwner = isOwner
                )
                
                val protectedApk = withContext(Dispatchers.IO) {
                    protector.protectApk(config) { status, progress ->
                        _status.postValue(status)
                        _progress.postValue(progress)
                    }
                }
                
                _status.value = "Protection complete!\nSaved to: ${protectedApk.absolutePath}"
                
            } catch (e: Exception) {
                _status.value = "Error: ${e.message}"
                _progress.value = 0
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    private fun getPathFromUri(uri: Uri): String {
        // In a real implementation, this would properly handle content URIs
        // and copy the file to a temporary location if needed
        return uri.path ?: throw IllegalArgumentException("Invalid APK path")
    }
}

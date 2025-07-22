package com.apkprotector.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.apkprotector.app.databinding.ActivityMainBinding
import com.apkprotector.app.viewmodel.ProtectionViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ProtectionViewModel
    
    private val selectApkLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectApk(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[ProtectionViewModel::class.java]
        
        setupUI()
        observeViewModel()
        checkPermissions()
    }
    
    private fun setupUI() {
        binding.selectApkButton.setOnClickListener {
            selectApkLauncher.launch("application/vnd.android.package-archive")
        }
        
        binding.protectButton.setOnClickListener {
            val trialDays = binding.trialDaysInput.text.toString().toIntOrNull() ?: 14
            val isOwner = binding.ownerCheckbox.isChecked
            viewModel.setProtectionConfig(trialDays, isOwner)
            viewModel.protectApk()
        }
        
        binding.ownerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.trialDaysInput.isEnabled = !isChecked
            if (isChecked) {
                binding.trialDaysInput.setText("0")
            } else {
                binding.trialDaysInput.setText("14")
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.selectedApk.observe(this) { uri ->
            binding.selectedApkText.text = uri?.toString() ?: "No APK selected"
            binding.protectButton.isEnabled = uri != null
        }
        
        viewModel.progress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.progressText.text = "Progress: $progress%"
        }
        
        viewModel.status.observe(this) { status ->
            binding.statusText.text = status
        }
        
        viewModel.isProcessing.observe(this) { isProcessing ->
            binding.selectApkButton.isEnabled = !isProcessing
            binding.protectButton.isEnabled = !isProcessing && viewModel.selectedApk.value != null
            binding.ownerCheckbox.isEnabled = !isProcessing
            binding.trialDaysInput.isEnabled = !isProcessing && !binding.ownerCheckbox.isChecked
        }
    }
    
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            
            if (permissions.any { 
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED 
            }) {
                ActivityCompat.requestPermissions(this, permissions, 1001)
            }
        }
    }
}

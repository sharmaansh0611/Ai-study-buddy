package com.sharmadipanshu.aistudybuddy

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AIStudyBuddyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        initializeFirebaseIfConfigured()
    }

    private fun initializeFirebaseIfConfigured() {
        if (FirebaseApp.getApps(this).isNotEmpty()) return

        val options = FirebaseOptions.Builder()
            .setApplicationId(getString(R.string.firebase_application_id))
            .setApiKey(getString(R.string.firebase_api_key))
            .setProjectId(getString(R.string.firebase_project_id))
            .setGcmSenderId(getString(R.string.firebase_sender_id))
            .setStorageBucket(getString(R.string.firebase_storage_bucket))
            .build()

        FirebaseApp.initializeApp(this, options)
    }
}

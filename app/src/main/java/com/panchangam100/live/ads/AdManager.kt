package com.panchangam100.live.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.panchangam100.live.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // AdMob Test IDs (replace with real IDs before release)
        const val BANNER_AD_UNIT = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT = "ca-app-pub-3940256099942544/1033173712"
        const val APP_OPEN_AD_UNIT = "ca-app-pub-3940256099942544/9257395921"

        const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun initialize() {
        MobileAds.initialize(context) {}
        loadInterstitial()
    }

    fun loadInterstitial() {
        if (BuildConfig.DEBUG) return
        if (isLoading || interstitialAd != null) return
        isLoading = true

        val req = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isLoading = false
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                isLoading = false
            }
        })
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit = {}) {
        if (BuildConfig.DEBUG) { onDismiss(); return }
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial()
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(e: AdError) {
                    interstitialAd = null
                    loadInterstitial()
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            onDismiss()
            loadInterstitial()
        }
    }
}

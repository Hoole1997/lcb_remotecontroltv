package com.example.lcb.app

import com.blankj.utilcode.util.LogUtils
import com.example.lcb.app.ad.LcbAdInitializer
import net.corekit.metrics.adjust.AdjustTracker

class LcbApp : com.universal.remote.tool.Dekvlpe0kc68o16eofq() {

    companion object {

        var lcbApp: LcbApp? = null

        fun backLaunchActivity() {
            lcbApp?.autoprotectnet()
        }
    }

    override fun onCreate() {
        super.onCreate()
        lcbApp = this
        LcbAdInitializer.initialize(this)
        this.routeweather {isOrganic, network, campaign, adgroup, creative, jsonResponse ->
            AdjustTracker.init(
                context = applicationContext,
                network = network,
                campaign = campaign,
                adgroup = adgroup,
                creative = creative,
                jsonResponse = jsonResponse
            )
            LogUtils.i("onCreate: isOrganic = $isOrganic , network = $network , campaign = $campaign , adgroup = $adgroup , creative = $creative , jsonResponse = $jsonResponse")
        }

    }

    override fun maxquicksmartcache(): Class<in Any>? {
        return MainActivity::class.java as Class<in Any>?
    }

    override fun litesmarttoolpanel(): List<Class<in Any>?>? {
        return listOf(
            MainActivity::class.java,
            RemoteControlActivity::class.java,
            ModelListActivity::class.java,
            BrandListActivity::class.java
        ) as List<Class<in Any>?>?
    }

}

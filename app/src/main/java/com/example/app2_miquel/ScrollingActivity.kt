package com.example.app2_miquel
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.app2_miquel.databinding.ActivityScrollingBinding
import com.example.app2_miquel.databinding.CardLayoutBinding
import org.json.JSONArray
import java.io.Serializable

class ScrollingActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 100
    }

    private lateinit var ActivityScrollbinding: ActivityScrollingBinding
    private var mediaPlayer: MediaPlayer? = null
    private var playPosition: Int = 0
    private var ccMap = mutableMapOf<String, CommercialCenter>()
    private var notLoadedJson = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityScrollbinding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(ActivityScrollbinding.root)

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.fortunate_son)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
        if (savedInstanceState != null) {
            ccMap = savedInstanceState.getSerializable("ccMap") as MutableMap<String, CommercialCenter>
            playPosition = savedInstanceState.getInt("position", 0)
            mediaPlayer?.seekTo(playPosition)
        }
        if (notLoadedJson) {
            ccMap = readCommercialCentersFromFile(this, "cc_data.json")
            notLoadedJson = false
        }

        for (cc in ccMap.values) {
            val cardBinding = CardLayoutBinding.inflate(layoutInflater)
            cardBinding.myCard.picture.setImageResource(cc.img)
            cardBinding.myCard.nameCC.text = cc.nameCC
            cardBinding.myCard.address.text = cc.address
            cardBinding.myCard.qtyShops.text = getString(R.string.shop_units_text, cc.qtyShops)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams.setMargins(40, 0, 40, 80)
            cardBinding.root.layoutParams = layoutParams

            cardBinding.root.setOnClickListener {
                val intent = Intent(this, CentreActivity::class.java)
                intent.putExtra("cc", ccMap[cc.nameCC])
                startActivityForResult(intent,REQUEST_CODE)
            }
            ActivityScrollbinding.centersList.addView(cardBinding.root)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==REQUEST_CODE && resultCode== Activity.RESULT_OK){
            val cc=data?.getSerializableExtra("cc") as CommercialCenter
            ccMap[cc.nameCC]=cc
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mediaPlayer != null) {
            playPosition = mediaPlayer!!.currentPosition
            outState.putInt("position", playPosition)
        } else {
            playPosition = 0
            outState.putInt("position", playPosition)
        }
        outState.putSerializable("ccMap", ccMap as Serializable)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        playPosition = mediaPlayer!!.currentPosition
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onRestoreInstanceState(savedState: Bundle) {
        playPosition =savedState.getInt("position")
        super.onRestoreInstanceState(savedState)
        ccMap = savedState.getSerializable("ccMap") as MutableMap<String, CommercialCenter>
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.seekTo(playPosition)
        mediaPlayer?.start()
    }

    fun readCommercialCentersFromFile(context: Context, filePath: String): MutableMap<String,CommercialCenter> {
        val assets = context.assets
        val jsonString= assets.open(filePath).bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val commercialCenters = mutableMapOf<String, CommercialCenter>()

        for (i in 0 until jsonArray.length()) {
            val centerObject = jsonArray.getJSONObject(i)

            val shopArray = centerObject.getJSONArray("shopList")
            val shops = mutableListOf<Shop>()
            val selectedCategories = mutableSetOf<String>()
            for (j in 0 until shopArray.length()) {
                val shopObject = shopArray.getJSONObject(j)
                val shop = Shop(
                    name = shopObject.getString("name"),
                    description = shopObject.getString("description"),
                    category = shopObject.getString("category")
                )
                shops.add(shop)
                selectedCategories.add(shop.category)
            }
            shops.sortBy { it.category }
            val imgName = centerObject.getString("img")
            val trackName = centerObject.getString("track")
            val imgId = context.resources.getIdentifier(imgName, "drawable", context.packageName)
            val trackId = context.resources.getIdentifier(trackName, "raw", context.packageName)
            val name = centerObject.getString("nameCC")

            val commercialCenter = CommercialCenter(
                nameCC = name,
                address = centerObject.getString("address"),
                qtyShops = shops.size,
                img = imgId,
                shopList = shops,
                track = trackId,
                playPosition = 0,
                selectedCategories = selectedCategories
            )

            commercialCenters.put(name, commercialCenter)
        }
        return commercialCenters
    }
}
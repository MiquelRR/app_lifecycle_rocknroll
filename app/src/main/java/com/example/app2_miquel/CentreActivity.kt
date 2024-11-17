package com.example.app2_miquel

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app2_miquel.databinding.ActivityCentreBinding
import com.example.app2_miquel.databinding.FilterChipBinding
import com.example.app2_miquel.databinding.ShopItemBinding
import java.io.Serializable

class CentreActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var cc: CommercialCenter
    private lateinit var binding: ActivityCentreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //cc = intent.getSerializableExtra("cc", CommercialCenter::class.java) as CommercialCenter
        cc = intent.getSerializableExtra("cc") as CommercialCenter
        val categories: Set<String> = cc.shopList.map { it.category }.toSet()

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, cc.track)
            mediaPlayer?.seekTo(cc.playPosition)
            mediaPlayer?.isLooping = true
        } else {
            mediaPlayer?.seekTo(cc.playPosition)
        }
        if (savedInstanceState != null) {
            cc = savedInstanceState.getSerializable("cc") as CommercialCenter
            mediaPlayer?.seekTo(cc.playPosition)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityCentreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.myCard.nameCC.text = cc.nameCC
        binding.myCard.address.text = cc.address
        binding.myCard.qtyShops.text = getString(R.string.shop_units_text, cc.qtyShops)
        binding.myCard.picture.setImageResource(cc.img)

        if (categories.size > 1) {
            categories.forEach { category ->
                val categoryItemBinding = FilterChipBinding.inflate(layoutInflater)
                categoryItemBinding.root.text = category
                categoryItemBinding.root.isChecked = category in cc.selectedCategories
                binding.filtersList.addView(categoryItemBinding.root)
                categoryItemBinding.root.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        cc.selectedCategories.add(category)
                    } else {
                        cc.selectedCategories.remove(category)
                    }
                    refreshShopList(cc)
                }
            }
        }
        refreshShopList(cc)
        binding.fab.setOnClickListener { view ->
            finish()
        }

    }
    private fun refreshShopList(cc: CommercialCenter) {
        binding.shList.removeAllViews() // Clear existing views
        for (shop in cc.shopList) {
            if (cc.selectedCategories.contains(shop.category)) {
                val shopItemBinding = ShopItemBinding.inflate(layoutInflater)
                shopItemBinding.shopName.text = shop.name
                shopItemBinding.shopDescription.text = shop.description
                shopItemBinding.category.text = shop.category
                binding.shList.addView(shopItemBinding.root)
            }
        }
    }
    override fun finish() {
        cc.playPosition = mediaPlayer?.currentPosition ?: 0
        val intent = Intent()
        intent.putExtra("cc", cc)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        cc.playPosition = mediaPlayer?.currentPosition ?: 0
        outState.putSerializable("cc", cc )
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        cc.playPosition = mediaPlayer?.currentPosition ?: 0
        mediaPlayer?.pause()
        super.onPause()
    }

    override fun onStop() {
        cc.playPosition = mediaPlayer?.currentPosition ?: 0
        super.onStop()
    }

    override fun onRestoreInstanceState(savedState: Bundle) {
        super.onRestoreInstanceState(savedState)
        //cc = savedState.getSerializable("cc", CommercialCenter::class.java) as CommercialCenter
        cc = savedState.getSerializable("cc") as CommercialCenter
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.seekTo(cc.playPosition)
        mediaPlayer?.start()
    }


}
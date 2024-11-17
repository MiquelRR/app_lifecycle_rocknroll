package com.example.app2_miquel

import java.io.Serializable

data class CommercialCenter (
    var nameCC: String,
    var address: String,
    var qtyShops: Int,
    var img: Int,
    var shopList: List<Shop>,
    var track: Int,
    var playPosition: Int,
    var selectedCategories: MutableSet<String>
) : Serializable
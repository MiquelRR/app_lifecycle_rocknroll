package com.example.app2_miquel

import java.io.Serializable

data class Shop (
    val name: String,
    val description: String,
    val category: String
) : Serializable
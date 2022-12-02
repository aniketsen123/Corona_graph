package com.example.api

import com.google.gson.annotations.SerializedName
import java.util.*

data class CovidData (
    val dateChecked:Date,
    val positiveIncrease:Int=0,
    val negativeIncrease:Int=0,
    val deathIncrease:Int=0,
  val state:String?
        )

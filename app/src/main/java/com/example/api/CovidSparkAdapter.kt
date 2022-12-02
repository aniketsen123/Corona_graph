package com.example.api

import android.graphics.RectF
import com.robinhood.spark.SparkAdapter

class CovidSparkAdapter(private val dailyData: List<CovidData>): SparkAdapter() {

    var metric=Metric.POSITIVE
    var daysAgo=Timescale.MAX
    override fun getCount(): Int {
       return dailyData.size
    }

    override fun getItem(index: Int): Any {
       return dailyData[index]
    }

    override fun getY(index: Int): Float {
        val choseDayData = dailyData[index]
        return when (metric){
            Metric.POSITIVE-> choseDayData.positiveIncrease.toFloat()
            Metric.NEGATIVE->choseDayData.negativeIncrease.toFloat()
            Metric.DEATH->choseDayData.deathIncrease.toFloat()
        }
    }

    override fun getDataBounds(): RectF {
        val bounds=super.getDataBounds()
        if(daysAgo!=Timescale.MAX)
        bounds.left=count - daysAgo.numday.toFloat()
        return bounds
    }
}

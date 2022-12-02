package com.example.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val ALL_STATES="All(Nationalist)"
    private lateinit var currentlyShownData: List<CovidData>
    private lateinit var adapter: CovidSparkAdapter
    private lateinit var perStateDailyData: Map<String?, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>
    private val base_url="https://api.covidtracking.com/v1/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gson=GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit=Retrofit.Builder().baseUrl(base_url).addConverterFactory(GsonConverterFactory.create(gson)).build()
       val service= retrofit.create(CovidService::class.java)
        service.getNationalData().enqueue(object :Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                val nationalData=response.body()
                if(nationalData==null)
                {
                    return
                }
                setupEventListners()
                nationalDailyData=nationalData.reversed()
                updateDisplay(nationalDailyData)

            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {

            }

        })
        service.getStatesData().enqueue(object :Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                val stateData=response.body()
                if(stateData==null)
                {
                    return
                }
                perStateDailyData=stateData.reversed().groupBy { it.state }
                updateSpinnerWithStateData(perStateDailyData.keys)

            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {

            }

        })

    }

    private fun updateSpinnerWithStateData(keys: Set<String?>) {
       val stateList=keys.toTypedArray()
        stateList.sort()
        val spinner=findViewById<Spinner>(R.id.spinnerSelect)
        val colors= arrayListOf("fdfddsf","dfsdfdsd","dfddsdfsd","fdfsdf")
        val adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,stateList)
        spinner.adapter=adapter
        spinner.onItemSelectedListener= object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selective= parent!!.getItemAtPosition(position)
                val selectedData=perStateDailyData[selective]?:nationalDailyData
                updateDisplay(selectedData)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun setupEventListners() {
        val sparkView=findViewById<com.robinhood.spark.SparkView>(R.id.sparkView)
        sparkView.isScrubEnabled=true
        sparkView.setScrubListener {
            if(it is CovidData)
            {
                updateInfoDate(it)
            }
        }
        val radiogroupTimeselection=findViewById<RadioGroup>(R.id.radioGroupTimeSelection)
        radiogroupTimeselection.setOnCheckedChangeListener{_,checkid->
            adapter.daysAgo=when(checkid){
                R.id.radioButtonWeek->Timescale.WEEK
                R.id.radioButtonMonth->Timescale.MONTH
                else ->Timescale.MAX

            }
adapter.notifyDataSetChanged()
        }
        val radiometricgroup=findViewById<RadioGroup>(R.id.radioGroupMetricSelection)
        radiometricgroup.setOnCheckedChangeListener{_,checkid->
            when(checkid){
                R.id.radioButtonPositive->updateDisplayMetric(Metric.POSITIVE)
                R.id.radioButtonNegative->updateDisplayMetric(Metric.NEGATIVE)
                R.id.radioButtonDeath->updateDisplayMetric(Metric.DEATH)
            }
        }
    }

    private fun updateDisplayMetric(positive: Metric) {
        val color=when(positive)
        {
            Metric.NEGATIVE->R.color.colorNegative
            Metric.POSITIVE->R.color.colorPositive
            Metric.DEATH->R.color.colorDeath
        }
        val sparkView=findViewById<com.robinhood.spark.SparkView>(R.id.sparkView)
        val colorint=ContextCompat.getColor(this,color)
        sparkView.lineColor=colorint
        val tvMetriclabel=findViewById<TextView>(R.id.tickerView)
        tvMetriclabel.setTextColor(colorint)
        sparkView.baseLineColor=colorint

adapter.metric=positive
        adapter.notifyDataSetChanged()
    updateInfoDate(currentlyShownData.last())
    }

    private fun updateDisplay(dailyData: List<CovidData>) {
        currentlyShownData=dailyData
         adapter=CovidSparkAdapter(dailyData)
        val sparkView=findViewById<com.robinhood.spark.SparkView>(R.id.sparkView)
        sparkView.adapter=adapter
        val radioButtonpositive=findViewById<RadioButton>(R.id.radioButtonPositive)
        radioButtonpositive.isChecked=true
        val radioButtonMax=findViewById<RadioButton>(R.id.radioButtonMax)
        radioButtonMax.isChecked=true
     updateInfoDate(dailyData.last())
    }

    private fun updateInfoDate(covidata: CovidData) {
        val numCAses=when(adapter.metric){
            Metric.NEGATIVE->covidata.negativeIncrease
            Metric.POSITIVE->covidata.positiveIncrease
            Metric.DEATH->covidata.deathIncrease
        }
    val tvMetriclabel=findViewById<TextView>(R.id.tickerView)
        tvMetriclabel.text=NumberFormat.getInstance().format( numCAses)
        val outputFormat=SimpleDateFormat("MMM dd ,yyyy", Locale.US)
        val tvDateLabel=findViewById<TextView>(R.id.tvDateLabel)
        tvDateLabel.text=outputFormat.format(covidata.dateChecked)
    }

}
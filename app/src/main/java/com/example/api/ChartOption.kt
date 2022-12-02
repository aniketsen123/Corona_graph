package com.example.api

enum class Metric{
    NEGATIVE,POSITIVE,DEATH
}
enum class Timescale(val numday:Int)
{
    WEEK(7),
    MONTH(12),
    MAX(-1)
}
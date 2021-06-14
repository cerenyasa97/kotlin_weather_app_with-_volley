package com.cerenyasa.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.zip.Inflater

class WeatherRecyclerAdapter(var dayList: Array<String>, var tempList: Array<Array<Double>>) : RecyclerView.Adapter<WeatherRecyclerAdapter.WeatherViewHolder>() {
    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val day = itemView.findViewById<TextView>(R.id.name)
        val temp = itemView.findViewById<TextView>(R.id.temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val oneRow = inflater.inflate(R.layout.one_row, parent, false)
        return WeatherViewHolder(oneRow)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.day.text = dayList[position]
        holder.temp.text = "${Math.ceil(tempList[position][0]).toInt()}/${Math.ceil(tempList[position][1]).toInt()} °C"
    }

    override fun getItemCount(): Int {
        return dayList.size
    }
}
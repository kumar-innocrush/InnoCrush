package com.innocrush.laser.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innocrush.laser.R
import com.innocrush.laser.databinding.RowItemBinding
import com.innocrush.laser.datamodel.LaserData
import kotlinx.android.synthetic.main.row_item.view.*

class HistoryAdapter(var laserData: List<LaserData>): RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: RowItemBinding): RecyclerView.ViewHolder(itemView.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){
            with(laserData[position]){
                if(position==0){
                    holder.itemView.setBackgroundColor(Color.parseColor("#000000"))
                holder.itemView.txtvw_datetime.text = "Date Time"
                holder.itemView.txtvw_param1.text = "Measured Volume"
                holder.itemView.txtvw_param2.text = "No of time steps"
                holder.itemView.txtvw_param3.text = "Avg height"
                holder.itemView.txtvw_param4.text = "Mean height"
                holder.itemView.txtvw_param5.text = "No of time slots"
                holder.itemView.txtvw_param6.text = "Belt pulse"
                    textColor(holder.itemView.txtvw_datetime)
                    textColor(holder.itemView.txtvw_param1)
                    textColor(holder.itemView.txtvw_param2)
                    textColor(holder.itemView.txtvw_param3)
                    textColor(holder.itemView.txtvw_param4)
                    textColor(holder.itemView.txtvw_param5)
                    textColor(holder.itemView.txtvw_param6)
            }else{
                    itemView.txtvw_datetime.text = this.datetime
                    itemView.txtvw_param1.text = this.p1.toString()
                    itemView.txtvw_param2.text = this.p2.toString()
                    itemView.txtvw_param3.text = this.p3.toString()
                    itemView.txtvw_param4.text = this.p4.toString()
                    itemView.txtvw_param5.text = this.p5.toString()
                    itemView.txtvw_param6.text = this.p6.toString()
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return laserData.size
    }

    fun textColor(txtvw: TextView){
        txtvw.setTextColor(Color.parseColor("#ffffff"))
    }

}

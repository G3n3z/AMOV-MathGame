package pt.isec.a2020116565_2020116988.mathgame.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.fragments.SinglePlayerLeaderboard

class SpRVAdapter(val info : ArrayList<SinglePlayerLeaderboard.Info>) : RecyclerView.Adapter<SpRVAdapter.SpViewHolder>() {
    class SpViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
        var tv1 : TextView = view.findViewById(R.id.tv_pos_sp_lb)
        var tv2 : TextView = view.findViewById(R.id.tv_name_sp_lb)
        var tv3 : TextView = view.findViewById(R.id.tv_time_sp_lb)
        var tv4 : TextView = view.findViewById(R.id.tv_points_sp_lb)

        @SuppressLint("SetTextI18n")
        fun update(info : SinglePlayerLeaderboard.Info) {
            tv1.text = "#" + info.str1
            tv2.text = info.str2
            tv3.text = info.str3
            tv4.text = info.str4
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_leaderboard_item,parent,false)
        return SpViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.update(info[position])
    }

    override fun getItemCount(): Int = info.size

}

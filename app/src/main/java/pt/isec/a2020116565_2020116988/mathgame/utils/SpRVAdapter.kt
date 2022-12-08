package pt.isec.a2020116565_2020116988.mathgame.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.LBPlayer

class SpRVAdapter(var context: Context?) : RecyclerView.Adapter<SpRVAdapter.SpViewHolder>() {

    private var playersInfo: MutableList<LBPlayer> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_leaderboard_item,parent,false)
        return SpViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.update(playersInfo[position], position, context)
    }

    override fun getItemCount(): Int = playersInfo.size


    @SuppressLint("NotifyDataSetChanged")
    fun addPlayers(players: List<LBPlayer>) {
        this.playersInfo.apply{
            clear()
            addAll(players)
        }
        notifyDataSetChanged()
    }

    /**
     * Mapeamento dos campos da vista aos atributos dos players
     */
    class SpViewHolder(val view : View, var context: Context?) : RecyclerView.ViewHolder(view) {
        var tvpos : TextView = view.findViewById(R.id.tv_pos_sp_lb)
        var tvname : TextView = view.findViewById(R.id.tv_name_sp_lb)
        var tvtime : TextView = view.findViewById(R.id.tv_time_sp_lb)
        var tvpoints : TextView = view.findViewById(R.id.tv_points_sp_lb)


        @SuppressLint("SetTextI18n")
        fun update(playersInfo: LBPlayer, position: Int ,context: Context?) {
            tvpos.text = "#" + (position+1).toString()
            tvname.text = playersInfo.username
            tvtime.text = "${context?.getString(R.string.time)} : ${playersInfo.totalTime}"
            tvpoints.text = "${context?.getString(R.string.points)} : ${playersInfo.points}"
        }
    }


}

package pt.isec.a2020116565_2020116988.mathgame.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.LBMultiPlayer

class MpRVAdapter(var context: Context?) : RecyclerView.Adapter<MpRVAdapter.MpViewHolder>() {

    private lateinit var posListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(pos: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
        posListener = listener
    }

    private var gamesInfo: MutableList<LBMultiPlayer> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mp_leaderboard_item,parent,false)
        return MpViewHolder(view, posListener, context)
    }

    override fun onBindViewHolder(holder: MpViewHolder, position: Int) {
        holder.update(gamesInfo[position], position, context)
    }

    override fun getItemCount(): Int = gamesInfo.size


    @SuppressLint("NotifyDataSetChanged")
    fun addGames(games: List<LBMultiPlayer>) {
        this.gamesInfo.apply{
            clear()
            addAll(games)
        }
        notifyDataSetChanged()
    }

    class MpViewHolder(val view : View, listener: onItemClickListener, var context: Context?) : RecyclerView.ViewHolder(view) {
        var tvpos : TextView = view.findViewById(R.id.tv_pos_mp_item)
        var tvtime : TextView = view.findViewById(R.id.tv_time_mp_item)
        var tvpoints : TextView = view.findViewById(R.id.tv_points_mp_item)

        init {
            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        fun update(gamesInfo: LBMultiPlayer, position: Int, context: Context?) {
            tvpos.text = "#" + (position+1).toString()
            tvtime.text = "${context?.getString(R.string.time)}: ${gamesInfo.totalTime}"
            tvpoints.text = "${context?.getString(R.string.points)}: ${gamesInfo.points}"
        }
    }


}

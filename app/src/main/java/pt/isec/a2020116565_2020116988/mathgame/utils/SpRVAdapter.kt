package pt.isec.a2020116565_2020116988.mathgame.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        var tvphoto: ImageView = view.findViewById(R.id.iv_photo_sp_lb)
        var tvpos : TextView = view.findViewById(R.id.tv_pos_sp_lb)
        var tvname : TextView = view.findViewById(R.id.tv_name_sp_lb)
        var tvlevel : TextView = view.findViewById(R.id.tv_lvl_sp_lb)
        var tvboards: TextView = view.findViewById(R.id.tv_boards_sp_lb)
        var tvpoints : TextView = view.findViewById(R.id.tv_points_sp_lb)
        var tvtime : TextView = view.findViewById(R.id.tv_time_sp_lb)


        @SuppressLint("SetTextI18n")
        fun update(playersInfo: LBPlayer, position: Int ,context: Context?) {
            val imageByteArray: ByteArray = Base64.decode( playersInfo.photo!!, Base64.DEFAULT)
            Glide.with(view.context)
                .load(imageByteArray)
                .circleCrop()
                .into(tvphoto)
            tvpos.text = "#" + (position+1).toString()
            tvname.text = playersInfo.username
            tvlevel.text = "${context?.getString(R.string.level)}: ${playersInfo.level}"
            tvboards.text = "${context?.getString(R.string.boards)}: ${playersInfo.totalTables}"
            tvtime.text = "${context?.getString(R.string.time)}: ${playersInfo.totalTime}"
            tvpoints.text = "${context?.getString(R.string.points)}: ${playersInfo.points}"
        }
    }


}

package pt.isec.a2020116565_2020116988.mathgame.views


import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.User


class ScoresRecycleViewAdapter(var users : MutableList<User> ) : RecyclerView.Adapter<ScoresRecycleViewAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scores_lv_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.update(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitNewData(list : MutableList<User>){
        list.sortByDescending { it.points }
        users = list;
        notifyDataSetChanged()
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvName : TextView = view.findViewById(R.id.item_tv_name)
        var tvPont : TextView = view.findViewById(R.id.item_tv_points)
//        var tvtime : TextView = view.findViewById(R.id.tvtime)

        var img : ImageView = view.findViewById(R.id.itemPhoto)

        var tvBoard : TextView = view.findViewById(R.id.item_tv_boards)

        var textUsername: String = view.context.getString(R.string.user_name)
        var textPont: String = view.context.getString(R.string.points)
        var textBoards: String = view.context.getString(R.string.boards)

        @SuppressLint("SetTextI18n")
        fun update(data : User) {
            tvName.text =  data.userName
            tvPont.text =  "$textPont: ${data.points}"
            tvBoard.text = String.format("%s: %d", textBoards, data.nTables)
            if (data.state == State.OnGameOver){
                tvName.setTextColor(Color.RED)
            }else{
                tvName.setTextColor(Color.WHITE)
            }

            val imageByteArray: ByteArray = Base64.decode( data.photo!!, Base64.DEFAULT)
            Glide.with(view.context)
                .load(imageByteArray)
                .circleCrop()
                .into(img);

        }

    }

}
package pt.isec.a2020116565_2020116988.mathgame.views


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.User


class ScoresRecycleViewAdapter(val users : MutableList<User> ) : RecyclerView.Adapter<ScoresRecycleViewAdapter.ViewHolder>() {



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



    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvName : TextView = view.findViewById(R.id.item_tv_name)
        var tvPont : TextView = view.findViewById(R.id.item_tv_points)
//        var tvtime : TextView = view.findViewById(R.id.tvtime)
        var textUsername: String = view.context.getString(R.string.user_name)
        var textPont: String = view.context.getString(R.string.points)
        fun update(data : User) {
            tvName.text =  textUsername + ": "+ data.userName
            tvPont.text = textPont + ": "+ data.points.toString()
        }

    }

}
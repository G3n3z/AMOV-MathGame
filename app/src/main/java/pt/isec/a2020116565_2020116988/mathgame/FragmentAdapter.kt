package pt.isec.a2020116565_2020116988.mathgame

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMainBinding
import pt.isec.a2020116565_2020116988.mathgame.fragments.GameFragment
import pt.isec.a2020116565_2020116988.mathgame.fragments.Home
import pt.isec.a2020116565_2020116988.mathgame.fragments.UserProfile

class FragmentAdapter(fa: FragmentActivity, binding: ActivityMainBinding):FragmentStateAdapter(fa) {



    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun createFragment(position: Int): Fragment {

        return when(position){
            1 -> Home()
            2 -> GameFragment()
            3 -> UserProfile()
           else -> Fragment()
        }
    }
}
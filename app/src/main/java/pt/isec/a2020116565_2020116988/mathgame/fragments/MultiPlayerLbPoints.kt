package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiPlayerLbPointsBinding

class MultiPlayerLbPoints : Fragment() {

    private lateinit var binding: FragmentMultiPlayerLbPointsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMultiPlayerLbPointsBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {

    }
}
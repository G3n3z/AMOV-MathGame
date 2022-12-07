package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiPlayerLbPointsBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiPlayerLbTimeBinding

class MultiPlayerLbTime : Fragment() {

    private lateinit var binding: FragmentMultiPlayerLbTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMultiPlayerLbTimeBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {

    }
}
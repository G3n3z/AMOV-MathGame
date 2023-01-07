package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.ativities.MultiplayerActivity
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMutiPlayerBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentPanelBinding
import pt.isec.a2020116565_2020116988.mathgame.views.GamePanelView
import pt.isec.a2020116565_2020116988.mathgame.views.ScoresRecycleViewAdapter


class PanelFragment : Fragment() {
    // TODO: Rename and change types of parameters

    lateinit var binding : FragmentPanelBinding;
    private val viewModel : MultiplayerModelView by activityViewModels()
    private lateinit var multiActivity: MultiplayerActivity
    lateinit var app: Application;
    private var adapter : ScoresRecycleViewAdapter? = null;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPanelBinding.inflate(inflater, container, false)
        app = (requireActivity() as MultiplayerActivity).app
        multiActivity = requireActivity() as MultiplayerActivity

        binding.flScoresFragment.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        adapter = ScoresRecycleViewAdapter(viewModel.users.value!!)
        binding.flScoresFragment.adapter = adapter;
        registerHandlers();
        return binding.root;
    }

    private fun registerHandlers() {
        viewModel.users.observe(viewLifecycleOwner){
            Log.i("registerCall", "reciclerView update")
            adapter?.submitNewData(it)
        }
        viewModel.state.observe(viewLifecycleOwner){
            when(it){
                State.OnGameOver -> { binding.tvPanelFragment.text = getString(R.string.game_over) }
                State.WINNER -> { binding.tvPanelFragment.text = getString(R.string.winner) }
                State.OnDialogResume -> { binding.tvPanelFragment.text = getString(R.string.next_multi_level) }
                State.OnDialogPause -> { binding.tvPanelFragment.text = getString(R.string.next_multi_level) }
                State.OnGame -> {
                    findNavController().navigate(R.id.fragment_multiplayer)
                }
                State.OnDialogBack ->{multiActivity.dialogQuit()}
                else ->{}
            }
        }
    }


}
package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.ativities.MultiplayerActivity
import pt.isec.a2020116565_2020116988.mathgame.ativities.SinglePlayerActivity
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentHomeBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMutiPlayerBinding
import pt.isec.a2020116565_2020116988.mathgame.dialog.DialogLevelMultiplayer
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.enum.MoveResult
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.utils.vibratePhone
import pt.isec.a2020116565_2020116988.mathgame.views.*


class MutiPlayerFragment : Fragment(), GameActivityInterface {



    lateinit var binding : FragmentMutiPlayerBinding;
    private val viewModel : MultiplayerModelView by activityViewModels()
    private lateinit var gamePanelView : GamePanelView;
    private lateinit var multiActivity: MultiplayerActivity
    lateinit var app: Application;
    private var jobResult : Job? = null;
    private var dialog : DialogLevelMultiplayer? = null
    private var adapter : ScoresRecycleViewAdapter? = null;
    private var points : Int = 0
        set(value) {
            field = value
            binding.gamePontMultiplayer.text = String.format("%s: %d",getString(R.string.points), value);
        }

    var level: Int = 0
        set(value) {
            field = value
            binding.gameLevel.text = String.format("%s: %d",getString(R.string.level), value);
        }
    var time: Int = 0
        set (value) {
            field = value
            binding.gameTimeMultiplayer.text = String.format("%s: %d",getString(R.string.time), value);
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMutiPlayerBinding.inflate(inflater, container, false)
        app = (requireActivity() as MultiplayerActivity).app
        multiActivity = requireActivity() as MultiplayerActivity
        gamePanelView = GamePanelView(requireContext(),null,0,0, app.data.operations, this);
        binding.gameTableMultiplayer.addView(gamePanelView)
        return binding.root
    }



    private fun connectionStateHandlers(it: ConnectionState, mode : GameMode) {
        if (it == ConnectionState.WAITING_OTHERS && multiActivity.clientInitDialog == null){
            if(mode == GameMode.CLIENT_MODE) {
                multiActivity.clientInitDialog = ClientWaitingDialog(cancelWait)
                multiActivity.clientInitDialog?.show(multiActivity.supportFragmentManager, "waitingFrag")
            }
        }else if(it == ConnectionState.CONNECTION_ESTABLISHED){
            if(mode == GameMode.CLIENT_MODE){
                multiActivity.clientInitDialog?.dismiss()
                multiActivity.clientInitDialog = null
            }
            binding.flScoresFragment.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter = ScoresRecycleViewAdapter(viewModel.users.value!!)
            binding.flScoresFragment.adapter = adapter;
        }
        else if(it == ConnectionState.CONNECTION_LOST && viewModel._state.value!! == State.OnGame){
            viewModel.closeSockets()
            dialog?.cancel()
            val intent = SinglePlayerActivity.getIntentFromMultiplayer(
                requireContext(),
                viewModel.state.value?.ordinal!!
            )
            app.data.generateMaxOperations();
            multiActivity.finish()
            startActivity(intent);
        }else if (it == ConnectionState.EXIT){
            viewModel.closeSockets()
            multiActivity.finish()
        }else if (it == ConnectionState.FAIL_CONNECT){
            viewModel.closeSockets()
            var snack = Snackbar.make(binding.root, getString(R.string.connection_failed), Snackbar.LENGTH_SHORT)
            snack.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    multiActivity.finish()
                }
            })
            snack.show()
        }
    }

    private var cancelWait = fun(){
        viewModel.stopGame()
        multiActivity.finish()
    }


    private fun registerCallbacksOnLabels() {
        viewModel.time.observe(viewLifecycleOwner){
            time = it
        }
        viewModel.level.observe(viewLifecycleOwner){
            level = it;
        }
        viewModel.points.observe(viewLifecycleOwner){
            points = it;
        }
        viewModel.moveResult.observe(viewLifecycleOwner){
            jobResult?.cancel()
            when(it) {
                MoveResult.NOTHING -> {binding.moveResponse.text = ""}
                MoveResult.WRONG_OPERATION -> {
                    binding.moveResponse.text = getString(R.string.wrong_response)
                    binding.moveResponse.setTextColor(Color.RED)
                    vibratePhone(multiActivity)
                }
                MoveResult.MAX_OPERATION -> {
                    binding.moveResponse.text = getString(R.string.right_answers)
                    binding.moveResponse.setTextColor(Color.GREEN)
                }
                MoveResult.SECOND_OPERATION ->{
                    binding.moveResponse.text = getString(R.string.second_answers)
                    binding.moveResponse.setTextColor(Color.BLUE)
                }
            }
            if(it != MoveResult.NOTHING){
                jobResult = CoroutineScope(Dispatchers.IO).launch{ clean() }
            }

        }
        viewModel.operation.observe(viewLifecycleOwner){
            gamePanelView.operations = it
            gamePanelView.mount()
        }
        viewModel.users.observe(viewLifecycleOwner){
            Log.i("registerCall", "reciclerView update")
            adapter?.submitNewData(it)
        }
    }

    private suspend fun clean(){
        delay(1000)
        binding.moveResponse.post{binding.moveResponse.text = ""}
    }


    override fun swipe(index: Int) {
        viewModel.swipe(index)
    }

    private fun registerCallbacksOnState() {
        viewModel.state.observe(this){
            onStateChange(it);
        }
    }

    private fun onStateChange(state : State) {
        when(state){
            State.OnGame -> {
                dialog?.dismiss()
                dialog = null
            }
            State.OnDialogBack -> {
                multiActivity.dialogQuit()
            }
            State.OnDialogResume -> {
                findNavController().navigate(R.id.fragment_panel)
                Log.i("onStateChange", "OnDialogResume");

            }
            State.OnDialogPause -> {
                findNavController().navigate(R.id.fragment_panel)
                Log.i("onStateChange", "OnDialogPause");

            }
            State.OnGameOver, State.WINNER ->{
                findNavController().navigate(R.id.fragment_panel)

            }
        }
    }


    override fun onStart() {
        super.onStart()
        Log.i("CREATE", "START")
        gamePanelView = GamePanelView(requireContext(),null,0,0, app.data.operations, this);
        binding.gameTableMultiplayer.addView(gamePanelView)
        val mode :GameMode =  GameMode.gameModeByInteger(multiActivity.intent.getIntExtra(MultiplayerActivity.MODE, -1))
        viewModel.connectionState.observe(this) { connectionStateHandlers(it, mode) }
        registerCallbacksOnState();
        registerCallbacksOnLabels();
        Log.i("OnStart", viewModel.state.value.toString())
        viewModel.refreshState()
        binding.gamePontMultiplayer.text = "${getString(R.string.points)}: $points";
        binding.gameLevel.text = "${getString(R.string.level)}: $level";

    }


}
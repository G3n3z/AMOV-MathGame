package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentGameBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentUserProfileBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.createFileFromUri
import pt.isec.a2020116565_2020116988.mathgame.utils.setPic

/**
 * A simple [Fragment] subclass.
 * Use the [UserProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfile : Fragment() {


    lateinit var binding: FragmentUserProfileBinding;
    var username : String = ""
        set(value) {
            field = value;
        }
    private var imagePath:String? = null
        set(value){
            field = value;
        }
    val app: Application by lazy { activity?.application as Application }

    private val requestPermissionLauncher = registerForActivityResult( ActivityResultContracts.RequestPermission())
    { isGranted ->
        run {
            Log.i("PErmissions", permissionGranted.toString())
            permissionGranted = isGranted
        }
    }

    private var startActivityForGalleryResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->

            if (result.resultCode == Activity.RESULT_OK) {
                var resultData = result.data;
                resultData?.data?.let { uri ->
                    imagePath = createFileFromUri(requireContext(), uri)
                    app.data.currentUser?.photo = imagePath.toString()
                    updateView()
                }
            }

    }
    private var  permissionGranted:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserProfileBinding.inflate(layoutInflater, container,false)
        verifyPermissions();
        username = app.data.currentUser?.userName ?: "";
        imagePath = app.data.currentUser?.photo;
        binding.editUserEditText.setText(username)
        Log.i("ONCREATE", username?:" ")

        binding.profileUploadImage.setOnClickListener {
            if (permissionGranted){
                Log.i("PERMISSIONS", "Tem")
                chooseImage();
            }else{
                Log.i("PERMISSIONS", "Não tem")
            }
        }
        binding.userProfileBtnSave.setOnClickListener{
            saveProfile();
        }

        return binding.root;
    }

    override fun onStart() {
        super.onStart()
        updateView();
    }
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForGalleryResult.launch(intent)
    }

    private fun verifyPermissions() {
        val permission =
            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_EXTERNAL_STORAGE
            else
                Manifest.permission.READ_MEDIA_IMAGES

        requestPermissionLauncher.launch(permission)
    }
    private fun updateView(){
        if(imagePath == null){
//            binding.photo.background = ResourcesCompat.getDrawable(resources
//                ,Android., null)
        }else{
            setPic(binding.photoIn, imagePath!!)
        }
    }

    private fun saveProfile()
    {
        if(binding.editUserEditText.text.trim().isEmpty())
        {
            Snackbar.make(
                binding.editUserEditText,
                "You must pick a username",
                Snackbar.LENGTH_LONG
            ).show()
            binding.editUserEditText.requestFocus()
            return
        }

        username = binding.editUserEditText.text.trim().toString()
        app.data.currentUser = User(username, imagePath)
        findNavController().navigate(R.id.fragment_home)
        //imagePath.toString()
        Log.i("SAVE", username ?: "")
        Log.i("SAVE", imagePath.toString())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserProfile().apply {

            }
    }
}
package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentUserProfileBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.encodeTobase64
import pt.isec.a2020116565_2020116988.mathgame.utils.updatePic

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
                    imagePath = encodeTobase64(requireContext(), uri)
                    updateView()
                }
            }

    }
    private var  permissionGranted:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = app.data.currentUser?.userName.orEmpty()
        imagePath = app.data.currentUser?.photo
        Log.i("CREATE", username)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserProfileBinding.inflate(layoutInflater, container,false)
        verifyPermissions();

        binding.editUserEditText.setText(username)
        Log.i("ONCREATE", username)

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
        binding.photoIn.post { updateView() }
        return binding.root;
    }

    override fun onStart() {
        super.onStart()
        Log.i("ONSTART", username)
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

        }else{
            val imageByteArray: ByteArray = Base64.decode( imagePath, Base64.DEFAULT)
            Glide.with(requireContext())
                .load(imageByteArray)
                .circleCrop()
                .into(binding.photoIn);
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

        val sharedPrefs = context?.getSharedPreferences(SHAREDPREFS, Context.MODE_PRIVATE)
        with(sharedPrefs!!.edit()) {
            putString(getString(R.string.profileUsername), username)
            putString(getString(R.string.profilePhoto), imagePath)
            apply()
        }

        app.data.currentUser = User(username, imagePath)

        findNavController().navigate(R.id.fragment_home)

    }

    fun loadUser(){
        val sharedPrefs = context?.getSharedPreferences(SHAREDPREFS, Context.MODE_PRIVATE)
        username = sharedPrefs?.getString(getString(R.string.profileUsername),"")!!
        imagePath = sharedPrefs.getString(getString(R.string.profilePhoto),"")!!
    }

    override fun onDestroy() {
        super.onDestroy()
        app.externalCacheDir?.deleteRecursively() //TODO verificar se está a correr bem e se é necessária
    }

    companion object {
        const val SHAREDPREFS = "ProfileSharedPrefs"

    }
}
package my.edu.tarc.contact

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import my.edu.tarc.contact.databinding.FragmentProfileBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream


class ProfileFragment : Fragment(), MenuProvider {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val getPhoto = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        if(uri != null){
            binding.imageViewPicture.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        //Initialise Shared Preference
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        with(sharedPreferences){
            binding.editTextProfileName.setText(getString(getString(R.string.name), ""))
            binding.editTextProfilePhone.setText(getString(getString(R.string.phone), ""))
        }

        //Let ProfileFragment to manage the Menu
        val menuHost: MenuHost = this.requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner,
            Lifecycle.State.RESUMED)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val image = readProfilePicture()
        if(image != null){
            binding.imageViewPicture.setImageBitmap(image)
        }else{
            binding.imageViewPicture.setImageResource(R.drawable.default_pic)
        }

        binding.imageViewPicture.setOnClickListener {
            getPhoto.launch("image/*")
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.second_menu, menu)

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if(menuItem.itemId == R.id.action_save){
            val name = binding.editTextProfileName.text.toString()
            val phone = binding.editTextProfilePhone.text.toString()
            with(sharedPreferences.edit()){
                putString(getString(R.string.name), name)
                putString(getString(R.string.phone), phone)
                apply()
            }

            //Save profile Pic to local storage
            saveProfilePicture(binding.imageViewPicture)
            //Save profile picture to cloud storage
            uploadProfilePicture()

            Toast.makeText(context, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
        }else if(menuItem.itemId == android.R.id.home){
            findNavController().navigateUp()
        }
        return true
    }

    private fun saveProfilePicture(view: View) {
        val filename = "profile.png"
        val file = File(this.context?.filesDir, filename)
        val image = view as ImageView

        val bd = image.drawable as BitmapDrawable
        val bitmap = bd.bitmap
        val outputStream: OutputStream

        try{
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }
    }

    private fun readProfilePicture(): Bitmap? {
        val filename = "profile.png"
        val file = File(this.context?.filesDir, filename)

        if(file.isFile){
            try{
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                return bitmap
            }catch (e: FileNotFoundException){
                e.printStackTrace()
            }
        }
        return null
    }

    private fun uploadProfilePicture(){
        val filename = "profile.png"
        val file = Uri.fromFile(File(this.context?.filesDir, filename))

        try {
            val storageRef = Firebase.storage("gs://contact-32bad.appspot.com").reference
            val userRef = sharedPreferences.getString(getString(R.string.phone),"")

            if (userRef.isNullOrEmpty()){
                //Alert user
                Toast.makeText(context, getString(R.string.profile_error), Toast.LENGTH_SHORT).show()
            }else{
                //Upload profile picture to Cloud Storage
                storageRef.child("profile_pic").child(userRef).putFile(file)
            }
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }
    }
}


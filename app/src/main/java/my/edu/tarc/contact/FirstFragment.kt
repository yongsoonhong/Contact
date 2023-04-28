package my.edu.tarc.contact

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import my.edu.tarc.contact.databinding.FragmentFirstBinding
import my.tarc.mycontact.ContactAdapter
import my.tarc.mycontact.ContactViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), MenuProvider {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //Refer to the ViewModel created by the Main Activity
    private val myContactViewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        //Let FirstFragment to manage the Menu
        val menuHost: MenuHost = this.requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner,
            Lifecycle.State.RESUMED)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactAdapter()

        //Add an observer
        myContactViewModel.contactList.observe(
            viewLifecycleOwner,
            Observer {
                if(it.isEmpty()){
                    binding.textViewCount.text =
                        getString(R.string.no_record)
                }else{
                    binding.textViewCount.isVisible = false
                    adapter.setContact(it)
                }
            }
        )
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        //Do Nothing Here
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_upload){
            //TODO - Upload records to the Cloud Database
            val sharedPreferences: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val id  = sharedPreferences.getString(getString(R.string.phone),"")
            if (id.isNullOrEmpty()){
                Toast.makeText(context,getString(R.string.profile_error),Toast.LENGTH_SHORT).show()
            }else{
                myContactViewModel.uploadContact(id)
                Toast.makeText(context,getString(R.string.contact_uploaded),Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}
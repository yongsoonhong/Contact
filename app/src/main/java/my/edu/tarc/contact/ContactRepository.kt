package my.tarc.mycontact

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ContactRepository(private val contactDao: ContactDao){
    //Room execute all queries on a separate thread
    val allContacts: LiveData<List<Contact>> = contactDao.getAllContact()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun add(contact: Contact){
        contactDao.insert(contact)
    }

    @WorkerThread
    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }

    @WorkerThread
    suspend fun update(contact: Contact){
        contactDao.update(contact)
    }

    fun uploadContact(id: String){
        if (allContacts.isInitialized){
            if (!allContacts.value.isNullOrEmpty()){
                val database = Firebase.database("https://contact-32bad-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
                allContacts.value!!.forEach{
                    database.child("user").child(id).child(it.phone).setValue(it)
                }
            }
        }
    }
}
package com.azrosk.tiersapp.ui.shared.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.azrosk.tiersapp.helper.Constants
import com.azrosk.tiersapp.databinding.ActivityLoginBinding
import com.azrosk.tiersapp.model.Admin
import com.azrosk.tiersapp.model.Client
import com.azrosk.tiersapp.ui.shared.login.admin_viewmodel.AdminViewModel
import com.azrosk.tiersapp.ui.shared.login.clients_viewmodel.ClientsViewModel
import com.azrosk.tiersapp.sharedpref.MySharedPreferences
import com.azrosk.tiersapp.ui.admins_ui.activities.MainActivity
import com.azrosk.tiersapp.ui.clients_ui.actitvity.ClientsActivity
import com.azrosk.tiersapp.ui.shared.signup.SignupActivity
import java.util.*
import kotlin.collections.ArrayList

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel : AdminViewModel
    private lateinit var clientViewModel : ClientsViewModel
    private var _binding : ActivityLoginBinding?=null
    private val binding get() = _binding!!
    private val spinnerOptions = arrayOf("Admin", "User")
    private val list = ArrayList<Admin>()
    private val admin = Admin(email = "admin@admin.com", password = "admin")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sp = MySharedPreferences(this)
        val lang = sp.getLanguage()
        val config = resources.configuration
        val locale = Locale(lang!!)
        Locale.setDefault(locale)
        config.setLocale(locale)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        clientViewModel = ViewModelProvider(this)[ClientsViewModel::class.java]

        val myAdapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, spinnerOptions)
        binding.spinnerLoginAs.adapter = myAdapter

        //if default admin account doesn't exist create new one
        addDefaultAdmin()

        binding.btnLogin.setOnClickListener {
           login()
        }

        binding.tvReg.setOnClickListener{
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun addDefaultAdmin() {
        val sp = MySharedPreferences(this)
        val adminExists = sp.adminExists()

        if (adminExists == 0){
            viewModel.addAdmin(admin)
            sp.saveAdmin(1)
        }

    }


    private fun login() {

        when{
            TextUtils.isEmpty(binding.etLoginEmail.text.toString().trim{it <= ' '}) -> {
                binding.etLoginEmail.error = "Enter your email"
                //                Toast.makeText(this, "Please enter email.", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.etLoginPassword.text.toString().trim{it <= ' '}) -> {
                binding.etLoginPassword.error = "Enter your password"
                Toast.makeText(this, "Please enter password.", Toast.LENGTH_LONG).show()
            }

            else -> {
                val email = binding.etLoginEmail.text.toString()
                val password = binding.etLoginPassword.text.toString()
                val adminLogIn = Admin(email = email, password = password)
                if (binding.spinnerLoginAs.selectedItem.toString() == "Admin"){
                    viewModel.readAllAdmins.observe(this){
                        loginAsAdmin(it, email, password)
                    }

                } else if (binding.spinnerLoginAs.selectedItem.toString() == "User"){
                    clientViewModel.readAllClients.observe(this) {
                        loginAsClient(it, email, password)
                    }
                } else{
                    Toast.makeText(this, "something went wrong! ", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun loginAsAdmin(i: Admin, email: String, password: String) {
        val sp = MySharedPreferences(this)
        if (i.email == email && i.password == password){
            sp.saveEmail(email)
            sp.saveUserType(Constants.ADMIN)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            if (sp.getEmail() == "empty"){
                Toast.makeText(this, "admin doesn't exist or wrong password!!", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun loginAsClient(list: List<Client>, email: String, password: String) {
        for (i in list) {
            if (i.email == email && i.password == password) {
                val sp = MySharedPreferences(this)
                sp.saveEmail(email)
                sp.saveUserType(Constants.CLIENT)
                val intent = Intent(this, ClientsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        Toast.makeText(this, "user doesn't exist or wrong password!!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
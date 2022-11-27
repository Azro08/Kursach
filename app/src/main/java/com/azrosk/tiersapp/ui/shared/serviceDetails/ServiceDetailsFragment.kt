package com.azrosk.tiersapp.ui.shared.serviceDetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.azrosk.tiersapp.R
import com.azrosk.tiersapp.helper.Constants
import com.azrosk.tiersapp.databinding.FragmentServiceDetailsBinding
import com.azrosk.tiersapp.model.Cart
import com.azrosk.tiersapp.model.Product
import com.azrosk.tiersapp.ui.clients_ui.fragments.myOrders.viewmodel.CartViewModel
import com.azrosk.tiersapp.ui.admins_ui.fragments.home.viewmodel.ServiceViewModel
import com.azrosk.tiersapp.sharedpref.MySharedPreferences
import com.azrosk.tiersapp.ui.admins_ui.activities.MainActivity
import com.azrosk.tiersapp.ui.clients_ui.actitvity.ClientsActivity
import com.azrosk.tiersapp.ui.shared.serviceDetails.viewmodel.ServiceDetailsCartViewModel
import com.azrosk.tiersapp.ui.shared.serviceDetails.viewmodel.ServiceDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class ServiceDetailsFragment : Fragment() {
    private var _binding : FragmentServiceDetailsBinding?=null
    private val binding get() = _binding!!
    lateinit var serVm : ServiceDetailsViewModel
    lateinit var cartVm : ServiceDetailsCartViewModel
    private var cartList = mutableListOf<Cart>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        _binding = FragmentServiceDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        serVm = ViewModelProvider(requireActivity())[ServiceDetailsViewModel::class.java]
        cartVm = ViewModelProvider(requireActivity())[ServiceDetailsCartViewModel::class.java]
        val sp = MySharedPreferences(requireContext())
        val user_type = sp.getLoggedInBy()
        val curUser =  sp.getEmail()

        if (user_type == Constants.ADMIN){
            binding.btnOrder.visibility = View.GONE
            binding.llAdmin.visibility = View.VISIBLE
        }

        val service_id = arguments?.getInt(Constants.SERVICE_ID)
        if (service_id != null && curUser!=null){
            cartVm.readCartItems.observe(requireActivity()) { cart ->
                cartList.clear()
                for (i in cart){
                    cartList.add(i)
                }
            }
            serVm.readAllProducts.observe(requireActivity()) {
                for (i in it) {
                    if (i.id == service_id) loadDetails(i,  curUser)
                }
            }
            binding.btnEditService.setOnClickListener {
                editService(service_id)
            }

        } else {
            binding.llSerDet.visibility = View.GONE
            binding.tvErrDetails.visibility = View.VISIBLE
        }
    }

    private fun editService(service_id: Int) {
        findNavController().navigate(R.id.nav_service_edit_service, bundleOf(Pair(Constants.ET_SERVICE_ID, service_id)))
    }

    private fun getCartList () : List<Cart>{
        val cartList = mutableListOf<Cart>()
        cartVm.readCartItems.observe(requireActivity()){
            cartList.clear()
            for (i in it){
                cartList.add(i)
            }
        }
        return cartList
    }

    private  fun loadDetails(i: Product, curUser: String) {
        binding.tvDetailsTitle.text = i.title
        binding.tvDetailsDes.text = i.description
        binding.tvDetailsPrice.text = i.price.toString()
        binding.tvDetailsRecMaster.text = i.recommended_masters
        binding.btnOrder.setOnClickListener {
            val cartExists = checkIfProdExists( curUser, i)
            val curCart = Cart(service_id = i.id, user = curUser, title = i.title, price = i.price)
            if (cartExists == 1) Toast.makeText(requireContext(), "You already added this service to your list", Toast.LENGTH_SHORT).show()
            else {
                cartVm.addToCart(curCart)
                Toast.makeText(requireContext(), "added", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDeleteService.setOnClickListener {
            deleteService(i)
        }

    }

    private fun deleteService(i: Product) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete")
        builder.setMessage("are you sure?")
        builder.setPositiveButton("YES") { dialog, which ->
            serVm.deleteProduct(i)
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        builder.setNegativeButton("NO") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private  fun checkIfProdExists(curUser: String, i: Product) : Int{
        var cartExists = 0
        val cartList = getCartList()
        for (j in cartList){
            if (j.user == curUser && j.service_id == i.id) {
                cartExists = 1
                break
            }
            else {
                cartExists = 0
            }
        }
        return  cartExists
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
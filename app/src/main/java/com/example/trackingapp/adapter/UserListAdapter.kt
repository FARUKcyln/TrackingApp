package com.example.trackingapp.adapter

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trackingapp.R
import com.example.trackingapp.databinding.FragmentUserInformationBinding
import com.example.trackingapp.models.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList

class UserListAdapter(
    private val requireContext: Context,
    private val userList: ArrayList<User>,
    private val mMap: GoogleMap
) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding: FragmentUserInformationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.fragment_user_information, parent, false
        )
        return UserListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        holder.setBind(requireContext, userList[position], mMap)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserListViewHolder(private val binding: FragmentUserInformationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setBind(
            requireContext: Context,
            user: User,
            mMap: GoogleMap
        ) {
            var userInformationText = StringBuilder()
            userInformationText.append(user.fullName)
            userInformationText.appendLine()

            val geocoder: Geocoder = Geocoder(requireContext, Locale.getDefault())

            val address: MutableList<Address>? =
                geocoder.getFromLocation(user.latitude!!, user.longitude!!, 1)

            if (address!!.size != 0) {
                userInformationText.append(address[0].getAddressLine(0))
            }
            binding.card.setOnClickListener {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(user.latitude!!, user.longitude!!), 8f))
            }
            binding.userInformation.text = userInformationText
        }

    }

}
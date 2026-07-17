package com.yonas.focusbro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yonas.focusbro.R

class ContactFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameView = view.findViewById<TextView>(R.id.developerName)
        val emailView = view.findViewById<TextView>(R.id.developerEmail)
        val phoneView = view.findViewById<TextView>(R.id.developerPhone)

        nameView.text = "Yonas Tedla"
        emailView.text = "yonastedla06@gmail.com"
        phoneView.text = "+251707106234"
    }
}
package com.prepzen.app.ui.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.prepzen.app.R
import com.prepzen.app.databinding.FragmentAboutBinding

class AboutFragment : Fragment(R.layout.fragment_about) {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        binding.aboutText.text = getString(R.string.about_body)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

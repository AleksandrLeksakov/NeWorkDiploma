package ru.netology.nework.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMainBinding
import ru.netology.nework.model.AuthModel
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)

        var token: AuthModel? = null
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
        }


        val childNavHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val childNavController = childNavHostFragment.navController
        binding.bottomNavigation.setupWithNavController(childNavController)

        binding.topAppBar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.user -> {
                    if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                        findNavController().navigate(
                            R.id.action_mainFragment_to_detailUserFragment,
                            bundleOf(AppConst.USER_ID to token?.id)
                        )
                    } else {
                        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                    }
                    true
                }

                else -> false
            }
        }

        return binding.root
    }

}
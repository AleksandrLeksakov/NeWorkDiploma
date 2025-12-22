package ru.netology.nework.fragment.detailitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.viewpager.PagerAdapter
import ru.netology.nework.databinding.FragmentDetailUserBinding
import ru.netology.nework.extension.loadAvatar
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UserViewModel


@AndroidEntryPoint
class DetailUserFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var pager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailUserBinding.inflate(inflater, container, false)
        tabLayout = binding.tabLayout
        pager = binding.pager

        val userId = arguments?.getLong(AppConst.USER_ID)
        if (userId != null) {
            userViewModel.getUser(userId)
        }


        binding.topAppBar.inflateMenu(
            if (userId == authViewModel.dataAuth.value?.id) R.menu.user_menu
            else R.menu.empty_menu
        )


        binding.topAppBar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.exit -> {
                    authViewModel.logout()
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        pagerAdapter = PagerAdapter(this, userId)
        binding.pager.adapter = pagerAdapter

        userViewModel.dataUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.mainPhoto.loadAvatar(user.avatar)
                binding.topAppBar.title = buildString {
                    append(user.name)
                    append(" / ")
                    append(user.login)
                }
            }
        }


        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.wall)
                1 -> tab.text = getString(R.string.jobs)
            }
        }.attach()
    }

}
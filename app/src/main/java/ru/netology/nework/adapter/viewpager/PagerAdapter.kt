package ru.netology.nework.adapter.viewpager

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nework.fragment.item.JobsFragment
import ru.netology.nework.fragment.item.PostsFragment
import ru.netology.nework.util.AppConst

class PagerAdapter(fragment: Fragment, private val userId: Long?) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PostsFragment().apply {
                    arguments = bundleOf(AppConst.USER_ID to userId)
                }
            }

            1 -> {
                JobsFragment.newInstance().apply {
                    arguments = bundleOf(AppConst.USER_ID to userId)
                }
            }

            else -> {
                error("Unknown position")
            }
        }
    }
}
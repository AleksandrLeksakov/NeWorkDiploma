package ru.netology.nework.fragments.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.netology.nework.R
import ru.netology.nework.adapter.listeners.PostInteractionListener
import ru.netology.nework.adapter.recyclerview.PostAdapter
import ru.netology.nework.adapter.recyclerview.PostViewHolder
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.model.AuthModel
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class PostsFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()

        var token: AuthModel? = null
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
        }

        val userId = arguments?.getLong(AppConst.USER_ID)

        val postAdapter = PostAdapter(object : PostInteractionListener {
            override fun onLike(feedItem: FeedItem) {
                if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                    postViewModel.like(feedItem as Post)
                } else {
                    parentNavController?.navigate(R.id.action_mainFragment_to_loginFragment)
                }
            }

            override fun onDelete(feedItem: FeedItem) {
                postViewModel.deletePost(feedItem as Post)
            }

            override fun onEdit(feedItem: FeedItem) {
                feedItem as Post
                postViewModel.edit(feedItem)
                parentNavController?.navigate(
                    R.id.action_mainFragment_to_newPostFragment,
                    bundleOf(AppConst.EDIT_POST to feedItem.content)
                )
            }

            override fun onOpenCard(feedItem: FeedItem) {
                postViewModel.openPost(feedItem as Post)

                val navController = parentNavController ?: return

                val actionId = when (navController.currentDestination?.id) {
                    R.id.mainFragment -> R.id.action_mainFragment_to_detailPostFragment
                    R.id.detailUserFragment -> R.id.action_detailUserFragment_to_detailPostFragment
                    else -> R.id.action_mainFragment_to_detailPostFragment
                }

                navController.navigate(actionId)
            }
        })

        binding.recyclerViewPost.adapter = postAdapter

        if (userId != null) {
            postViewModel.loadUserWall(userId)
            postViewModel.userWall.observe(viewLifecycleOwner) { wallPosts ->
                if (wallPosts != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        postAdapter.submitData(PagingData.from(wallPosts))
                    }
                }
            }
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    postViewModel.data.collectLatest {
                        postAdapter.submitData(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                if (it.append is LoadState.Error
                    || it.prepend is LoadState.Error
                    || it.refresh is LoadState.Error
                ) {
                    Snackbar.make(
                        binding.root,
                        R.string.connection_error,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                suspendCancellableCoroutine {
                    it.invokeOnCancellation {
                        (0 until binding.recyclerViewPost.childCount)
                            .asSequence()
                            .map(binding.recyclerViewPost::getChildAt)
                            .map(binding.recyclerViewPost::getChildViewHolder)
                            .filterIsInstance<PostViewHolder>()
                            .forEach(PostViewHolder::stopPlayer)
                    }
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }

        binding.buttonNewPost.isVisible = userId == null
        binding.buttonNewPost.setOnClickListener {
            if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                parentNavController?.navigate(R.id.action_mainFragment_to_newPostFragment)
            } else {
                parentNavController?.navigate(R.id.action_mainFragment_to_loginFragment)
            }
        }

        return binding.root
    }
}
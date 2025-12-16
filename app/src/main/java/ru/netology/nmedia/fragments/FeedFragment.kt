package ru.netology.nmedia.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var auth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var binding: FragmentFeedBinding
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupAdapter() {
        adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onPostClick(post: Post) {
                // TODO: Переход к деталям поста
                Snackbar.make(binding.root, "Клик по посту ${post.id}", Snackbar.LENGTH_SHORT).show()
            }
        }, auth)
    }

    private fun setupRecyclerView() {
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        // Подписываемся на PagingData
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        // Обработка состояний загрузки Paging
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    // Обновление состояния SwipeRefresh
                    binding.swiperefresh.isRefreshing = loadState.refresh is LoadState.Loading

                    // Показываем/скрываем прогресс
                    binding.progress.isVisible = loadState.refresh is LoadState.Loading

                    // Показываем текст "пусто" если нет данных
                    binding.emptyText.isVisible = loadState.refresh is LoadState.NotLoading &&
                            adapter.itemCount == 0

                    // Показываем ошибки загрузки
                    val errorState = when {
                        loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                        loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                        loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                        else -> null
                    }

                    errorState?.let {
                        Snackbar.make(
                            binding.root,
                            "Ошибка загрузки: ${it.error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        // Swipe to refresh
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        // Кнопка обновления сверху
        binding.refreshPrependButton.setOnClickListener {
            adapter.refresh()
        }

        // Кнопка FAB для создания нового поста
        binding.fab.setOnClickListener {
            if (auth.authStateFlow.value.token != null) {
                viewModel.createNewPost("")
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
            }
        }

        // Скрываем FAB при скролле вниз
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10 && binding.fab.isShown) {
                    binding.fab.hide()
                } else if (dy < -10 && !binding.fab.isShown) {
                    binding.fab.show()
                }

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                binding.refreshPrependButton.isVisible = firstVisibleItemPosition == 0
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.fab.show()
                }
            }
        })
    }
}
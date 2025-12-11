package ru.netology.nmedia.activity

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
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    @Inject
    lateinit var repository: PostRepository

    @Inject
    lateinit var auth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
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

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        }, auth) // ДОБАВЬТЕ auth В АДАПТЕР

        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(requireContext())

        // Загрузка данных Paging
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        // Обработка состояний загрузки Paging
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    // Обновление состояния SwipeRefresh
                    binding.swiperefresh.isRefreshing = state.refresh is LoadState.Loading

                    // Показываем/скрываем прогресс
                    binding.progress.isVisible = state.refresh is LoadState.Loading

                    // Показываем текст "пусто" если нет данных
                    binding.emptyText.isVisible =
                        state.refresh is LoadState.NotLoading && adapter.itemCount == 0

                    // Показываем ошибки загрузки
                    val errorState = when {
                        state.refresh is LoadState.Error -> state.refresh as LoadState.Error
                        state.append is LoadState.Error -> state.append as LoadState.Error
                        state.prepend is LoadState.Error -> state.prepend as LoadState.Error
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

        // Swipe to refresh
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        // Кнопка FAB для создания нового поста
        binding.fab.setOnClickListener {
            // Проверяем авторизацию перед созданием поста
            if (auth.authStateFlow.value?.token != null) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                // Показать диалог с предложением войти
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

                // Показываем кнопку обновления сверху
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

        return binding.root
    }
}
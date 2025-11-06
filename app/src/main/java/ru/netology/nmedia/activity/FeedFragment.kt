package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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
                viewModel.likeById(post.id)
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
        })
        binding.list.adapter = adapter


        // Загрузка данных Paging
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }


        // Актуальный вариант
        // Обработка состояний загрузки Paging
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swiperefresh.isRefreshing = state.refresh is LoadState.Loading


                    // Показываем/скрываем прогресс и пустой текст
                    binding.progress.visibility =
                        if (state.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                    binding.emptyText.visibility =
                        if (state.refresh is LoadState.NotLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE


                    // Показываем ошибки загрузки
                    val errorState = state.refresh as? LoadState.Error
                        ?: state.append as? LoadState.Error
                        ?: state.prepend as? LoadState.Error

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

//обычный refresh
        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        // Кнопка для ручного обновления сверху
        binding.refreshPrependButton.setOnClickListener {
            viewModel.refreshPrepend()
        }

        // Наблюдение за состоянием ручного обновления сверху
        viewModel.prependState.observe(viewLifecycleOwner) { state ->
            binding.refreshPrependButton.isEnabled = !state.loading

            when {
                state.loading -> {
                    binding.refreshPrependButton//.text = "Загрузка..."
                }

                state.refreshPrependCount > 0 -> {
                    binding.refreshPrependButton//.text = "Обновить"
                    Snackbar.make(
                        binding.root,
                        "Загружено ${state.refreshPrependCount} новых постов",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // Автоматически скрываем кнопку через несколько секунд
                    binding.root.postDelayed({
                        binding.refreshPrependButton.visibility = View.GONE
                    }, 3000)
                }

                state.error -> {
                    binding.refreshPrependButton//.text = "Обновить"
                    Snackbar.make(
                        binding.root,
                        "Ошибка загрузки новых постов",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                else -> {
                    binding.refreshPrependButton//.text = "Обновить"
                }
            }
        }

        // Наблюдение за общим состоянием данных
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, "Ошибка загрузки", Snackbar.LENGTH_LONG).show()
            }
        }
        // Показываем кнопку обновления при скролле к верху
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Показываем кнопку обновления, если пользователь прокрутил к самому верху
                if (firstVisibleItemPosition == 0) {
                    binding.refreshPrependButton.visibility = View.VISIBLE
                } else {
                    // скрыть кнопку, если не вверху, или оставить видимой
                    binding.refreshPrependButton.visibility = View.GONE
                }
            }
        })


        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}

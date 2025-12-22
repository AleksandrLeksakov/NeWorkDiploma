package ru.netology.nework.fragment.detailitem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.recyclerview.AvatarAdapter
import ru.netology.nework.adapter.tools.AvatarItemDecoration
import ru.netology.nework.adapter.tools.InvolvedOnClickListener
import ru.netology.nework.databinding.FragmentDetailPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Post
import ru.netology.nework.extension.loadAttachment
import ru.netology.nework.extension.loadAvatar
import ru.netology.nework.model.InvolvedItemType
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.PostViewModel
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DetailPostFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private var player: ExoPlayer? = null
    private var placeMark: PlacemarkMapObject? = null
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailPostBinding.inflate(inflater, container, false)

        var post: Post? = null

        val avatarDecoration = AvatarItemDecoration(64)

        val likersAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment2,
                    bundleOf(AppConst.LIKERS to gson.toJson(post?.likeOwnerIds))
                )
            }
        })
        val mentionedAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment2,
                    bundleOf(AppConst.MENTIONED to gson.toJson(post?.mentionIds))
                )
            }
        })

        binding.recyclerLikers.apply {
            addItemDecoration(avatarDecoration)
            adapter = likersAdapter
        }
        binding.recyclerMentioned.apply {
            addItemDecoration(avatarDecoration)
            adapter = mentionedAdapter
        }

        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on_24)

        postViewModel.postData.observe(viewLifecycleOwner) { postItem ->
            post = postItem
            with(binding) {
                avatar.loadAvatar(postItem.authorAvatar)
                authorName.text = postItem.author
                lastWork.text = postItem.authorJob ?: getString(R.string.in_search_work)

                when (postItem.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        imageContent.loadAttachment(postItem.attachment.url)
                        imageContent.isVisible = true
                    }

                    AttachmentType.VIDEO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(postItem.attachment.url))
                        }
                        videoContent.player = player
                        videoContent.isVisible = true
                    }

                    AttachmentType.AUDIO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(postItem.attachment.url))
                        }
                        videoContent.player = player
                        audioContent.isVisible = true
                    }

                    null -> {
                        imageContent.isVisible = false
                        videoContent.isVisible = false
                        audioContent.isVisible = false
                        player?.release()
                    }
                }

                datePublished.text =
                    postItem.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                content.text = postItem.content

                lifecycleScope.launch {
                    postViewModel.getInvolved(postItem.likeOwnerIds, InvolvedItemType.LIKERS)
                }

                lifecycleScope.launch {
                    postViewModel.getInvolved(postItem.mentionIds, InvolvedItemType.MENTIONED)
                }


                buttonLike.text = postItem.likeOwnerIds.size.toString()
                buttonLike.isChecked = postItem.likedByMe
                buttonMentioned.text = postItem.mentionIds.size.toString()

                val point =
                    if (postItem.coords != null) Point(
                        postItem.coords.lat,
                        postItem.coords.long
                    ) else null
                if (point != null) {
                    if (placeMark == null) {
                        placeMark = binding.map.mapWindow.map.mapObjects.addPlacemark()
                    }
                    placeMark?.apply {
                        geometry = point
                        setIcon(imageProvider)
                        isVisible = true
                    }
                    binding.map.mapWindow.map.move(
                        CameraPosition(
                            point,
                            13.0f,
                            0f,
                            0f
                        )
                    )
                } else {
                    placeMark = null
                }
                binding.map.isVisible = placeMark != null && point != null

                playPauseAudio.setOnClickListener {
                    if (player?.isPlaying == true) {
                        player!!.playWhenReady = !player!!.playWhenReady
                    } else {
                        player?.apply {
                            prepare()
                            play()
                        }
                    }
                }

                player?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        binding.playPauseAudio.setIconResource(
                            if (isPlaying) R.drawable.ic_pause_24 else R.drawable.ic_play_arrow_24
                        )
                    }
                })

            }
        }

        postViewModel.involvedData.observe(viewLifecycleOwner) { involved ->
            likersAdapter.submitList(involved.likers)
            mentionedAdapter.submitList(involved.mentioned)
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        player?.apply {
            stop()
        }
        postViewModel.resetInvolved()
    }

}
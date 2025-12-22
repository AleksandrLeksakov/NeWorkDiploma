package ru.netology.nework.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nework.R

fun ImageView.loadCircleCrop(url: String, placeholderResId: Int = R.drawable.ic_image_placeholder) {
    Glide.with(this)
        .load(url)
        .placeholder(placeholderResId)
        .error(placeholderResId)
        .transform(CircleCrop())
        .into(this)
}
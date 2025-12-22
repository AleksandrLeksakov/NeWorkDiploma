package ru.netology.nework.extension

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nework.R

fun ImageView.loadAvatar(url: String?) {
    Glide.with(this)
        .load(url)
        .error(R.drawable.ic_account_circle_24)
        .placeholder(R.drawable.ic_account_circle_24)
        .timeout(10_000)
        .circleCrop()
        .into(this)
}

fun ImageView.loadAttachment(url: String?) {
    if (url == null) {
        return
    }
    Glide.with(this)
        .load(url)
        .error(R.drawable.ic_account_circle_24)
        .timeout(10_000)
        .into(this)
}
package de.fqsmedia.cedrik.surfcity_android.ui.adapters

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BindingHolder<T : ViewDataBinding>(val view: View) : RecyclerView.ViewHolder(view) {
   var binding: T? = null
        private set

    init {
        try {
            binding = DataBindingUtil.bind(view)
        } catch (e: IllegalArgumentException) {
            binding = null
        }

    }
}
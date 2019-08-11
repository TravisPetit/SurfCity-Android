package de.fqsmedia.cedrik.surfcity_android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.fqsmedia.cedrik.surfcity_android.R
import de.fqsmedia.cedrik.surfcity_android.databinding.ItemFeedBinding
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Message

class PublicFeedAdapter : RecyclerView.Adapter<BindingHolder<ItemFeedBinding>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemFeedBinding> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
        return BindingHolder(itemView)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: BindingHolder<ItemFeedBinding>, position: Int) {
        val message = messages[position]
        val binding = holder.binding as ItemFeedBinding
        binding.message = message
    }

    private var messages: List<Message> = ArrayList()

    fun setMessages(messages: List<Message>) {
        this.messages = messages
        notifyDataSetChanged()
    }


}
package de.fqsmedia.cedrik.surfcity_android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.fqsmedia.cedrik.surfcity_android.R
import de.fqsmedia.cedrik.surfcity_android.databinding.ItemPrivateMessageBinding
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.PrivateMessage

class PrivateFeedAdapter : RecyclerView.Adapter<BindingHolder<ItemPrivateMessageBinding>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemPrivateMessageBinding> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_private_message, parent, false)
        return BindingHolder(itemView)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: BindingHolder<ItemPrivateMessageBinding>, position: Int) {
        val message = messages[position]
        val binding = holder.binding as ItemPrivateMessageBinding
        binding.privateMessage = message
    }

    private var messages: List<PrivateMessage> = ArrayList()

    fun setMessages(messages: List<PrivateMessage>) {
        this.messages = messages
        notifyDataSetChanged()
    }


}
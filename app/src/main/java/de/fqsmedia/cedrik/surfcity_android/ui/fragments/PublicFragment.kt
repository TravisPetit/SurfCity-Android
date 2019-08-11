package de.fqsmedia.cedrik.surfcity_android.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import de.fqsmedia.cedrik.surfcity_android.R
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Message
import de.fqsmedia.cedrik.surfcity_android.ui.MessageViewModel
import de.fqsmedia.cedrik.surfcity_android.ui.adapters.PublicFeedAdapter
import kotlinx.android.synthetic.main.fragment_public.view.*

class PublicFragment : Fragment() {
    private lateinit var messagesViewModel: MessageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_public, container, false)
        val adapter = PublicFeedAdapter()

        view.feed_recycler.layoutManager = LinearLayoutManager(context)
        view.feed_recycler.adapter = adapter

        messagesViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        messagesViewModel.getAllByType("post").observe(this,
            Observer<List<Message>> {
                messages -> adapter.setMessages(messages)
            })

        return view
    }
}

package de.fqsmedia.cedrik.surfcity_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.fqsmedia.cedrik.surfcity_android.R
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.PrivateMessage
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.ui.MessageViewModel
import de.fqsmedia.cedrik.surfcity_android.ui.adapters.PrivateFeedAdapter
import kotlinx.android.synthetic.main.fragment_public.view.*

class PrivateFragment(val identity: Identity) : Fragment() {

    private lateinit var messagesViewModel: MessageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_private, container, false)
        val adapter = PrivateFeedAdapter()

        view.feed_recycler.layoutManager = LinearLayoutManager(context)
        view.feed_recycler.adapter = adapter
        messagesViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        messagesViewModel.getPrivate().observe(this,
            Observer<List<PrivateMessage>> {
                    messages -> adapter.setMessages(messages)
            })

        return view
    }
}

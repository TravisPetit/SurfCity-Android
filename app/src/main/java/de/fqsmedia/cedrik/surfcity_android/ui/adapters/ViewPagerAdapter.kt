package de.fqsmedia.cedrik.surfcity_android.ui.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import de.fqsmedia.cedrik.surfcity_android.R
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.ui.fragments.PrivateFragment
import de.fqsmedia.cedrik.surfcity_android.ui.fragments.PublicFragment

class ViewPagerAdapter internal constructor(fragmentManager: FragmentManager, val context: Context, val identity: Identity) : FragmentPagerAdapter(fragmentManager) {
    private val tabCount = 2

    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(position: Int): Fragment? {
        var fragment: Fragment? = null
        when(position) {
            0 -> fragment = PublicFragment()
            1 -> fragment = PrivateFragment(identity)
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0 -> return context.resources.getString(R.string.public_tab_name)
            1 -> return context.resources.getString(R.string.private_tab_name)
        }
        return null
    }
}
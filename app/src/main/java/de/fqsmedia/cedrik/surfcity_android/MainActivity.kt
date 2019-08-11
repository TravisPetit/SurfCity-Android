package de.fqsmedia.cedrik.surfcity_android


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.goterl.lazycode.lazysodium.utils.Key
import de.fqsmedia.cedrik.surfcity_android.dagger.DaggerSurfCityComponent
import de.fqsmedia.cedrik.surfcity_android.dagger.SurfCityModule
import de.fqsmedia.cedrik.surfcity_android.ssb.SurfCity
import de.fqsmedia.cedrik.surfcity_android.ssb.data.SurfCityDatabase
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Pub
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.Invite
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.SecretHandler
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import de.fqsmedia.cedrik.surfcity_android.ui.adapters.ViewPagerAdapter
import javax.inject.Inject
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var database: SurfCityDatabase

    @Inject
    lateinit var secretHandler: SecretHandler
    private lateinit var surfCity: SurfCity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerSurfCityComponent.builder().module(SurfCityModule(this)).build().inject(this)
        Log.d("IDENTITY", secretHandler.getIdentity().getString())


        val viewPager = findViewById<ViewPager>(R.id.pager)
        if (viewPager != null) {
            val adapter = ViewPagerAdapter(supportFragmentManager, this, secretHandler.getIdentity())
            viewPager.adapter = adapter
        }

        surfCity = SurfCity(secretHandler, this, this, database)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sync -> {
                thread(start = true) { surfCity.syncFeeds() }
                return true
            }
            R.id.action_add_pub -> {
                showPubDialog()
                return true
            }
            R.id.action_import -> {
                showImportDialog()
                return true
            }
            R.id.action_process -> {
                thread(start = true) { surfCity.processMessages() }
                return true
            }
            else ->
                return false
        }
    }

    private fun showPubDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val promptView = layoutInflater.inflate(R.layout.pub_input_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(promptView)

        val editText = promptView.findViewById(R.id.edittext) as EditText
        alertDialogBuilder.setCancelable(false)
            .setPositiveButton(
                "Add"
            ) { _, _ -> addPub(editText.text.toString()) }
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun showImportDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val promptView = layoutInflater.inflate(R.layout.import_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(promptView)

        val secretText = promptView.findViewById(R.id.secretinput) as EditText
        alertDialogBuilder.setCancelable(false)
            .setPositiveButton(
                "Import"
            ) { _, _ -> importIdentity(secretText.text.toString()) }
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun importIdentity(secret: String) {
        secretHandler.importIdentity(Identity.fromSecretKey(Key.fromBytes(Base64.decode(secret, Base64.DEFAULT))))
        val toast = Toast.makeText(applicationContext, "Imported. Restart App.", Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun addPub(pubstring: String) {
        if (pubstring.contains("~")) {
            val invite = Invite.fromString(pubstring)
            if (invite != null)
                surfCity.addPub(
                    Pub(pubkey = invite.pubKey, host = invite.host, port = invite.port)
                ) else {
                val toast = Toast.makeText(applicationContext, "Invalid Invite Code", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        else {
            val components = pubstring.split(":")
            val pubkey = RPCIdentifier.fromString(components[2])
            if(pubkey != null){
                val host = components[0]
                val port = components[1].toInt()
                surfCity.addPub(
                    Pub(pubkey = pubkey, host = host, port = port)
                )
            }else{
                val toast = Toast.makeText(applicationContext, "Invalid Pub String", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}

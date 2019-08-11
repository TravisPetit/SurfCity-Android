package de.fqsmedia.cedrik.surfcity_android.ssb.identity

import android.content.Context
import android.util.Base64
import android.util.Log
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import org.json.JSONObject
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class SecretHandler(private val context: Context) {

    private var currentIdentity : Identity

    init {
        var identity = readSecret()
        if(identity == null){
            identity = Identity.random()
            createSecret(identity = identity)
        }
        currentIdentity = identity
    }

    fun getIdentity() : Identity {
        return currentIdentity
    }

    private fun createSecret(curve : String = "ed25519", identity: Identity) {
        val jsonSecret = "# this is your SECRET name.\n" +
                "# this name gives you magical powers.\n" +
                "# with it you can mark your messages so that your friends can verify\n" +
                "# that they really did come from you.\n" +
                "#\n" +
                "# if any one learns this name, they can use it to destroy your identity\n" +
                "# NEVER show this to anyone!!!\n" +
                "\n" +
                "{\n" +
                "  \"curve\": \"" + curve + "\",\n" +
                "  \"public\": \"" + identity.getString().replace("@","") + "\",\n" +
                "  \"private\": \"" + identity.getSecret() + "\",\n" +
                "  \"id\": \"" + identity.getString() + "\"\n" +
                "}\n" +
                "\n" +
                "# WARNING! It's vital that you DO NOT edit OR share your secret name\n" +
                "# instead, share your public name\n" +
                "# your public name: " + identity.getString()

        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput("secret", Context.MODE_PRIVATE)
            fileOutputStream.write(jsonSecret.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun readSecret(): Identity? {
        try {
            val inputStream = context.openFileInput("secret")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonSecret = String(buffer, StandardCharsets.UTF_8)

            val jsonObject = JSONObject(jsonSecret)
            if (jsonObject.getString("curve") == "ed25519") {

                return Ed25519Identity(
                    KeyPair(
                        Key.fromBytes(Base64.decode(jsonObject.getString("public").removeSuffix(".ed25519"), Base64.NO_WRAP)),
                        Key.fromBytes(Base64.decode(jsonObject.getString("private").removeSuffix(".ed25519"), Base64.NO_WRAP))
                    )
                )
            }

        } catch (e: java.lang.Exception) {
            Log.d("SECRET HANDLER", e.message)
            return null
        }

        return null
    }

    fun importIdentity(identity: Identity) {
        context.deleteFile("secret")
        createSecret(identity = identity)
        val identity = readSecret()
        if(identity != null){
            currentIdentity = identity
        } else{
            throw IdentityException("Could not import identity.")
        }
    }
}
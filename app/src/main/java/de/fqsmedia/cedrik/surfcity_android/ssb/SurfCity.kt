package de.fqsmedia.cedrik.surfcity_android.ssb

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import de.fqsmedia.cedrik.surfcity_android.ssb.connection.ConnectionPool
import de.fqsmedia.cedrik.surfcity_android.ssb.data.SurfCityDatabase
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.FeedDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.FollowDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.ProcessDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.PubDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Feed
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Follow
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Pub
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.SecretHandler
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import java.util.*
import java.util.concurrent.*

class SurfCity(
    private val secretHandler: SecretHandler,
    val context: Context,
    lifecycleOwner: LifecycleOwner,
    val database: SurfCityDatabase
) {


    private val processDAO: ProcessDAO = database.processDAO()
    private val followDAO: FollowDAO = database.followDAO()
    private val numberOfCores = Runtime.getRuntime().availableProcessors()
    private val executorPool: ScheduledExecutorService
    private val pubDAO: PubDAO = database.pubDAO()
    private val feedDAO: FeedDAO = database.feedDAO()
    private val connectionPool: ConnectionPool

    init {

        executorPool = Executors.newScheduledThreadPool(
            numberOfCores,
            threadFactory("SSB Pool", true)
        )

        connectionPool = ConnectionPool(
            identity = secretHandler.getIdentity(),
            context = context
        )
        addPubsToPool()
        addOwnIdentityAsFeed()
        executorPool.scheduleAtFixedRate(
            { processMessages() },
            60,
            60,
            TimeUnit.SECONDS
        )
        executorPool.scheduleAtFixedRate(
            {
                connectionPool.sync()
            },
            5,
            60,
            TimeUnit.SECONDS
        )
    }

    companion object {
        @JvmStatic
        fun threadFactory(name: String, daemon: Boolean): ThreadFactory {
            return ThreadFactory { runnable ->
                val result = Thread(runnable, name)
                result.isDaemon = daemon
                result
            }
        }
    }

    private fun addPubsToPool() {
        executorPool.execute {
            val pubs = pubDAO.getAll()
            for (pub in pubs) {
                connectionPool.add(pub.host, pub.port, Base64.decode(pub.pubkey.keyHash, Base64.DEFAULT).toByteString())

            }
        }

    }

    fun syncFeeds() {
        executorPool.execute { connectionPool.sync() }
    }

    fun addPub(pub: Pub) {
        executorPool.submit { pubDAO.insert(pub) }
        connectionPool.add(pub.host, pub.port, Base64.decode(pub.pubkey.keyHash, Base64.DEFAULT).toByteString())
    }


    private fun addOwnIdentityAsFeed() {
        executorPool.submit {
            val pubkey = RPCIdentifier.fromString(secretHandler.getIdentity().getString())!!
            val feedID = processDAO.getFeedIDByKey(pubkey)
            if (feedID == 0) {
                feedDAO.insertPeer(Feed(id = null, pubkey = pubkey, scan_low = 1, front_seq = 0, front_prev = null))
            }
        }
    }

    fun processMessages() {
        Log.d("PROCESSING: ", "STARTED")
        updateFeeds()
        updateNames()
        generateFeedsByFollows()
        cleanMessages()
        Log.d("PROCESSING: ", "FINISHED")
    }

    private fun updateFeeds() {
        val feedIDs = processDAO.getAllFeeds()

        for (id in feedIDs) {
            val pubkey = processDAO.getPubkeyById(id)
            val sequence = processDAO.getFrontSequence(pubkey)
            val previous = processDAO.getFrontPrevious(pubkey)
            processDAO.updateFeed(id, sequence, previous)
        }
    }

    private fun updateNames() {
        val feedIDs = processDAO.getAllFeeds()
        for (id in feedIDs) {
            val names = processDAO.getNamesFromMessages(id)
            if (names.isNotEmpty()) {
                val oldNames = processDAO.getNamesById(id)
                if (oldNames == null) {
                    processDAO.insertIntoAbout(id, names.toString())
                } else {
                    processDAO.updateNamesOfId(id, names.union(oldNames.toList()).toString())
                }
            }
        }
    }

    private fun generateFeedsByFollows() {
        val follows =
            processDAO.getFollowingsByPubkey(RPCIdentifier.fromString(secretHandler.getIdentity().getString())!!)
        val ownID = processDAO.getFeedIDByKey(RPCIdentifier.fromString(secretHandler.getIdentity().getString())!!)
        if (follows.isNotEmpty()) {
            for (follow in follows) {
                var idDB = processDAO.getFeedIDByKey(follow).toLong()
                if (idDB == 0L) {
                    idDB = feedDAO.insertPeer(
                        Feed(null, follow, 1, 0, null)
                    )
                }
                followDAO.insert(Follow(ownID, idDB.toInt(), 1))
            }
        }
    }

    private fun cleanMessages() {
        processDAO.deleteMessagesLeaveOnlyType(
            listOf("post", "private")
        )
        val limit = Date(System.currentTimeMillis() - Constants.frontierWindow)
        processDAO.forgetMessagesByTimeLimit(limit)
    }


}
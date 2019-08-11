package de.fqsmedia.cedrik.surfcity_android.ssb.data.utils

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import de.fqsmedia.cedrik.surfcity_android.ssb.data.content.Address
import de.fqsmedia.cedrik.surfcity_android.ssb.data.content.VoteInfo
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import java.util.*

class DBTypeConverters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun toUri(uri: Uri?): String {
            return uri.toString()
        }

        @TypeConverter
        @JvmStatic
        fun fromUri(from: String): Uri {
            return Uri.parse(from)
        }

        @TypeConverter
        @JvmStatic
        fun toDate(timestamp: Long): Date {
            return Date(timestamp)
        }

        @TypeConverter
        @JvmStatic
        fun fromDate(date: Date): Long {
            return date.time
        }

        private const val IDENTIFER_REGEX = "([@%&])([a-zA-Z0-9+/]*={0,3})(\\.)(\\w)+"

        @TypeConverter
        @JvmStatic
        fun fromString(from: String): RPCIdentifier? {
            val regex = Regex(IDENTIFER_REGEX)
            if (from.length < 4 || regex.matchEntire(from) == null)
                return null

            val type = RPCIdentifier.IdentifierType.fromChar(from[0])
            val algoRegex = Regex("([@%&])([a-zA-Z0-9+/]*={0,3})(\\.)")

            val split = algoRegex.split(from, 2)
            RPCIdentifier.AlgoType.fromString(split[1])?.let { algo ->
                val keyHash = from.substring(1 until (from.length - 1 - algo.algo.length))
                return RPCIdentifier(keyHash, algo, type)
            }
            return null
        }

        @TypeConverter
        @JvmStatic
        fun toString(identifier: RPCIdentifier?): String {
            return identifier?.toString() ?: "null"
        }

        @TypeConverter
        @JvmStatic
        fun listToString(list: List<RPCIdentifier>?): String {
            return Gson().toJson(list)
        }

        @TypeConverter
        @JvmStatic
        fun identifierStringToList(identifier: String): List<RPCIdentifier>? {
            return Gson().fromJson(identifier, Array<RPCIdentifier>::class.java)?.asList()
        }

        @TypeConverter
        @JvmStatic
        fun addressToString(address: Address): String {
            return Gson().toJson(address)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAddress(address: String): Address {
            return Gson().fromJson(address, Address::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun voteInfoToString(voteInfo: VoteInfo): String {
            return Gson().toJson(voteInfo)
        }

        @TypeConverter
        @JvmStatic
        fun stringToVoteInfo(voteinfo: String): VoteInfo {
            return Gson().fromJson(voteinfo, VoteInfo::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun namesToString(names: List<String>): String{
            return names.toString()
        }

        @TypeConverter
        @JvmStatic
        fun stringToNames(name: String): List<String>{
            return listOf(name)
        }
    }
}
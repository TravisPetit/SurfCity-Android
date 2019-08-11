package de.fqsmedia.cedrik.surfcity_android.ssb.rpc

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson


class RPCIdentifier(val keyHash: String, val algorithm: AlgoType, val type: IdentifierType){

    companion object {
        private const val IDENTIFER_REGEX = "([@%&])([a-zA-Z0-9+/]*={0,3})(\\.)(\\w)+"

        @TypeConverter
        @JvmStatic
        fun fromString(from: String): RPCIdentifier? {
            val regex = Regex(IDENTIFER_REGEX)
            if (from.length < 4 || regex.matchEntire(from) == null)
                return null

            val type = IdentifierType.fromChar(from[0])
            val algoRegex = Regex("([@%&])([a-zA-Z0-9+/]*={0,3})(\\.)")

            val split = algoRegex.split(from, 2)
            AlgoType.fromString(split[1])?.let { algo ->
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
    }

    enum class AlgoType(val algo: String) {
        SHA256("sha256"),
        ED25519("ed25519");

        companion object {
            private val map = values().associateBy(AlgoType::algo)
            fun fromString(type: String) = map[type]
        }
    }

    enum class IdentifierType(val symbol: Char) {
        IDENTITY('@'),
        MESSAGE('%'),
        BLOB('&');

        companion object {
            fun fromChar(symbol: Char) = values().first { it.symbol == symbol }
        }
    }

    override fun toString(): String {
        return "${type.symbol}$keyHash.${algorithm.algo}"
    }

    override fun hashCode(): Int {
        var result = keyHash.hashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other != null
                && other is RPCIdentifier
                && keyHash == other.keyHash
                && algorithm == other.algorithm
                && type == other.type
    }

    class IdentifierJsonAdapter {
        @FromJson
        fun fromJson(from: String): RPCIdentifier {
            return fromString(from)!!
        }

        @ToJson
        fun toJson(value: RPCIdentifier): String {
            return value.toString()
        }

    }

}
package de.fqsmedia.cedrik.surfcity_android.ssb.rpc

import okio.ByteString

data class RPCMessage(
    val stream: Boolean = true,
    val endError: Boolean = false,
    val bodyType: RPCProtocol.Companion.RPCBodyType = RPCProtocol.Companion.RPCBodyType.JSON,
    val bodyLength: Int,
    val requestNumber: Int,
    val body: ByteString
)
package de.fqsmedia.cedrik.surfcity_android.ssb.data.utils

import com.squareup.moshi.*
import java.lang.reflect.Type
import java.util.*
import java.util.Collections.singletonList


class Adapters {
    class DataTypeAdapter {
        @FromJson
        fun fromJson(jsonReader: JsonReader): Date {
            return Date(jsonReader.nextDouble().toLong())
        }

        @ToJson
        fun toJson(jsonWriter: JsonWriter, date: Date){
            jsonWriter.value(date.time)
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    @JsonQualifier
    annotation class SingleToArray
    class SingleToArrayAdapter(
        private val delegateAdapter: JsonAdapter<List<Any>>,
        private val elementAdapter: JsonAdapter<Any>
    ) : JsonAdapter<List<Any>>() {

        companion object {
            val INSTANCE = SingleToArrayAdapterFactory()
        }

        override fun fromJson(reader: JsonReader): List<Any> =
            (if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
                singletonList(elementAdapter.fromJson(reader)) as List<Any>?
            } else delegateAdapter.fromJson(reader))!!

        override fun toJson(writer: JsonWriter, value: List<Any>?){
            if(value?.size == 1){
                elementAdapter.toJson(writer, value[0])
            } else {
                delegateAdapter.toJson(writer, value)
            }
        }


        class SingleToArrayAdapterFactory : Factory {
            override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): SingleToArrayAdapter? {
                val delegateAnnotations = Types.nextAnnotations(annotations, SingleToArray::class.java) ?: return null
                if (Types.getRawType(type) != List::class.java) throw IllegalArgumentException("Only lists may be annotated with @SingleToArray. Found: $type")
                val elementType = Types.collectionElementType(type, List::class.java)
                val delegateAdapter: JsonAdapter<List<Any>> = moshi.adapter(type, delegateAnnotations)
                val elementAdapter: JsonAdapter<Any> = moshi.adapter(elementType)

                return SingleToArrayAdapter(delegateAdapter, elementAdapter)
            }
        }
    }
}
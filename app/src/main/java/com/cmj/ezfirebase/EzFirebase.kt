package com.cmj.ezfirebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlin.reflect.full.memberProperties

class EzFirebase(var databaseRef: DatabaseReference) {
    fun stringToFullReference(path: String): DatabaseReference{
        var finalReference = databaseRef
        val nodes = path.split('/')
        nodes.forEach { child ->
            finalReference = finalReference.child(child)
        }

        return finalReference
    }

    inline fun <reified T : Any> getObjectByValue(
        reference: String,
        propertyName: String,
        propertyValue: Any,
        crossinline callback: (T?) -> Unit
    ) {
        stringToFullReference(reference).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundObject: T? = null
                for (child in snapshot.children) {
                    val pojo = child.getValue(T::class.java)
                    if (pojo != null) {
                        val property = T::class.memberProperties.find { it.name == propertyName }
                        if (property != null) {
                            val value = property.getter.call(pojo)
                            if (value == propertyValue) {
                                foundObject = pojo
                                break
                            }
                        } else {
                            Log.e("EzFirebase", "Property $propertyName not found in class ${T::class.simpleName}")
                        }
                    }
                }
                callback(foundObject)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EzFirebase", "Error getting object: ${error.message}")
                callback(null)
            }
        })
    }

    fun putObject(
        reference: String,
        value: Any
    ): String {
        val ref = stringToFullReference(reference)
        val key = ref.push().key!!

        ref.child(key).setValue(value)
        return key
    }

    fun updateObject(
        reference: String,
        key: String,
        value: Any
    ) {
        stringToFullReference(reference).child(key).setValue(value)
    }
}
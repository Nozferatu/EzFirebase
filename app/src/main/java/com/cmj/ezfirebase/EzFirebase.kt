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

    inline fun <reified T : Any> getObjectsByValue(
        reference: String,
        propertyName: String,
        propertyValue: Any,
        crossinline callback: (List<T>?) -> Unit
    ) {
        stringToFullReference(reference).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var objectsList = mutableListOf<T>()
                for (child in snapshot.children) {
                    val pojo = child.getValue(T::class.java)
                    if (pojo != null) {
                        val property = T::class.memberProperties.find { it.name == propertyName }
                        if (property != null) {
                            val value = property.getter.call(pojo)
                            if (value == propertyValue) {
                                objectsList.add(pojo)
                                break
                            }
                        } else {
                            Log.e("EzFirebase", "Property $propertyName not found in class ${T::class.simpleName}")
                        }
                    }
                }
                callback(objectsList)
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

    inline fun <reified T : Any> getObjectsOnDataChangeByValue(
        reference: String,
        propertyName: String,
        propertyValue: Any,
        crossinline callback: (List<T>?) -> Unit
    ){
        val ref = stringToFullReference(reference)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<T>()
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(T::class.java)
                    if (item != null) {
                        val property = T::class.memberProperties.find { it.name == propertyName }
                        if (property != null) {
                            val value = property.getter.call(item)
                            if (value == propertyValue) {
                                items.add(item)
                            }
                        } else {
                            Log.e("EzFirebase", "Property $propertyName not found in class ${T::class.simpleName}")
                        }
                    }
                }
                callback(items)
            }

            override fun onCancelled(databaseError: DatabaseError) { callback(null) }
        }

        ref.addValueEventListener(valueEventListener)
    }
}
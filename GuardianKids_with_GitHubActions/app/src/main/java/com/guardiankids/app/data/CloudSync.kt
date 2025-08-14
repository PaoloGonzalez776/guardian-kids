package com.guardiankids.app.data

import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CloudSync(private val ctx: Context) {
    private val db = Firebase.firestore
    private fun childDoc(childId: String) = db.collection("children").document(childId)

    fun pushRules(childId: String, rules: Map<String, Any>, onDone: (() -> Unit)? = null) {
        childDoc(childId).collection("config").document("rules").set(rules).addOnSuccessListener { onDone?.invoke() }
    }

    fun pushEvent(childId: String, event: Map<String, Any>) {
        childDoc(childId).collection("events").add(event + mapOf("ts" to FieldValue.serverTimestamp()))
    }

    fun pullRules(childId: String, onRules: (Map<String, Any>) -> Unit) {
        childDoc(childId).collection("config").document("rules").get().addOnSuccessListener {
            onRules(it.data ?: emptyMap())
        }
    }
}

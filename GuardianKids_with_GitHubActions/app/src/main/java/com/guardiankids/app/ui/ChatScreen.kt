package com.guardiankids.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

data class ChatMsg(val text:String="", val from:String="", val ts:Long=0)

@Composable
fun ChatScreen(roomId: String = "parent-kid-001", sender:String = "kid"){
    val db = Firebase.firestore
    var input by remember { mutableStateOf("") }
    var msgs by remember { mutableStateOf(listOf<ChatMsg>()) }

    LaunchedEffect(roomId){
        db.collection("chats").document(roomId).collection("messages")
            .orderBy("ts")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { it.toObject(ChatMsg::class.java) } ?: emptyList()
                msgs = list
            }
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Chat seguro padre–hijo (Firestore)", style=MaterialTheme.typography.titleMedium)
        LazyColumn(modifier=Modifier.weight(1f)) {
            items(msgs){ m ->
                Text("${m.from}: " + try { com.guardiankids.app.data.ChatCrypto.decrypt(m.text) } catch (e:Exception){ "[mensaje]" })
            }
        }
        Row {
            OutlinedTextField(value=input,onValueChange={input=it},modifier=Modifier.weight(1f),label={ Text("Mensaje") })
            Spacer(Modifier.width(8.dp))
            Button(onClick={
                if (input.isNotBlank()) {
                    val m = hashMapOf("text" to com.guardiankids.app.data.ChatCrypto.encrypt(input), "from" to sender, "ts" to Date().time)
                    db.collection("chats").document(roomId).collection("messages").add(m)
                    input = ""
                }
            }){ Text("Enviar") }
        }
    }
}

package com.example.smartfrs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val chatList: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].isBot) VIEW_TYPE_BOT else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatList[position]
        if (holder is UserViewHolder) {
            holder.tvUserMessage.text = chatMessage.message
        } else if (holder is BotViewHolder) {
            holder.tvBotMessage.text = chatMessage.message
        }
    }

    override fun getItemCount(): Int = chatList.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
    }

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBotMessage: TextView = itemView.findViewById(R.id.tvBotMessage)
    }
}
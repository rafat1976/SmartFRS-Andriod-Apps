package com.example.smartfrs

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.QuotaExceededException
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupportActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SupportActivity"
        private const val MODEL_NAME = "gemini-3.6-flash"
    }

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var fabScrollToBottom: FloatingActionButton

    private val chatList = ArrayList<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private var generativeModel: GenerativeModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)
        fabScrollToBottom = findViewById(R.id.fabScrollToBottom)

        adapter = ChatAdapter(chatList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        fabScrollToBottom.setOnClickListener {
            scrollToBottom()
        }

        setupScrollListener()

        // বটের প্রথম মেসেজ
        if (chatList.isEmpty()) {
            addBotMessage("হ্যালো! SmartFRS সাপোর্টে আপনাকে স্বাগতম। আমি আপনাকে কীভাবে সাহায্য করতে পারি?")
        }

        setupGeminiModel()

        btnSend.setOnClickListener {
            val userMsg = etMessage.text.toString().trim()
            if (userMsg.isNotEmpty()) {
                addUserMessage(userMsg)
                etMessage.text.clear()
                generateBotResponse(userMsg)
            }
        }
    }

    private fun setupScrollListener() {
        chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                // স্ক্রোল উপরে নামালে বা মেসেজ একাধিক হলে scroll to bottom বাটনটি দৃশ্যমান হবে
                if (chatList.isNotEmpty() && lastVisibleItemPosition < chatList.size - 2) {
                    fabScrollToBottom.show()
                } else {
                    fabScrollToBottom.hide()
                }
            }
        })
    }

    private fun scrollToBottom() {
        if (chatList.isNotEmpty()) {
            chatRecyclerView.smoothScrollToPosition(chatList.size - 1)
        }
    }

    private fun setupGeminiModel() {
        val apiKey = BuildConfig.API_KEY
        Log.d(TAG, "Checking API Key loading status...")
        Log.d(TAG, "API Key Length: ${apiKey.length}")

        if (apiKey.isBlank()) {
            Log.e(TAG, "API Key is empty in BuildConfig!")
            addBotMessage("API Key missing! local.properties ফাইলে সঠিক API Key সেট করে Rebuild Project করুন।")
            return
        }

        try {
            generativeModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey,
                systemInstruction = content {
                    text("""
                        তুমি 'SmartFRS' অ্যাপের একজন কাস্টমার সাপোর্ট অ্যাসিস্ট্যান্ট। 
                        ইউজার যদি কোনো ভুল তথ্য দেয় বা অ্যাপ ব্যবহার করতে গিয়ে কোনো সমস্যায় পড়ে, তবে তাকে সঠিক নিয়মটি বুঝিয়ে বলবে এবং সাহায্য করবে।
                        
                        তোমার একটি কঠোর নিয়ম আছে: যদি ইউজারের কোনো সমস্যার সমাধান তোমার জানা না থাকে, বা তুমি বুঝতে না পারো, অথবা তোমার ক্ষমতার বাইরে হয়, তখন তুমি নিজে থেকে কোনো ভুল তথ্য বানাবে না। 
                        সেই পরিস্থিতিতে তুমি শুধুমাত্র এই কথাটি বলবে: "দুঃখিত, আমি এই বিষয়ে আপনাকে সাহায্য করতে পারছি না। বিস্তারিত সহায়তার জন্য অনুগ্রহ করে আমাদের হটলাইন নম্বরে যোগাযোগ করুন: +8804224012"
                    """.trimIndent())
                }
            )
            Log.i(TAG, "GenerativeModel initialized successfully ($MODEL_NAME)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize GenerativeModel", e)
            addBotMessage("মডেল ইনিশিয়ালাইজ করতে সমস্যা হয়েছে: ${e.message}")
        }
    }

    private fun addUserMessage(message: String) {
        chatList.add(ChatMessage(message, false))
        adapter.notifyItemInserted(chatList.size - 1)
        scrollToBottom()
    }

    private fun addBotMessage(message: String) {
        chatList.add(ChatMessage(message, true))
        adapter.notifyItemInserted(chatList.size - 1)
        scrollToBottom()
    }

    // ইউজারের মেসেজের রিপ্লাই জেনারেট করার ফাংশন
    private fun generateBotResponse(userInput: String) {
        val model = generativeModel
        if (model == null) {
            addBotMessage("API Key missing বা মডেল প্রস্তুত নয়। local.properties এ সঠিক Key সেট করুন।")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Sending prompt to Gemini: $userInput")
                val response = model.generateContent(userInput)
                val botReply = response.text

                withContext(Dispatchers.Main) {
                    if (!botReply.isNullOrBlank()) {
                        addBotMessage(botReply)
                    } else {
                        addBotMessage("দুঃখিত, আমি কোনো উত্তর জেনারেট করতে পারিনি।")
                    }
                }
            } catch (e: QuotaExceededException) {
                Log.w(TAG, "QuotaExceededException: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    addBotMessage("API সীমা অতিক্রম করেছে (Quota Exceeded)! অনুগ্রহ করে ৩০-৬০ সেকেন্ড অপেক্ষা করে আবার চেষ্টা করুন অথবা Google AI Studio থেকে নতুন API Key ব্যবহার করুন।")
                }
            } catch (e: ServerException) {
                Log.e(TAG, "ServerException: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    val msg = e.message ?: ""
                    when {
                        msg.contains("404") -> {
                            addBotMessage("Error 404: $MODEL_NAME মডেলটি পাওয়া যায়নি।")
                        }
                        msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") -> {
                            addBotMessage("API সীমা অতিক্রম করেছে (429 Rate Limit)! অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।")
                        }
                        msg.contains("503") || msg.contains("UNAVAILABLE") || msg.contains("high demand") -> {
                            addBotMessage("গুগল এআই সার্ভারে এই মুহূর্তে অতিরিক্ত চাপ রয়েছে (503 High Demand)। অনুগ্রহ করে ১০-১৫ সেকেন্ড পর আবার চেষ্টা করুন।")
                        }
                        else -> {
                            addBotMessage("গুগল সার্ভার সমস্যা (503): কিছুক্ষণ পর আবার চেষ্টা করুন।")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "SDK Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    val msg = e.message ?: ""
                    if (msg.contains("503") || msg.contains("UNAVAILABLE") || msg.contains("high demand")) {
                        addBotMessage("গুগল এআই সার্ভারে এই মুহূর্তে অতিরিক্ত চাপ রয়েছে (503 High Demand)। অনুগ্রহ করে ১০-১৫ সেকেন্ড পর আবার চেষ্টা করুন।")
                    } else {
                        addBotMessage("ত্রুটি ঘটেছে: নেটওয়ার্ক বা সার্ভারে সমস্যা। আবার চেষ্টা করুন।")
                    }
                }
            }
        }
    }
}

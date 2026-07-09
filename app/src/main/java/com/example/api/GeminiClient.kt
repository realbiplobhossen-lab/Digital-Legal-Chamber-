package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Generates text content using the Gemini API based on a prompt.
     * Optionally configures a system instruction.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or using default template value.")
            return@withContext "ত্রুটি: অনুগ্রহ করে AI Studio-র 'Secrets' প্যানেলে আপনার GEMINI_API_KEY যুক্ত করুন। এটি ছাড়া AI অ্যাসিস্ট্যান্ট সচল হবে না।"
        }

        try {
            // Build request JSON
            val root = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // System Instruction
            if (!systemInstruction.isNullOrEmpty()) {
                val sysInstructObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstructObj.put("parts", sysPartsArray)
                root.put("systemInstruction", sysInstructObj)
            }

            val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "ত্রুটি: সার্ভার রেসপন্স কোড ${response.code}"
                }
                val responseString = response.body?.string() ?: ""
                val responseJson = JSONObject(responseString)
                val candidates = responseJson.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
                "ত্রুটি: AI কোনো উত্তর জেনারেট করতে পারেনি।"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content", e)
            "ত্রুটি: ${e.message}"
        }
    }

    /**
     * Special helper for voice screen to parse speech to tasks.
     * It transforms the raw transcribed speech of a senior lawyer into organized actionable list items.
     */
    suspend fun parseSeniorVoiceInstructions(voiceText: String): List<String> {
        val systemPrompt = """
            You are a helpful legal assistant for an apprentice advocate in Bangladesh. 
            Your task is to take a raw voice transcription from a senior lawyer (which is spoken fast in Bengali) and extract actionable To-Do tasks ("করণীয় কাজ").
            Return the output strictly as a JSON array of strings, where each string is a single clear action item in beautiful, formal Bengali. 
            Do not include any markdown format (like ```json) in the response, return only the raw JSON array of strings e.g. ["কাজ ১", "কাজ ২"].
            Example input: "রহিম সাহেবের জামিন শুনানির জন্য ওকালতনামা রেডি করবা আর ল্যান্ড সার্ভে ট্রাইব্যুনালে একটা টাইম পিটিশন দিও কালকে"
            Example output: ["রহিম সাহেবের জামিন শুনানির জন্য ওকালতনামা প্রস্তুত করা", "ল্যান্ড সার্ভে ট্রাইব্যুনালে আগামীকালের জন্য সময়ের আবেদন (Time Petition) দাখিল করা"]
        """.trimIndent()

        val response = generateContent(voiceText, systemPrompt)
        return try {
            val cleanResponse = response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val jsonArray = JSONArray(cleanResponse)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            // Fallback: split by common Bengali sentence connectors or lines
            Log.e(TAG, "Failed parsing voice json, falling back to simple split: $response", e)
            response.lines()
                .map { it.trim().removePrefix("-").removePrefix("*").trim() }
                .filter { it.isNotEmpty() }
        }
    }
}


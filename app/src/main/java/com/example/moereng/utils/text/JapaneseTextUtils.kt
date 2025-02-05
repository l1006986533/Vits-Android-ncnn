package com.example.moereng.utils.text

import android.content.res.AssetManager
import android.util.Log

class JapaneseTextUtils(
    override val symbols: List<String>,
    override val cleanerName: String,
    override val assetManager: AssetManager
) : TextUtils {
    private var openJtalkInitialized = false

    private fun initDictionary(assetManager: AssetManager) {
        // init openjatlk
        if (!openJtalkInitialized) {
            openJtalkInitialized = initOpenJtalk(assetManager)
            if (!openJtalkInitialized) {
                throw RuntimeException("初始化openjtalk字典失败！")
            }
            Log.i("TextUtils", "Openjtalk字典初始化成功！")
        }
    }

    override fun cleanInputs(text: String): String {
        return text.replace("\"", "").replace("\'", "")
            .replace("\t", " ").replace("”", "")
    }

    override fun splitSentence(text: String): List<String> {
        return text.split("\n")
    }

    override fun wordsToLabels(text: String): IntArray {
        val labels = ArrayList<Int>()
        labels.add(0)

        // symbol to id
        val symbolToIndex = HashMap<String, Int>()
        symbols.forEachIndexed { index, s ->
            symbolToIndex[s] = index
        }

        // clean text
        var cleanedText = ""
        val cleaner = JapaneseCleaners()
        when{
            (cleanerName == "japanese_cleaners" || cleanerName == "japanese_cleaners1")-> {
                cleanedText = cleaner.japanese_clean_text1(text)
            }
            cleanerName == "japanese_cleaners2" -> {
                cleanedText = cleaner.japanese_clean_text2(text)
            }
        }

        // symbol to label
        for (symbol in cleanedText) {
            if (!symbols.contains(symbol.toString())) {
                continue
            }
            val label = symbolToIndex[symbol.toString()]
            if (label != null) {
                labels.add(label)
                labels.add(0)
            }
        }
        return labels.toIntArray()
    }

    override fun convertSentenceToLabels(
        text: String
    ): List<IntArray> {
        val sentences = splitSentence(text)
        val converted = ArrayList<IntArray>()

        for (sentence in sentences){
            if (sentence.length > 200){
                throw RuntimeException("句子长度不能超过200字！当前${sentence.length}字！")
            }
            if (sentence.isEmpty()) continue
            val labels = wordsToLabels(sentence)
            converted.add(labels)
        }
        return converted
    }

    override fun convertText(
        text: String
    ): List<IntArray> {
        // init dict
        initDictionary(assetManager)

        // clean inputs
        val cleanedInputs = cleanInputs(text)

        // convert inputs
        return convertSentenceToLabels(cleanedInputs)
    }

    external fun initOpenJtalk(assetManager: AssetManager): Boolean

    init {
        System.loadLibrary("moereng")
    }
}
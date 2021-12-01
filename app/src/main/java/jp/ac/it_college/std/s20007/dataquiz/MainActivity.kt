package jp.ac.it_college.std.s20007.dataquiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import jp.ac.it_college.std.s20007.dataquiz.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private val templateurl = "https://script.google.com/macros/s/AKfycbznWpk2m8q6lbLWSS6qaz3uS6j3L4zPwv7CqDEiC433YOgAdaFekGJmjoAO60quMg6l/exec?f="
    private lateinit var oldVersion: String
    private val helper = Databeas(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        oldVersion = pref.getString("version", "000000").toString()

        setContentView(binding.root)

        binding.quizButton.setOnClickListener {
            val intent = Intent(this, DataQuzi2::class.java)
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        getVersion(templateurl+"version")

    }

    @UiThread
    private fun getVersion(url: String) {
        lifecycleScope.launch {
            val result = getJson(url)
            getVersionPost(result)
        }
    }

    @UiThread
    private fun getData(url: String) {
        lifecycleScope.launch {
            val result = getJson(url)
            getDataPost(result)
        }
    }

    @WorkerThread
    private suspend fun getJson(url: String): String {
        val res = withContext(Dispatchers.IO) {
            var result = ""
            val url = URL(url)
            val con = url.openConnection() as? HttpURLConnection
            con?.let {
                try {
                    it.connectTimeout = 10000
                    it.readTimeout = 10000
                    it.requestMethod = "GET"
                    it.connect()

                    val stream = it.inputStream
                    result = extendString(stream)
                    stream.close()
                } catch(ex: SocketTimeoutException) {
                    println("通信タイムアウト")
                }
                it.disconnect()
            }
            result
        }
        return res
    }

    private fun extendString(stream: InputStream?) : String {
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        return reader.readText()
    }

    @UiThread
    private fun getVersionPost(result: String) {
        val newVersion = JSONObject(result).getString("version")
        if (oldVersion != newVersion) {
            val db = helper.writableDatabase
            val delete = """
                DELETE FROM ryota;
            """.trimIndent()
            val stmt = db.compileStatement(delete)
            stmt.executeUpdateDelete()

            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            pref.edit().putString("version", newVersion).apply()
            getData(templateurl + "data")
        }

    }

    @UiThread
    private fun getDataPost(result: String) {
        val rootData = JSONArray(result)
        val db = helper.writableDatabase
        for(i in 0 until rootData.length()) {
            val data = rootData.getJSONObject(i)
            val insert = """
                insert into ryota(_id, question, answers, choices)
                values (?, ?, ?, ?)
            """.trimIndent()

            val stmt = db.compileStatement(insert)
            stmt.bindLong(1, data.getLong("id"))
            stmt.bindString(2, data.getString("question"))
            stmt.bindLong(3, data.getLong("answers"))
            stmt.bindString(4, data.getString("choices"))
            stmt.executeInsert()

        }
    }
}
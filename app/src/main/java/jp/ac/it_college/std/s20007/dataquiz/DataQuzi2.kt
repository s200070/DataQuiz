package jp.ac.it_college.std.s20007.dataquiz

import android.annotation.SuppressLint
import android.content.Intent
import android.database.DatabaseUtils
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.ThemedSpinnerAdapter
import androidx.core.os.postDelayed
import jp.ac.it_college.std.s20007.dataquiz.databinding.ActivityDataQuzi2Binding
import org.json.JSONArray

class DataQuzi2 : AppCompatActivity() {
    private lateinit var binding: ActivityDataQuzi2Binding
    private var alldata = arrayListOf<List<Any>>()

    private val helper = Databeas(this)
    private var i = 0
    private var score = 0
    private var totaltime = 0L
    private var answer = mutableListOf<Int>()
    private var useranser = mutableListOf<Int>()

    private val timer = TimeLefttCountdown(10L * 1000, 100L)
    private var nowtime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataQuzi2Binding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.choice.setOnClickListener{ click(0)}
        binding.choice1.setOnClickListener{ click(1)}
        binding.choice2.setOnClickListener{ click(2)}
        binding.choice3.setOnClickListener{ click(3)}
        binding.choice4.setOnClickListener{ click(4)}
        binding.choice5.setOnClickListener{ click(5)}

        binding.okButton.setOnClickListener {
            result()
        }
    }

    fun click(n: Int) {
        if (n in useranser){
            useranser.remove(n)
            toParple(n)
        }
        else {
            useranser.add(n)
            toGray(n)
        }

    }

    fun toGray(n : Int) {
        when(n) {
            0 -> binding.choice.setBackgroundColor(Color.rgb(200, 200, 200))
            1 -> binding.choice1.setBackgroundColor(Color.rgb(200, 200, 200))
            2 -> binding.choice2.setBackgroundColor(Color.rgb(200, 200, 200))
            3 -> binding.choice3.setBackgroundColor(Color.rgb(200, 200, 200))
            4 -> binding.choice4.setBackgroundColor(Color.rgb(200, 200, 200))
            5 -> binding.choice5.setBackgroundColor(Color.rgb(200, 200, 200))
        }
    }

    fun toParple(n: Int) {
        when(n) {
            0 -> binding.choice.setBackgroundColor(Color.rgb(150, 100, 150))
            1 -> binding.choice1.setBackgroundColor(Color.rgb(150, 100, 150))
            2 -> binding.choice2.setBackgroundColor(Color.rgb(150, 100, 150))
            3 -> binding.choice3.setBackgroundColor(Color.rgb(150, 100, 150))
            4 -> binding.choice4.setBackgroundColor(Color.rgb(150, 100, 150))
            5 -> binding.choice5.setBackgroundColor(Color.rgb(150, 100, 150))
        }
    }

    @SuppressLint("Range")
    override fun onResume() {
        super.onResume()

        val db = helper.readableDatabase
        val select = """
            select * from ryota
            where _id = ?
        """.trimIndent()
        val dbsize = DatabaseUtils.queryNumEntries(db,"ryota").toInt()
        val randonint = (1 .. dbsize).toList().shuffled()

        for (i in 0 until 10) {
            val ranid = (1000 + randonint[i]).toString()
            val cursor = db.rawQuery(select, arrayOf(ranid))

            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex("_id"))
                val question = cursor.getString(cursor.getColumnIndex("question"))
                val answers = cursor.getLong(cursor.getColumnIndex("answers")).toInt()
                val choices = mutableListOf<String>()
                val temp = JSONArray(cursor.getString(cursor.getColumnIndex("choices")))

                for (j in 0 until temp.length()) {
                    if (temp.getString(j) != "") {
                        choices.add(temp.getString(j))
                    }
                }
                alldata.add(listOf(id, question, answers, choices))
            }
            cursor.close()

        }
        print()
    }

    inner class TimeLefttCountdown(minPast: Long, countInterval: Long): CountDownTimer(minPast, countInterval) {
        override fun onTick(millisUntilFinished: Long) {
            binding.timeLeftBar.progress = (millisUntilFinished / 100).toInt()
            nowtime = 10000 - millisUntilFinished
        }

        override fun onFinish() {
            nowtime = 10000L
            result()
        }
    }

    fun result(){

        timer.cancel()

        totaltime += nowtime

        val handler = Handler(Looper.getMainLooper())
        if (useranser.sorted() == answer.sorted()) {
            ++score
            binding.maruImagView.setImageResource(R.drawable.maru)
            binding.maruImagView.visibility = View.VISIBLE
        }else{
            binding.maruImagView.setImageResource(R.drawable.batu)
            binding.maruImagView.visibility = View.VISIBLE
        }
        handler.postDelayed(2000L) {
            moveToNext()
        }
    }

    fun print() {

        timer.start()

        useranser = mutableListOf()
        answer = mutableListOf()

        for (j in 0 until 6) {
            toParple(j)
        }

        binding.maruImagView.visibility = View.GONE

        binding.idView.text = alldata[i][0].toString()
        binding.questionView.text = alldata[i][1].toString()

        val temp = alldata[i][3] as List<*>
        val choices = temp.shuffled()

        for(j in 0 until alldata[i][2] as Int) {
            answer.add(choices.indexOf(temp[j]))
        }
        binding.choice.text = choices[0].toString()
        binding.choice1.text = choices[1].toString()
        binding.choice2.text = choices[2].toString()
        binding.choice3.text = choices[3].toString()

        when(choices.size) {
            4 -> {
                binding.choice4.visibility = View.GONE
                binding.choice5.visibility = View.GONE
            }
            5 -> {
                binding.choice4.visibility = View.VISIBLE
                binding.choice4.text = alldata[4].toString()
                binding.choice5.visibility = View.GONE

            }
            else -> {
                binding.choice4.visibility = View.VISIBLE
                binding.choice5.visibility = View.VISIBLE
                binding.choice4.text = choices[4].toString()
                binding.choice5.text = choices[5].toString()

            }
        }
    }

    fun moveToNext() {
        ++i
        if (i >= 10) {
            val intent = Intent(this, DataQuiz3::class.java)
            intent.apply{
                putExtra("score", score)
                putExtra("time", totaltime)
            }
            startActivity(intent)
        }else{
            print()
        }
    }
    override fun onKeyDown(keyCode: Int, ecent: KeyEvent?): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        return true
    }
}
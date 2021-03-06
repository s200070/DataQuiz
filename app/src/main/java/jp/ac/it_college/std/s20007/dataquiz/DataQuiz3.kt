package jp.ac.it_college.std.s20007.dataquiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import jp.ac.it_college.std.s20007.dataquiz.databinding.ActivityDataQuiz3Binding
import java.math.BigInteger

class DataQuiz3 : AppCompatActivity() {
    private lateinit var binding: ActivityDataQuiz3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataQuiz3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("score", 0)
        val time = intent.getLongExtra("time", 0L)

        val nin = time / 1000L / 60L
        val sec = time / 1000L % 60000

        binding.scoreView.text = "${score}点"
        binding.timeView.text = "${nin}分${sec}秒"

        binding.backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, ecent: KeyEvent?): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        return true
    }
}
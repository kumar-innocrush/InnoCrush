package com.innocrush.laser.views

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innocrush.laser.adapter.HistoryAdapter
import com.innocrush.laser.databinding.ActivityHistoryBinding
import com.innocrush.laser.datamodel.LaserData
import com.innocrush.laser.persistence.AppDatabase
import com.innocrush.laser.persistence.LaserDataDAO
import com.innocrush.laser.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class History  : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val TAG = "History"
    private lateinit var historyMain: MainActivityViewModel
    private lateinit var laserAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        historyMain = ViewModelProvider(this)[MainActivityViewModel::class.java]
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerHistory.context, 1
        )
        binding.recyclerHistory.addItemDecoration(dividerItemDecoration)
        readData()
    }

    private fun readData() {
        CoroutineScope(Dispatchers.IO).launch {
            val db: LaserDataDAO = AppDatabase.getInstance(this@History)?.laserDataDao()!!
            val data:List<LaserData> = db.getAllData()
            laserAdapter = HistoryAdapter(data)
            binding.recyclerHistory.adapter = laserAdapter
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
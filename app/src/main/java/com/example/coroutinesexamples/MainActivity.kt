package com.example.coroutinesexamples

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class MainActivity : BaseActivity(), View.OnClickListener {

    companion object {
        val TAG = MainActivity.javaClass.name
    }

    private lateinit var _progressBar: ProgressBar
    private lateinit var _btn_sample: Button
    private lateinit var _btn_timeout: Button
    private lateinit var _btn_sequential: Button
    private lateinit var _btn_parallel: Button
    private lateinit var _btn_exception_handler: Button
    private lateinit var _btn_cancel: Button
    private lateinit var _btn_runblocking: Button

    //var related to coroutines
    private lateinit var job: Job
    private lateinit var jobTimeout: Job
    private lateinit var jobSequential: Job
    private lateinit var jobParallel: Job
    private lateinit var jobExceptionHandler: Job
    private lateinit var jobLoadCancelTaskIfRunning: Job

    private var numberList = listOf(2, 8, 9, 7, 5, 66, 77, 123)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Init()
        OnClick()

        // Init all the jobs
        job = Job()
        jobTimeout = Job()
        jobSequential = Job()
        jobParallel = Job()
        jobExceptionHandler = Job()
        jobLoadCancelTaskIfRunning = Job()
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        jobTimeout.cancel()
        jobSequential.cancel()
        jobParallel.cancel()
        jobExceptionHandler.cancel()
        jobLoadCancelTaskIfRunning.cancel()
    }

    override fun getLayout(): Int {
        return R.layout.activity_main;
    }

    @InternalCoroutinesApi
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_sample -> {
                LoadData()
            }
            R.id.btn_timeout -> {
                LoadDataWithTimeOut()
            }
            R.id.btn_sequential -> {
                LoadSequentialData();
            }
            R.id.btn_parallel -> {
                LoadParallelTask();
            }
            R.id.btn_exception_handler -> {
                LoadCoroutineExceptionHandler()
            }
            R.id.btn_cancel -> {
                if (jobLoadCancelTaskIfRunning.isActive) {
                    jobLoadCancelTaskIfRunning.cancel()
                    jobLoadCancelTaskIfRunning = job
                }
                LoadCancelTaskIfRunning()
            }
            R.id.btn_runblocking -> {
                RunBlockingExample()
            }
        }
    }

    private fun Init() {
        _progressBar = findViewById(R.id.progress_bar)
        _btn_sample = findViewById(R.id.btn_sample)
        _btn_timeout = findViewById(R.id.btn_timeout)
        _btn_sequential = findViewById(R.id.btn_sequential)
        _btn_parallel = findViewById(R.id.btn_parallel)
        _btn_exception_handler = findViewById(R.id.btn_exception_handler)
        _btn_cancel = findViewById(R.id.btn_cancel)
        _btn_runblocking = findViewById(R.id.btn_runblocking)
    }

    private fun OnClick() {
        _btn_sample.setOnClickListener(this)
        _btn_timeout.setOnClickListener(this)
        _btn_sequential.setOnClickListener(this)
        _btn_parallel.setOnClickListener(this)
        _btn_exception_handler.setOnClickListener(this)
        _btn_cancel.setOnClickListener(this)
        _btn_runblocking.setOnClickListener(this)
    }

    /*Methods starts in Main thread and for delayoutput alone excecute in IO thread and again ShowHideProgressBar run in main thread*/
    private fun LoadData() = GlobalScope.launch(Dispatchers.Main + job)
    {
        ShowHideProgressBar(true);

        val value = Delayoutput() // run in the IO

        ShowHideProgressBar(false);

        ShowShortOrLongToast(value, false)
    }

    /*After particualr timeout job will be closed*/
    private fun LoadDataWithTimeOut() = GlobalScope.launch(Dispatchers.Main + jobTimeout) {

        ShowHideProgressBar(true);

        val result = withTimeoutOrNull(1000) { Delayoutput() }

        ShowHideProgressBar(false);

        ShowShortOrLongToast(result ?: "Timeout", false)
    }

    /*Load both the task in parallel by usings the async and results using the await*/
    private fun LoadParallelTask() = GlobalScope.launch(Dispatchers.Main + jobParallel) {
        ShowHideProgressBar(true)
        val firstParallel = async { Delayoutput("parallel one") }
        val secondParallel = async { Delayoutput("parallel two") }

        Log.v(TAG, " LoadParallelTask firstParallel : ${firstParallel.await()}")
        Log.v(TAG, " LoadParallelTask secondParallel : ${secondParallel.await()}")
        ShowHideProgressBar(false)
    }

    /*Cancels the job and create another job*/
    private fun LoadCancelTaskIfRunning() =
        GlobalScope.launch(Dispatchers.Main + jobLoadCancelTaskIfRunning) {
            ShowHideProgressBar(true)
            val result = SortNumberList(false)
            Log.v(TAG, " LoadCancelTaskIfRunning: " + result.toString())
            //test join jobs
            jobLoadCancelTaskIfRunning.invokeOnCompletion {
                Log.v(TAG, " LoadCancelTaskIfRunning: job completed")
            }
            LoadData()
            jobLoadCancelTaskIfRunning.join()
            launch {
                LoadDataWithTimeOut()
            }
            ShowHideProgressBar(false)
        }

    /*Runs the task both in sequential order*/
    private fun LoadSequentialData() = GlobalScope.launch(Dispatchers.Main + jobSequential) {
        ShowHideProgressBar(true);

        val requltOne = Delayoutput("sequential one")
        Log.v(TAG, " LoadSequentialData  requltOne : $requltOne")
        val requltTwo = Delayoutput("sequential two")
        Log.v(TAG, " LoadSequentialData requltTwo : $requltTwo")

        Log.v(TAG, " LoadSequentialData: $requltOne  $requltTwo")
        ShowHideProgressBar(false);
    }

    /*It pauses all the task and resume the paused task after completing the runblocking task*/
    private fun RunBlockingExample() {
        LoadData()
        LoadDataWithTimeOut()
        runBlocking {
            /*here it pause all above called coroutines task and strat excecuting the LoadSequesntial job and after completion of the runblocking above coroutines will be resumed*/
            print("runBlocking : blocks all the above tasks and runblocking starts excecuting")
        }
    }


    @InternalCoroutinesApi
    private fun LoadCoroutineExceptionHandler() {

        var parentJobForException = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            ShowCurrentThread()
            var jobOne = launch {
                var resultOne = getNumbers(false)
                Log.v(TAG, " LoadCoroutineExceptionHandler jobOne $resultOne")
            }

            jobOne.invokeOnCompletion { it ->
                if (it != null) {
                    Log.v(TAG, " LoadCoroutineExceptionHandler Error in jobOne ")
                }
            }

            var jobTwo = launch {
                var resultTwo = getNumbers(false)
                Log.v(TAG, " LoadCoroutineExceptionHandler jobtwo $resultTwo")
            }

            jobTwo.invokeOnCompletion { it ->
                if (it != null) {
                    Log.v(TAG, " LoadCoroutineExceptionHandler Error in jobTwo ")
                }
            }

            var jobThree = launch {
                var resultTwo = getNumbers(true)
                Log.v(TAG, " LoadCoroutineExceptionHandler jobthree $resultTwo")
            }

            jobThree.invokeOnCompletion { it ->
                if (it != null) {
                    Log.v(TAG, " LoadCoroutineExceptionHandler Error in jobThree ")
                }
            }
        }

        parentJobForException.invokeOnCompletion { it ->
            if (it != null) {
                Log.v(TAG, " LoadCoroutineExceptionHandler Error in parentJobForException ")
            } else {
                Log.v(
                    TAG,
                    " LoadCoroutineExceptionHandler parentJobForException completed successfully"
                )
            }
            ShowCurrentThread()
        }

    }

    private suspend fun Delayoutput(value: String = "sample"): String =
        withContext(Dispatchers.IO) {

            Log.v(TAG, "Delayoutput value: " + value)
            delay(5000)

            "After Delay of 5000"
        }

    private fun ShowCurrentThread() {
        Log.v(TAG, "Current Thread: ${Thread.currentThread()}")
    }

    @InternalCoroutinesApi
    private suspend fun getNumbers(isException: Boolean): Int {
        if (isException) {
            /*this will stop all the other job inside the parent job*/
            throw Exception("Get Number Exception")
            /*by using cancel it wont stop all other jobs inside the parent job*/
            //cancel(CancellationException("JOb cancelled exception"))
            //throw CancellationException("JOb cancelled exception")
        }
        return Random(Int.MAX_VALUE).nextInt()
    }

    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { a, throwable ->
            runOnUiThread {
                ShowShortOrLongToast("${throwable.message}")
                ShowHideProgressBar(false)
                ShowCurrentThread()
            }
            // init job after cancel the job for next iterations
            jobExceptionHandler = Job()
        }

    private suspend fun SortNumberList(isAscending: Boolean): List<Int> =
        withContext(Dispatchers.Default) {
            var resturnList: List<Int>?
            if (isAscending) {
                resturnList = numberList.sortedDescending()
                resturnList.reversed()
            } else {
                resturnList = numberList.sortedDescending()
                resturnList
            }
        }

    private fun ShowHideProgressBar(boolean: Boolean) {
        if (boolean) {
            _progressBar.visibility = View.VISIBLE;
        } else {
            _progressBar.visibility = View.GONE;
        }
    }
}
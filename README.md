# CoroutinesKotlin
 
 This project Give a basic Understanding about the Kotlin Coroutines with the points below
 
 1. Kotlin coroutines is like a light weight thread
 
 2. It is used to manage the concurrency of the threads. It is a framework written on top of the threadings 
 
 3. Like threads, coroutines can run in parallel, wait for each other and communicate.
 
 4. Only suspend method can me called inside the coroutines or another suspend function.
 
 5. Suspend function is a function that could be started, paused and resume.
 
 6. There are 3 Dispatchers for making the functions write in Main and Background thread 
      * Dispatchers.Main - will run in the main thread.
			* Dispatchers.IO - used to run the api calls and database access.
			* Dispatchers.Default - used to run only the very heavy tasks like parsing the json and sorting the lists
      
 7. There are 2 ways to launch the coroutines they are launch and asyn. Main difference is launch wont return any thing and async will return Deffered<T> and have await function.
 
 8. withContext is nothing but an another way writing the async where we do not have to write await().
 
 9. Globalscope will have the scope over the application even after the activity is being destroyed. 
 
 10. Exception handling example while using the launch
        val handler = CoroutineExceptionHandler { _, exception ->
    Log.d(TAG, "$exception handled !")
}
Then, we can attach the handler like below:
GlobalScope.launch(Dispatchers.IO + handler) {
    fetchUserAndSaveInDatabase() // do on IO thread
}


  11. Exception handling while using the async can be done through try catch
  
  12. If coroutine is inside the another coroutine when the parent job is canceled it all child will also be canceled 
  
  13. if we dont want to cancel the child jobs inside the patrent coroutine jobs then make request.join
  
  14. difference between the coroutine scope and supervisor scope is if any of the child calls fails in the coroutine scope it stops all other child calls but supervisor scope wont fails
  for this situation. If both child calls not fails these scopeswill wait for all the calls to be completed.
  
  15.There is also viewmodelscope which cancels when the viewmodel is destroyed.
  
  16. suspend(pause) function coroutine will suspend the work when work starts and resume coroutines when all the work is being completed.
  
  17. Difference between the coroutinescope and withcontext is from withcontext it is easily change the context other that both are of similar only 
  
  18. Runblocking : if many coroutines tasks are being running by using the runblocking it blocks all its tasks and executes runblocking the task inside them. It alos works with the coroutine scope.
 

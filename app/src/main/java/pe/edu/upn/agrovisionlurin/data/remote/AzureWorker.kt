package pe.edu.upn.agrovisionlurin.data.remote
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Remote sync was removed from the approved local MVP. Old queued jobs must never mark records uploaded. */
class AzureWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = Result.failure()
}

package pe.edu.upn.agrovisionlurin.data.local.ml

import android.content.Context
import pe.edu.upn.agrovisionlurin.domain.repository.LeafClassifier

/** Compatibility entry point for prior screens, with the strict lemon contract and no fallback. */
class TFLiteLeafClassifier private constructor(context: Context) : LeafClassifier by LemonClassifier(context) {
    companion object {
        @Volatile private var instance: TFLiteLeafClassifier? = null
        fun getInstance(context: Context): TFLiteLeafClassifier = instance ?: synchronized(this) {
            instance ?: TFLiteLeafClassifier(context.applicationContext).also { instance = it }
        }
    }
}


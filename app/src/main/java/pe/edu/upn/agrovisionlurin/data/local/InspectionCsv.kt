package pe.edu.upn.agrovisionlurin.data.local

object InspectionCsv {
    fun encode(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
    fun render(record: InspectionEntity): String {
        val headers = listOf("id","fecha_epoch_ms","parcela","nota_ubicacion","usuario","estado","clase_codigo","clase_etiqueta","confianza","latencia_ms","modelo","imagen_local","recomendaciones","error","dominio_historico_clase","dominio_historico_severidad")
        val fields = listOf(record.id,record.createdAt.toString(),record.plotCodeSnapshot,record.subPlot,record.userNameSnapshot,
            record.state,record.classCode,record.classLabel,record.confidence.toString(),record.latencyMs.toString(),
            record.modelVersion,record.imagePath,record.recommendationsJson,record.errorMessage,
            if(record.state=="LEGADO") record.diagnosisClass.toString() else "",
            if(record.state=="LEGADO") record.severityLevel else "")
        // Quote CSV and neutralize spreadsheet formula prefixes in user-controlled cells.
        return "\uFEFF" + headers.joinToString(",") + "\r\n" + fields.joinToString(",") {
            encode(if(it.trimStart().firstOrNull() in listOf('=','+','-','@')) "'$it" else it)
        } + "\r\n"
    }
}

package pe.edu.upn.agrovisionlurin.data.local

import org.junit.Assert.*
import org.junit.Test

class LocalUtilitiesTest {
    @Test fun saltedPasswordHashesVerifyAndRejectWrongPassword() {
        val a=PasswordHash.salt();val b=PasswordHash.salt()
        assertNotEquals(a,b)
        val hash=PasswordHash.hash("correct-password",a)
        assertTrue(PasswordHash.verify("correct-password",a,hash))
        assertFalse(PasswordHash.verify("wrong-password",a,hash))
        assertFalse(PasswordHash.verify("correct-password",b,hash))
        assertFalse(PasswordHash.verify("correct-password","",""))
    }
    @Test fun csvEscapesQuotesNewlinesAndSpreadsheetFormulas() {
        val record=InspectionEntity("A","plot","","/images/A.jpg",0,0.91f,15,"","[\"Observe\"]",0,123,
            state="VALIDO",classCode="MINADOR_COMPATIBLE",classLabel="Daño compatible con minador",modelVersion="v1",
            plotCodeSnapshot="=SUM(1,2)",userNameSnapshot="Ana, \"Campo\"\nNorte")
        val csv=InspectionCsv.render(record)
        assertTrue(csv.contains("\"'=SUM(1,2)\""))
        assertTrue(csv.contains("\"Ana, \"\"Campo\"\"\nNorte\""))
        assertTrue(csv.contains("\"A\""))
        assertTrue(csv.contains("\"MINADOR_COMPATIBLE\""))
        assertTrue(csv.contains("\"v1\""))
        assertFalse(csv.contains("Azure"))
    }
}

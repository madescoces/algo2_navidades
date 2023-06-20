
import java.time.LocalDate
import java.time.DayOfWeek

const val VALOR_MINIMO = 5000.0
val MARCAS_VALIOSAS = listOf("Jordache","Lee","Charro","Motor Oil")
const val VALOR_VOUCHER_APP = 2000.0
const val FECHA_JUGUETE_COLECCIONABLE = 2000

class Papapp{
    val regalos = mutableListOf<Regalo>()
    val personas = mutableListOf<Persona>()
    fun personasSinRegalo() = personas.filter { p -> p.noTengoRegaloAsignado() }

    fun addRegalo(regalo: Regalo){
        regalos.add(regalo)
    }

    fun addPersona(persona: Persona){
        personas.add(persona)
    }

    fun ejecutar(procesos: List<Proceso>) {
        procesos.forEach{ p -> p.execute(this)}
    }
}

interface Proceso {
    fun execute(app:Papapp)
}

class AsignarRegalos: Proceso{
    override fun execute(app: Papapp) {
        app.personasSinRegalo().forEach { p -> p.asignarRegalo(app.regalos) }
    }
}

class RegaloConsuelo: Proceso{
    override fun execute(app: Papapp) {
        app.personasSinRegalo().forEach{ p -> p.asignarRegalo(mutableListOf(VoucherSAPP()))}
    }
}

class DespacharRegalo: Proceso{

    override fun execute(app: Papapp) {

    }
}

abstract class Regalo(val valor: Double) {
    open lateinit var marca: String
    fun esCaro(): Boolean = valor > VALOR_MINIMO
    open fun esValioso(): Boolean = esCaro() && condicionSerValioso()
    abstract fun condicionSerValioso(): Boolean
}

class Ropa(valor: Double) : Regalo(valor){
    override fun condicionSerValioso() = MARCAS_VALIOSAS.contains(marca)
}

class Juguete(valor: Double, private val fecha: LocalDate) : Regalo(valor){
    override fun condicionSerValioso() = fecha.year < FECHA_JUGUETE_COLECCIONABLE
}

class Perfume(valor: Double, private val origenExtranjero: Boolean) : Regalo(valor){
    override fun condicionSerValioso() = origenExtranjero
}

class Experiencia(valor: Double, private val fecha: LocalDate) : Regalo(valor){
    override fun condicionSerValioso() = fecha.dayOfWeek == DayOfWeek.FRIDAY
}

class VoucherSAPP:Regalo(VALOR_VOUCHER_APP){
    override var marca = "Papapp"
    override fun condicionSerValioso() = false
}

class Persona(private var preferencia: Preferencia){
    private lateinit var regaloAsignado:Regalo
    private fun aceptaRegalo(regalo: Regalo) = preferencia.puedeAceptarRegalo(regalo)

    fun asignarRegalo(regalos: MutableList<Regalo>){
        regaloAsignado = regalos.find{ this.aceptaRegalo(it)}!!
    }

    fun noTengoRegaloAsignado() = !::regaloAsignado.isInitialized
}

interface Preferencia {
    fun puedeAceptarRegalo(regalo:Regalo): Boolean
}

class Conformista: Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = true
}

class Interesada(): Preferencia{
    var minimo = VALOR_MINIMO
    override fun puedeAceptarRegalo(regalo: Regalo) = minimo < regalo.valor
    fun cambiarMinimo(valor:Double){
        minimo = valor
    }
}

class Exigentes: Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = regalo.esValioso()
}

class Marqueras(private var marcaPreferencia: String): Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = regalo.marca == marcaPreferencia
}

class Combinetas(private val preferencias: MutableList<Preferencia>): Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = preferencias.any{ preferencia -> preferencia.puedeAceptarRegalo(regalo) }

    fun addPreferencia(preferencia:Preferencia){
        preferencias.add(preferencia)
    }
    fun removePreferencia(preferencia:Preferencia){
        preferencias.remove(preferencia)
    }
}


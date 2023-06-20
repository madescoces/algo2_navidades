
import java.time.LocalDate
import java.time.DayOfWeek

const val VALOR_MINIMO = 5000.0
val MARCAS_VALIOSAS = listOf("Jordache","Lee","Charro","Motor Oil")
const val VALOR_VOUCHER_APP = 2000.0


class Papapp{
    val regalos = mutableListOf<Regalo>()
    val personas = mutableListOf<Persona>()
    fun personasSinRegalo() = personas.filter { p -> p.noTengoRegalo() }
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
class RegistrarEntrega: Proceso{
    override fun execute(app: Papapp) {

    }
}

abstract class Regalo(val marca:String, val valor: Double) {
    fun esCaro(): Boolean = valor > VALOR_MINIMO
    open fun esValioso(): Boolean = esCaro() && condicionSerValioso()
    abstract fun condicionSerValioso(): Boolean
}

class Ropa(marca: String, valor: Double) : Regalo(marca, valor){
    override fun condicionSerValioso() = MARCAS_VALIOSAS.contains(marca)
}

class Juguete(marca: String, valor: Double, private val fecha: LocalDate) : Regalo(marca, valor){
    override fun condicionSerValioso() = fecha.year < 2000
}

class Perfume(marca: String, valor: Double, private val origenExtranjero: Boolean) : Regalo(marca, valor){
    override fun condicionSerValioso() = origenExtranjero
}

class Experiencia(marca: String, valor: Double, private val fecha: LocalDate) : Regalo(marca, valor){
    override fun condicionSerValioso() = fecha.dayOfWeek == DayOfWeek.FRIDAY
}

class VoucherSAPP():Regalo("Papapp", VALOR_VOUCHER_APP){
    override fun condicionSerValioso(): Boolean = false
}

class Persona(var preferencia: Preferencia){
    lateinit var regalo:Regalo
    fun aceptaRegalo(regalo: Regalo) = preferencia.puedeAceptarRegalo(regalo)
    fun asignarRegalo(regalos: MutableList<Regalo>){
        regalo = regalos.find{ this.aceptaRegalo(it)}!!
    }
    fun noTengoRegalo() = !::regalo.isInitialized
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
class Marqueras(var marcaPreferencia: String): Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = regalo.marca == marcaPreferencia
}
class Combinetas(val preferencias: MutableList<Preferencia>): Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = preferencias.any{ preferencia -> preferencia.puedeAceptarRegalo(regalo)  }
    fun addPreferencia(preferencia:Preferencia){
        preferencias.add(preferencia)
    }
    fun removePreferencia(preferencia:Preferencia){
        preferencias.remove(preferencia)
    }
}


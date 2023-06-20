
import java.time.LocalDate
import java.time.DayOfWeek
import kotlin.properties.Delegates

const val VALOR_MINIMO = 5000.0
val MARCAS_VALIOSAS = listOf("Jordache","Lee","Charro","Motor Oil")
const val VALOR_VOUCHER_APP = 2000.0
const val FECHA_JUGUETE_COLECCIONABLE = 2000
const val VALOR_NO_TODO_SE_PUEDE = 10000

class Papapp{
    val regalos = mutableListOf<Regalo>()
    private val personas = mutableListOf<Persona>()
    var personasConRegaloAsignado = mutableMapOf<Persona, Regalo>()
    private var lastId = 0

    fun personasSinRegalo() = personas.filterNot { personasConRegaloAsignado.containsKey(it) }

    fun addRegalo(regalo: Regalo){
        regalo.id = lastId++
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
        app.personasSinRegalo().forEach{ app.personasConRegaloAsignado[it] = it.elegirRegalo(app.regalos)}
    }
}

class AsignarRegaloConsuelo: Proceso{
    override fun execute(app: Papapp) {
        app.personasSinRegalo().forEach{ app.personasConRegaloAsignado[it] = VoucherSAPP() }
    }
}

class EntregarRegalo: Proceso{
    private val listeners = mutableSetOf<EntregaListener>()

    override fun execute(app: Papapp) {
        app.personasConRegaloAsignado.forEach{ enviarA(it.key, it.value) }
    }

    private fun enviarA(persona: Persona, regalo: Regalo){
        persona.recibirRegalo(regalo)
        listeners.forEach { it.ejecutarListener(persona, regalo) }
    }

    fun addListener(listener:EntregaListener){
        listeners.add(listener)
    }

    fun removeListener(listener:EntregaListener){
        listeners.remove(listener)
    }
}

interface EntregaListener{
    fun ejecutarListener(persona: Persona, regalo: Regalo)
}

class EnviarMailPedido(private var mailSender:MailSender): EntregaListener{
    private val sender = "santa@polo.nt"
    override fun ejecutarListener(persona: Persona, regalo: Regalo) {
        val mail = Mail(
            from = sender,
            to = persona.email,
            subject = "Tu regalo ${regalo.javaClass.simpleName} marca ${regalo.marca} esta en camino!",
            content = "Feliz navidad ${initcap(persona.nombre)}, en breve te llegará tu regalo"
        )
        mailSender.sendMail(mail)
    }
}

class InformarEmpresaEnvio(private var transporte: Transportista): EntregaListener{
    override fun ejecutarListener(persona: Persona, regalo: Regalo) {
        val envio = Envio(
            direccion = persona.direccion,
            nombre = persona.nombre,
            dni = persona.dni,
            idRegalo = regalo.id
        )
        transporte.enviar(envio)
    }
}

class RegaloCaroAfectaPersona: EntregaListener{
    override fun ejecutarListener(persona: Persona, regalo: Regalo) {
        if (regalo.valor > VALOR_NO_TODO_SE_PUEDE){
            persona.preferencia = Interesada(5000.0)
        }
    }
}

abstract class Regalo(val valor: Double) {
    open lateinit var marca: String
    open var id by Delegates.notNull<Int>()
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

data class Persona(
    var preferencia: Preferencia,
    var email:String,
    val nombre:String,
    var direccion: String,
    val dni:Int,
){
    private lateinit var regalo:Regalo

    private fun aceptaRegalo(regalo: Regalo) = preferencia.puedeAceptarRegalo(regalo)

    fun elegirRegalo(regalos: MutableList<Regalo>) = regalos.find{ this.aceptaRegalo(it) }!!

    fun recibirRegalo(regaloRecibido: Regalo){
        regalo = regaloRecibido
    }
}

interface Preferencia {
    fun puedeAceptarRegalo(regalo:Regalo): Boolean
}

class Conformista: Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = true
}

class Interesada(private var minimo: Double = VALOR_MINIMO): Preferencia{
    override fun puedeAceptarRegalo(regalo: Regalo) = minimo < regalo.valor
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

interface MailSender{
    fun sendMail(mail:Mail){}
}

data class Mail(
    val from:String,
    val to:String,
    val subject:String,
    val content:String
)

class Transportista(nombre: String){
    //TODO: En el futuro alguien implementará que sucede cuando se realiza un envio...
    fun enviar(envio:Envio){}
}

data class Envio(
    val direccion:String,
    val nombre:String,
    val dni:Int,
    val idRegalo:Int
)

fun initcap(str:String) = str.replaceFirstChar { it.uppercase() }


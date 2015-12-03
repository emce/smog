package mobi.cwiklinski.bloodline.ui.extension

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public object DelegatesExt {
    fun <T : Any> notNullSingleValue(): ReadWriteProperty<Any?, T> = NotNullSingleValueVar()
}

private class NotNullSingleValueVar<T : Any>() : ReadWriteProperty<Any?, T> {

    private var settledValue: T? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.settledValue = if (this.settledValue == null) value
        else throw IllegalStateException("${property.name} already initialized")
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return settledValue ?: throw IllegalStateException("${property.name} not initialized")
    }
}

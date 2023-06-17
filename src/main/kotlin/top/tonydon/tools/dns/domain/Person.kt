package top.tonydon.tools.dns.domain

import javafx.beans.property.SimpleStringProperty

class Person(name: String?, email: String?) {
    private val nameSP: SimpleStringProperty
    private val emailSP: SimpleStringProperty

    init {
        this.nameSP = SimpleStringProperty(name)
        this.emailSP = SimpleStringProperty(email)
    }

    fun setName(name: String?) {
        this.nameSP.set(name)
    }

    fun getName(): String {
        return nameSP.get()
    }
}

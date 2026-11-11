package it.univaq.grr.fitslot.data.local

import it.univaq.grr.fitslot.domain.exceptions.KeyAlreadyExistsException
import it.univaq.grr.fitslot.domain.exceptions.KeyNotFoundException

object LocalStorage {

    private val storage = mutableMapOf<String, Any?>()

    fun addLocalStorage(key: String, unit: Any?){
        if(storage.containsKey(key)) throw KeyAlreadyExistsException(key)
        storage[key] = unit
    }

    fun deleteFromKey(key: String) {
        if(!storage.containsKey(key)) throw KeyNotFoundException(key)
        storage.remove(key)
    }

    fun getLocalStorage(key: String): Any? {
        if (!storage.containsKey(key)) throw KeyNotFoundException(key)
        return storage[key]
    }

    fun getAll(): Map<String, Any?> {
        return storage.toMap()
    }

    fun deleteAll(){
        storage.clear()
    }

}
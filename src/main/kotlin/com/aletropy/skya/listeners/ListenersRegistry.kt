package com.aletropy.skya.listeners

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.jar.JarFile

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterListener

class ListenersRegistry(plugin : JavaPlugin)
{
    init {
        val file = plugin.javaClass.protectionDomain.codeSource.location.file
        val jar = JarFile(file)

        for(entry in jar.entries())
        {
            if(!entry.name.endsWith(".class")) continue
            val className = entry.name
                .replace("/", ".")
                .removeSuffix(".class")

            try {
                val clazz = Class.forName(className, false, plugin.javaClass.classLoader)
                if(clazz.getAnnotation(RegisterListener::class.java) != null) {
                    val listener = clazz.kotlin.constructors.first().call() as Listener
                    plugin.server.pluginManager
                        .registerEvents(listener, plugin)

                    plugin.logger.info(
                        "Registered a new event listener: ${clazz.simpleName}"
                    )
                }
            } catch (_: Throwable) {}
        }
    }
}
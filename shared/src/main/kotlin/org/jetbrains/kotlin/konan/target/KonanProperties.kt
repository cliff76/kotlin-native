/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed -> in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.konan.properties

import org.jetbrains.kotlin.konan.file.*
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.util.DependencyProcessor

interface TargetableExternalStorage {
    fun targetString(key: String): String? 
    fun targetList(key: String): List<String> 
    fun hostString(key: String): String? 
    fun hostList(key: String): List<String> 
    fun hostTargetString(key: String): String? 
    fun hostTargetList(key: String): List<String> 
    fun absolute(value: String?): String
}

interface KonanPropertyValues: TargetableExternalStorage {

    val llvmHome get() = hostString("llvmHome")

    // TODO: Delegate to a map?
    val llvmLtoNooptFlags get() = targetList("llvmLtoNooptFlags")
    val llvmLtoOptFlags get() = targetList("llvmLtoOptFlags")
    val llvmLtoFlags get() = targetList("llvmLtoFlags")
    val llvmLtoDynamicFlags get() = targetList("llvmLtoDynamicFlags")
    val entrySelector get() = targetList("entrySelector")
    val linkerOptimizationFlags get() = targetList("linkerOptimizationFlags")
    val linkerKonanFlags get() = targetList("linkerKonanFlags")
    val linkerNoDebugFlags get() = targetList("linkerNoDebugFlags")
    val linkerDynamicFlags get() = targetList("linkerDynamicFlags")
    val llvmDebugOptFlags get() = targetList("llvmDebugOptFlags")
    val s2wasmFlags get() = targetList("s2wasmFlags")

    val targetSysRoot get() = targetString("targetSysRoot")
    val libffiDir get() = targetString("libffiDir")
    val gccToolchain get() = targetString("gccToolchain")
    val targetArg get() = targetString("quadruple")
    // Notice: these ones are host-target.
    val targetToolchain get() = hostTargetString("targetToolchain")
    val dependencies get() = hostTargetList("dependencies")
    val osVersionMin: String? get() = targetString("osVersionMin")

    val absoluteTargetSysRoot get() = absolute(targetSysRoot)
    val absoluteTargetToolchain get() = absolute(targetToolchain)
    val absoluteGccToolchain get() = absolute(gccToolchain)
    val absoluteLlvmHome get() = absolute(llvmHome)
    val absoluteLibffiDir get() = absolute(libffiDir)

    val mingwWithLlvm: String?
}

class KonanProperties(val target: KonanTarget, val properties: Properties, val baseDir: String? = null): KonanPropertyValues {

    fun downloadDependencies() {
        dependencyProcessor!!.run()
    }

    override fun targetString(key: String): String? 
        = properties.targetString(key, target)
    override fun targetList(key: String): List<String> 
        = properties.targetList(key, target)
    override fun hostString(key: String): String? 
        = properties.hostString(key)
    override fun hostList(key: String): List<String> 
        = properties.hostList(key)
    override fun hostTargetString(key: String): String? 
        = properties.hostTargetString(key, target)
    override fun hostTargetList(key: String): List<String> 
        = properties.hostTargetList(key, target)
    override fun absolute(value: String?): String =
            dependencyProcessor!!.resolveRelative(value!!).absolutePath
    override val mingwWithLlvm: String?
        get() { 
            if (target != KonanTarget.MINGW) {
                error("Only mingw target can have '.mingwWithLlvm' property")
            }
            // TODO: make it a property in the konan.properties.
            // Use (equal) llvmHome fow now.
            return targetString("llvmHome")
        }

    private val dependencyProcessor = baseDir?.let { DependencyProcessor(java.io.File(it), this) }

}

/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.properties.*

// TODO: hostProperties should not be passed here.
// Need to clean up host clang support a little.
class Platform(hostProperties: Configurables, val targetConfigurables: Configurables) 
    : Configurables by targetConfigurables {

    val clang by lazy {
        ClangManager(hostProperties, targetConfigurables)
    }
    val linker by lazy {
        linker(targetConfigurables)
    }
}

class PlatformManager(properties: Properties, baseDir: String) {
    private val host = TargetManager.host
    private val enabledTargets = TargetManager.enabled
    private val hostConfigurables = loadConfigurables(host, properties, baseDir)
    private val platforms = enabledTargets.map {
        it to Platform(hostConfigurables, loadConfigurables(it, properties, baseDir))
    }.toMap()

    fun platform(target: KonanTarget) = platforms[target]!!

    val hostClang = platforms[host]!!.clang
    val hostClangArgs = hostClang.hostArgs
    val hostClangPath: List<String> = hostClangArgs.hostClangPath
    val hostCompilerArgsForJni = hostClang.hostCompilerArgsForJni
}


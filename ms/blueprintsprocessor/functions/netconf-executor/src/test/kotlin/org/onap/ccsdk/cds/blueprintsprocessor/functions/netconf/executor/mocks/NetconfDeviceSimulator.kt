/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.mocks


import org.apache.sshd.common.NamedFactory
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.UserAuth
import org.apache.sshd.server.auth.UserAuthNoneFactory
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.util.*


class NetconfDeviceSimulator(private val port: Int) {

    private var sshd: SshServer? = null

    fun start() {
        sshd = SshServer.setUpDefaultServer()
        sshd!!.port = port
        sshd!!.keyPairProvider = SimpleGeneratorHostKeyProvider()

        val userAuthFactories = ArrayList<NamedFactory<UserAuth>>()
        userAuthFactories.add(UserAuthNoneFactory())
        sshd!!.userAuthFactories = userAuthFactories

        val namedFactoryList = ArrayList<NamedFactory<Command>>()
        namedFactoryList.add(NetconfSubsystemFactory())
        sshd!!.subsystemFactories = namedFactoryList

        try {
            sshd!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun stop() {
        try {
            sshd!!.stop(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.ssh.service

import kotlinx.coroutines.runBlocking
import org.apache.sshd.common.config.keys.KeyUtils.RSA_ALGORITHM
import org.apache.sshd.common.keyprovider.KeyPairProvider
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.shell.ProcessShellCommandFactory
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BluePrintSshLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BluePrintSshLibConfiguration::class,
    BlueprintPropertyConfiguration::class, BluePrintProperties::class])
@TestPropertySource(properties =
["blueprintsprocessor.sshclient.sample.type=basic-auth",
    "blueprintsprocessor.sshclient.sample.host=localhost",
    "blueprintsprocessor.sshclient.sample.port=52815",
    "blueprintsprocessor.sshclient.sample.username=root",
    "blueprintsprocessor.sshclient.sample.password=dummyps"
])
class BlueprintSshClientServiceTest {

    @Autowired
    lateinit var bluePrintSshLibPropertyService: BluePrintSshLibPropertyService

    @Test
    fun testBasicAuthSshClientService() {
        runBlocking {
            val sshServer = setupTestServer("localhost", 52815, "root", "dummyps")
            sshServer.start()
            println(sshServer)
            val bluePrintSshLibPropertyService = bluePrintSshLibPropertyService.blueprintSshClientService("sample")
            val sshSession = bluePrintSshLibPropertyService.startSession()
            val response = bluePrintSshLibPropertyService.executeCommandsNB(arrayListOf("pwd", "ls -ltr"), 2000)
            assertNotNull(response, "failed to get command response")
            bluePrintSshLibPropertyService.closeSession()
            sshServer.stop(true)
        }
    }

    private fun setupTestServer(host: String, port: Int, userName: String, password: String): SshServer {
        val sshd = SshServer.setUpDefaultServer()
        sshd.port = port
        sshd.host = host
        sshd.keyPairProvider = createTestHostKeyProvider()
        sshd.passwordAuthenticator = BogusPasswordAuthenticator(userName, password)
        sshd.publickeyAuthenticator = AcceptAllPublickeyAuthenticator.INSTANCE
        //sshd.shellFactory = EchoShellFactory()
        sshd.commandFactory = ProcessShellCommandFactory.INSTANCE
        return sshd
    }

    private fun createTestHostKeyProvider(): KeyPairProvider {
        val keyProvider = SimpleGeneratorHostKeyProvider()
        keyProvider.path = Paths.get("target").resolve("hostkey." + RSA_ALGORITHM.toLowerCase())
        keyProvider.algorithm = RSA_ALGORITHM
        return keyProvider
    }
}

class BogusPasswordAuthenticator(userName: String, password: String) : PasswordAuthenticator {
    override fun authenticate(username: String, password: String, serverSession: ServerSession): Boolean {
        assertEquals(username, "root", "failed to match username")
        assertEquals(password, "dummyps", "failed to match password")
        return true
    }
}



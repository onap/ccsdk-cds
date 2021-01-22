/*
 *  Copyright © 2019 IBM.
 *
 *  Modifications Copyright © 2018-2019 IBM, Bell Canada
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
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BlueprintSshLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.service.echoShell.EchoShellFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BlueprintSshLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.sshclient.sample.type=basic-auth",
            "blueprintsprocessor.sshclient.sample.host=localhost",
            "blueprintsprocessor.sshclient.sample.port=52815",
            "blueprintsprocessor.sshclient.sample.username=root",
            "blueprintsprocessor.sshclient.sample.password=dummyps"
        ]
)
class BlueprintSshClientServiceTest {

    @Autowired
    lateinit var bluePrintSshLibPropertyService: BlueprintSshLibPropertyService

    lateinit var bluePrintSshLibPropertyServiceMock: BlueprintSshLibPropertyService

    private lateinit var sshServer: SshServer

    @BeforeTest
    fun startShellServer() {
        runBlocking {
            println("Start local Shell server")
            sshServer = setupTestServer("localhost", 52815, "root", "dummyps")
            sshServer.start()
            println(sshServer)
        }
    }

    @AfterTest
    fun stopShellServer() {
        println("End the Shell server")
        sshServer.stop(true)
    }

    @Test
    fun testStartSessionNB() {
        val clientSession = getSshClientService().startSession()
        assertNotNull(clientSession, "Failed to start ssh session with server")
    }

    @Test
    fun testBasicAuthSshClientService() {
        runBlocking {
            val blueprintSshClientService = getSshClientService()
            blueprintSshClientService.startSession()
            // Preparing response
            val commandResults = arrayListOf<CommandResult>()
            commandResults.add(CommandResult("echo 1", "echo 1\n#", true))
            commandResults.add(CommandResult("echo 2", "echo 1\n#echo 2\n#", true))
            val response = blueprintSshClientService.executeCommands(arrayListOf("echo 1", "echo 2"), 2000)
            blueprintSshClientService.closeSession()

            assertEquals(response, commandResults, "failed to get command responses")
        }
    }

    @Test
    fun `testBasicAuthSshClientService single execution command`() {
        runBlocking {
            val blueprintSshClientService = getSshClientService()
            blueprintSshClientService.startSession()
            val response = blueprintSshClientService.executeCommand("echo 1", 2000)
            blueprintSshClientService.closeSession()

            assertEquals(response, CommandResult("echo 1", "echo 1\n#", true), "failed to get command response")
        }
    }

    @Test
    fun testCloseSessionNB() {
        val bluePrintSshLibPropertyService = bluePrintSshLibPropertyService.blueprintSshClientService("sample")
        val clientSession = bluePrintSshLibPropertyService.startSession()
        bluePrintSshLibPropertyService.closeSession()
        assertTrue(clientSession.isClosed, "Failed to close ssh session with server")
    }

    private fun setupTestServer(host: String, port: Int, username: String, password: String): SshServer {
        val sshd = SshServer.setUpDefaultServer()
        sshd.port = port
        sshd.host = host
        sshd.keyPairProvider = createTestHostKeyProvider()
        sshd.passwordAuthenticator = BogusPasswordAuthenticator(username, password)
        sshd.publickeyAuthenticator = AcceptAllPublickeyAuthenticator.INSTANCE
        sshd.shellFactory = EchoShellFactory.INSTANCE
        sshd.commandFactory = ProcessShellCommandFactory.INSTANCE
        return sshd
    }

    private fun createTestHostKeyProvider(): KeyPairProvider {
        val keyProvider = SimpleGeneratorHostKeyProvider()
        keyProvider.path = Paths.get("target").resolve("hostkey." + RSA_ALGORITHM.toLowerCase())
        keyProvider.algorithm = RSA_ALGORITHM
        return keyProvider
    }

    private fun getSshClientService(): BlueprintSshClientService {
        return bluePrintSshLibPropertyService.blueprintSshClientService("sample")
    }
}

class BogusPasswordAuthenticator(private val usr: String, private val pwd: String) : PasswordAuthenticator {

    override fun authenticate(username: String, password: String, serverSession: ServerSession): Boolean {
        assertEquals(username, usr, "failed to match username")
        assertEquals(password, pwd, "failed to match password")
        return true
    }
}

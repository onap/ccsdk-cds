/*
 *  Copyright © 2019 IBM. Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.ssh.service.echoShell

import org.apache.sshd.common.Factory
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.command.Command
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.io.OutputStream

class EchoShellFactory : Factory<Command> {

    override fun create(): Command {
        return EchoShell()
    }

    companion object {

        val INSTANCE = EchoShellFactory()
    }
}

class EchoShell : Command, Runnable {

    var `in`: InputStream? = null
        private set
    var out: OutputStream? = null
        private set
    var err: OutputStream? = null
        private set
    private var callback: ExitCallback? = null
    var environment: Environment? = null
        private set
    private var thread: Thread? = null

    override fun setInputStream(`in`: InputStream) {
        this.`in` = `in`
    }

    override fun setOutputStream(out: OutputStream) {
        this.out = out
    }

    override fun setErrorStream(err: OutputStream) {
        this.err = err
    }

    override fun setExitCallback(callback: ExitCallback) {
        this.callback = callback
    }

    @Throws(IOException::class)
    override fun start(env: Environment) {
        environment = env
        thread = Thread(this, "EchoShell")
        thread!!.isDaemon = true
        thread!!.start()
    }

    override fun destroy() {
        thread!!.interrupt()
    }

    override fun run() {
        val r = BufferedReader(InputStreamReader(`in`))
        try {
            while (true) {
                val s = r.readLine() ?: return
                out!!.write((s + "\n").toByteArray())
                out!!.write("#".toByteArray())
                out!!.flush()
                if ("exit" == s) {
                    return
                }
            }
        } catch (e: InterruptedIOException) {
            // Ignore
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            callback!!.onExit(0)
        }
    }
}

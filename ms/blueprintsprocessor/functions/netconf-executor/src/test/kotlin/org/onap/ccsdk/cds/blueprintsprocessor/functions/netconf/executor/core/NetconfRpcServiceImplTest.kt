package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.mocks.NetconfDeviceSimulator

class NetconfRpcServiceImplTest {

    private var device: NetconfDeviceSimulator? = null
    private var deviceInfo: DeviceInfo? = null

    @Before
    fun before() {
        deviceInfo = DeviceInfo().apply {
            username = "username"
            password = "password"
            ipAddress = "localhost"
            port = 2224
            connectTimeout = 10
        }

        device = NetconfDeviceSimulator(deviceInfo!!.port)
        device!!.start()
    }

    @After
    fun after() {
        device!!.stop()
    }

    @Test
    fun setNetconfSession() {

    }

    @Test
    fun getConfig() {

        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.getConfig("filter","target").status.equals("failure"))
    }


    @Test
    fun deleteConfig() {

        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.deleteConfig("target").status.equals("failure"))
    }

    @Test
    fun lock() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.lock("target").status.equals("failure"))
    }

    @Test
    fun unLock() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.unLock("target").status.equals("failure"))
    }

    @Test
    fun commit() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.commit(true,60,"persist","1").status.equals("failure"))

    }

    @Test
    fun cancelCommit() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!, NetconfRpcServiceImpl(DeviceInfo()))
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(DeviceInfo())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()

        Assert.assertNotNull(netconfRpcServiceImpl.cancelCommit("1"))
    }

    @Test
    fun discardConfig() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.discardConfig().status.equals("failure"))

    }

    @Test
    fun editConfig() {
    }

    @Test
    fun validate() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo!!)
        val netconfSession = NetconfSessionImpl(deviceInfo!!, netconfRpcServiceImpl)
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        Assert.assertTrue(netconfRpcServiceImpl.validate("target").status.equals("failure"))

    }

}
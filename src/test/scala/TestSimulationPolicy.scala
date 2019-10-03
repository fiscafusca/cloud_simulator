import java.util.Calendar

import com.gfisca2.{SimulationPolicy, NewCloudlet}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.{Cloudlet, Datacenter, Vm}
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.{FlatSpec, Outcome}

class TestSimulationPolicy extends FlatSpec {

  CloudSim.init(1, Calendar.getInstance(), false)
  val conf: Config = ConfigFactory.load("testparams.conf")

  private val cloudletsNum = conf.getInt("cloudlets.count")
  behavior of "createCloudlet() method"
  it should "return a container of " + cloudletsNum + " new cloudlets" in {
    val cloudlets: List[Cloudlet] = SimulationPolicy.createCloudlet(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    assert(cloudlets.size == cloudletsNum)
  }
  private val num_user = conf.getInt("main.num_user")
  it should "associate the number of users " + num_user + " to each cloudlet" in {
    val cloudlets: List[Cloudlet] = SimulationPolicy.createCloudlet(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    cloudlets.foreach { c =>
      assert(c.getUserId == num_user)
    }
  }

  private val vmNum = conf.getInt("vms.count")
  behavior of "createVM() method"
  it should "return a container of " + vmNum + " new VMs" in {
    val vms: List[Vm] = SimulationPolicy.createVM(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    assert(vms.size == vmNum)
  }
  it should "associate the number of users " + num_user + " to each vm" in {
    val vms: List[Vm] = SimulationPolicy.createVM(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    vms.foreach { v =>
      assert(v.getUserId == num_user)
    }
  }

  behavior of "createDatacenter() method"
  it should "return a new object of type Datacenter" in {
    val datacenter = SimulationPolicy.createDatacenter("test_datacenter", conf)
    assert(datacenter.getClass.getSimpleName == "Datacenter")
  }
  private val hostNum = conf.getInt("datacenter.host_num")
  it should "contain " + hostNum + " hosts in the host list" in {
    val datacenter = SimulationPolicy.createDatacenter("test_datacenter", conf)
    assert(datacenter.getHostList.size() == hostNum)
  }

  behavior of "createMasterNode() method"
  private val masterNodeId = conf.getInt("masternode.id")
  private val masterNodeName = "MasterNode_" + masterNodeId
  it should "return a new object of type MasterNode with name set to " + masterNodeName in {
    val masterNode = SimulationPolicy.createMasterNode(masterNodeId, "testparams.conf")
    assert(masterNode.getClass.getSimpleName == "MasterNode")
    assert(masterNode.getName == masterNodeName)
  }

}
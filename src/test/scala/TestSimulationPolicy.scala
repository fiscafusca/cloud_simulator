import java.util.Calendar

import com.gfisca2.SimulationPolicy.{cloudletList, createCloudlet, createDatacenter, createMasterNode, createVM, vmList}
import com.gfisca2.{NewCloudlet, SimulationPolicy}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.Vm
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.FlatSpec

import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters._

class TestSimulationPolicy extends FlatSpec {

  CloudSim.init(1, Calendar.getInstance(), false)
  val conf: Config = ConfigFactory.load("testparams.conf")

  private val cloudletsNum = conf.getInt("test.cloudlets.count")
  behavior of "createCloudlet() method"
  it should "return a container of " + cloudletsNum + " new cloudlets" in {
    val cloudlets: List[NewCloudlet] = SimulationPolicy.createCloudlet(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    assert(cloudlets.size == cloudletsNum)
  }
  private val num_user = conf.getInt("main.num_user")
  it should "associate the number of users " + num_user + " to each cloudlet" in {
    val cloudlets: List[NewCloudlet] = SimulationPolicy.createCloudlet(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    cloudlets.foreach { c =>
      assert(c.getUserId == num_user)
    }
  }

  private val vmNum = conf.getInt("test.vms.count")
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
  private val hostNum = conf.getInt("test.datacenter.host_num")
  it should "contain " + hostNum + " hosts in the host list" in {
    val datacenter = SimulationPolicy.createDatacenter("test_datacenter", conf)
    assert(datacenter.getHostList.size() == hostNum)
  }

  behavior of "createMasterNode() method"
  private val masterNodeId = conf.getInt("test.masternode.id")
  private val masterNodeName = "MasterNode_" + masterNodeId
  it should "return a new object of type MasterNode with name set to " + masterNodeName in {
    val masterNode = SimulationPolicy.createMasterNode(masterNodeId, "testparams.conf")
    assert(masterNode.getClass.getSimpleName == "MasterNode")
    assert(masterNode.getName == masterNodeName)
  }

  behavior of "getOverallCost() method"
  private val totalCost = conf.getDouble("test.cloudlets.cost")*conf.getInt("test.cloudlets.count")
  it should "return a total cost equal to " + totalCost in {
    val cloudlets: List[NewCloudlet] = SimulationPolicy.createCloudlet(conf.getInt("main.num_user"), conf.getInt("main.idShift"), conf)
    cloudlets.foreach(c => c.setCloudletCost(conf.getDouble("test.cloudlets.cost")))
    assert(SimulationPolicy.getOverallCost(asJava(cloudlets)) == totalCost)
  }

  behavior of "processEvents() method of MasterNode.java"
  private val reducersCount = 2
  private val conf2: Config = ConfigFactory.load("testparams2.conf")
  private val vmList2 : ListBuffer[Vm] = ListBuffer[Vm]()
  private val cloudletList2 : ListBuffer[NewCloudlet] = ListBuffer[NewCloudlet]()
  it should "generate " + reducersCount + " reducers with 4 mappers" in {
    val datacenter0 = createDatacenter("Datacenter_0", conf2)
    val masterNode = createMasterNode(0, "testparams2.conf")
    val masterNodeId = masterNode.getId
    vmList2 ++= createVM(masterNodeId, conf2.getInt("main.idShift"), conf2)
    vmList2.toList
    masterNode.submitVmList(asJava(vmList2))
    cloudletList2 ++= createCloudlet(masterNodeId, conf2.getInt("main.idShift"), conf2)
    cloudletList2.toList
    println(cloudletList2.size)
    masterNode.submitCloudletList(asJava(cloudletList2))
    CloudSim.startSimulation()
    val newList = masterNode.getCloudletReceivedList[NewCloudlet]
    val count = asScala(newList).count(c => c.getCloudletType == NewCloudlet.Type.REDUCER)
    assert(count == reducersCount)
  }

}
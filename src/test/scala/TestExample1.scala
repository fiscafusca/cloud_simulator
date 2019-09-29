import org.cloudbus.cloudsim.Cloudlet
import org.scalatestplus.junit.AssertionsForJUnit
import com.gfisca2.Example1
import com.typesafe.config.{Config, ConfigFactory}
import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}
import org.cloudbus.cloudsim.{Cloudlet, Datacenter, DatacenterCharacteristics, Host, Log, NetworkTopology, Pe, Storage, UtilizationModelFull, Vm, VmAllocationPolicySimple, VmSchedulerTimeShared}
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.javaapi.CollectionConverters.asJava
import scala.collection.mutable.ListBuffer

class TestExample1 extends AssertionsForJUnit {

  def testCreateCloudlets(): Unit = {

    val user_id = 2
    val idShift = 0

    val conf: Config = ConfigFactory.load()

    val testListCloudlets : ListBuffer[Cloudlet] = ListBuffer[Cloudlet]()

    testListCloudlets ++= Example1.createCloudlet(user_id, idShift)

    assert(testListCloudlets.size == conf.getInt("cloudlet_0.count")+conf.getInt("cloudlet_1.count"))

  }

}

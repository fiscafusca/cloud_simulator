package com.gfisca2

import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}
import org.cloudbus.cloudsim.{Cloudlet, CloudletSchedulerSpaceShared, Datacenter, DatacenterCharacteristics, Host, NetworkTopology, Pe, Storage, UtilizationModelFull, Vm, VmAllocationPolicySimple, VmSchedulerTimeShared}
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.javaapi.CollectionConverters._
import scala.collection.mutable.ListBuffer

/**
 * An example showing the creation of 2 datacenters with both map/reduce and general cloudlets running on them.
 * This simulation is executed applying the defined scheduling policy.
 *
 * @author Giorgia Fiscaletti
 */
object SimulationPolicy {

  /**
   * The cloudlet list.
   */
  val cloudletList: ListBuffer[Cloudlet] = ListBuffer[Cloudlet]()
  /**
   * The VM list.
   */
  val vmList: ListBuffer[Vm] = ListBuffer[Vm]()
  /**
   * The logger used for information recording.
   */
  val LOG: Logger = LoggerFactory.getLogger(getClass)
  /**
   * Creates main() to run this example.
   *
   * @param args the args
   */
  def main(args: Array[String]) {

    LOG.info("Starting simulation...")

    try {

      val calendar = Calendar.getInstance() // Calendar whose fields have been initialized with the current date and time.

      LOG.info("Select the name of the configuration file for the desired simulation to run (type the correspondent index): ")

      LOG.info("1.0 # of single-core VMs ≥ # of dual-core VMs & # of single-core cloudlets ≥ # of dual-core cloudlets with scheduling policy;")
      LOG.info("1.1 # of single-core VMs ≥ # of dual-core VMs & # of single-core cloudlets ≥ # of dual-core cloudlets without scheduling policy;")

      LOG.info("2.0 # of single-core VMs ≥ # of dual-core VMs & # of single-core cloudlets < # of dual-core cloudlets with scheduling policy;")
      LOG.info("2.1 # of single-core VMs ≥ # of dual-core VMs & # of single-core cloudlets < # of dual-core cloudlets without scheduling policy;")

      LOG.info("3.0 # of single-core VMs < # of dual-core VMs & # of single-core cloudlets < # of dual-core cloudlets with scheduling policy;")
      LOG.info("3.1 # of single-core VMs < # of dual-core VMs & # of single-core cloudlets < # of dual-core cloudlets without scheduling policy;")

      LOG.info("4.0 # of single-core VMs < # of dual-core VMs & # of single-core cloudlets ≥ # of dual-core cloudlets with scheduling policy;")
      LOG.info("4.1 # of single-core VMs < # of dual-core VMs & # of single-core cloudlets ≥ # of dual-core cloudlets without scheduling policy;")

      val input = scala.io.StdIn.readDouble()

      val confName : String = chooseFile(input)

      /**
       * Initialization of the parameter to load the configuration.
       */
      val conf: Config = ConfigFactory.load(confName)

      // Initialization of the CloudSim library.
      CloudSim.init(conf.getInt("main.num_user"), calendar, conf.getBoolean("main.trace_flag"))

      // Creation of the 2 datacenters.
      val datacenter0 = createDatacenter("Datacenter_0", conf)
      val datacenter1 = createDatacenter("Datacenter_1", conf)

      // Creation of the Master Node.
      val masterNode = createMasterNode(0, confName)
      val masterNodeId = masterNode.getId

      // Creation of a list of Virtual Machines with the parameters defined in the configuration file.
      // The VMs are directly added to the list.
      vmList ++= createVM(masterNodeId, conf.getInt("main.idShift"), conf)
      vmList.toList

      // Submission of VM list to the Master Node.
      masterNode.submitVmList(asJava(vmList))

      // Creation of a list of Cloudlets with the parameters defined in the configuration file.
      // The Cloudlets are directly added to the list.
      cloudletList ++= createCloudlet(masterNodeId, conf.getInt("main.idShift"), conf)
      cloudletList.toList

      // Submission of Cloudlet list to the Master Node.
      masterNode.submitCloudletList(asJava(cloudletList))

      // Mapping of CloudSim entities to BRITE entities to add the delay.
      // It was assumed that there were different delays between the Master Node and the 2 different datacenters.
      NetworkTopology.addLink(datacenter0.getId, masterNode.getId, conf.getDouble("main.bw_0"), conf.getDouble("main.lat_0"))
      NetworkTopology.addLink(datacenter1.getId, masterNode.getId, conf.getDouble("main.bw_1"), conf.getDouble("main.lat_1"))

      // Start the simulation.
      CloudSim.startSimulation()

      val newList = masterNode.getCloudletReceivedList[NewCloudlet]

      CloudSim.stopSimulation()

      // Print results when the simulation is over.
      printCloudletList(newList)

      LOG.info("Simulation finished!")

    } catch {
      case e: Exception =>
        e.printStackTrace()
        LOG.error("An error occurred.")
    }
  }

  def createCloudlet(userId: Int, idShift: Int, conf: Config): List[NewCloudlet] = { // Creates a container to store Cloudlets
    // Creates a container to store the desired Cloudlets
    val list = ListBuffer[NewCloudlet]()
    val utilizationModel = new UtilizationModelFull

    // For each distinct cloudlet type defined in the configuration file...
    0.until(conf.getInt("main.cloudlet_types")).foreach { i =>
      // Creates the desired number of Cloudlets with the parameters defined in the configuration file.
      0.until(conf.getInt("cloudlet_" + i + ".count")).foreach { j =>
        val cloudlet = new NewCloudlet(
          idShift + j,
          conf.getInt("cloudlet_" + i + ".length"),
          conf.getInt("cloudlet_" + i + ".pesNumber"),
          conf.getInt("cloudlet_" + i + ".fileSize"),
          conf.getInt("cloudlet_" + i + ".outputSize"),
          utilizationModel, utilizationModel, utilizationModel,
          conf.getInt("cloudlet_" + i + ".type")
        )
        // Setting the cloudlet owner.
        cloudlet.setUserId(userId)
        list.addOne(cloudlet)

      }

    }

    list.toList

  }

  def createVM(userId: Int, idShift: Int, conf: Config): List[Vm] = { // Creates a container to store Cloudlets
    // Creates a container to store the desired Cloudlets
    val list = ListBuffer[Vm]()

    // For each distinct VM type defined in the configuration file...
    0.until(conf.getInt("main.vm_types")).foreach { i =>
      // Creates the desired number of VMs with the parameters defined in the configuration file.
      0.until(conf.getInt("vm_" + i + ".count")).foreach { j =>
        val vm = new Vm(
          conf.getInt("vm_" + i + ".idShift") + j,
          userId,
          conf.getInt("vm_" + i + ".mips"),
          conf.getInt("vm_" + i + ".pesNumber"),
          conf.getInt("vm_" + i + ".ram"),
          conf.getInt("vm_" + i + ".bw"),
          conf.getInt("vm_" + i + ".size"),
          conf.getString("vm_" + i + ".vmm"),
          new CloudletSchedulerSpaceShared()
        )
        list.addOne(vm)
      }

    }

    list.toList

  }

  def createDatacenter(name: String, conf: Config): Datacenter = {

    // Creates the datacenter.
    val hostList = ListBuffer[Host]() // List to store the machine(s)
    val peList1 = ListBuffer[Pe]()    // First list of processing elements, defining the first CPU
    val peList2 = ListBuffer[Pe]()    // Second list of processing elements, defining the second CPU

    // Cores for the CPU of the first host type
    0.until(conf.getInt("datacenter.cores_0")).foreach(
      i => peList1.addOne(new Pe(i, new PeProvisionerSimple(conf.getInt("datacenter.mips_0"))))
    )
    peList1.toList

    // Cores for the CPU of the second host type
    0.until(conf.getInt("datacenter.cores_1")).foreach(
      i => peList2.addOne(new Pe(i, new PeProvisionerSimple(conf.getInt("datacenter.mips_1"))))
    )
    peList2.toList

    // For each distinct host type defined in the configuration file...
    0.until(conf.getInt("datacenter.host_types")).foreach { i =>
      // Creates the desired number of hosts with the parameters defined in the configuration file.
      0.until(conf.getInt("datacenter.hostCount_" + i)).foreach(
        hostList.addOne(
          new Host(conf.getInt("datacenter.hostId_" + i),
            new RamProvisionerSimple(conf.getInt("datacenter.ram")),
            new BwProvisionerSimple(conf.getInt("datacenter.bw")),
            conf.getInt("datacenter.storage"),
            asJava(peList1),
            new VmSchedulerTimeShared(asJava(peList1)))
        )
      )

    }

    // Create a DatacenterCharacteristics object that stores the
    // properties of a data center: architecture, OS, list of
    // Machines, allocation policy: time- or space-shared, time zone
    // and its price (G$/Pe time unit). The characteristics are defined
    // in the configuration file.

    val storageList = ListBuffer[Storage]() // we are not adding SAN devices

    val characteristics = new DatacenterCharacteristics(
      conf.getString("datacenter.arch"),
      conf.getString("datacenter.os"),
      conf.getString("datacenter.vmm"),
      asJava(hostList),
      conf.getDouble("datacenter.time_zone"),
      conf.getDouble("datacenter.cost"),
      conf.getDouble("datacenter.costPerMem"),
      conf.getDouble("datacenter.costPerStorage"),
      conf.getDouble("datacenter.costPerBw")
    )

    storageList.toList

    // Create a PowerDatacenter object.
    val datacenter: Datacenter = try {
      new Datacenter(name, characteristics, new VmAllocationPolicySimple(asJava(hostList)), asJava(storageList), 0)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return null
    }

    datacenter

  }

  def createMasterNode(id: Int, confName: String): MasterNode = {
    // Creates the Master Node.
    val masterNode: MasterNode = try {
      new MasterNode("MasterNode_"+id, confName)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return null
    }

    masterNode

  }

  def getOverallCost(cloudletList : util.List[NewCloudlet]) : Double = {
    val costList : ListBuffer[Double] = ListBuffer[Double]()
    asScala(cloudletList).foreach(cloudlet => costList += cloudlet.getCloudletCost)
    costList.toList
    costList.sum
  }

  /**
   * Prints the Cloudlet objects
   *
   * @param list list of Cloudlets
   */
  def printCloudletList(list: util.List[NewCloudlet]): Unit = {

    val indent = "    "

    LOG.info("\n")
    LOG.info("========== OUTPUT ==========")
    LOG.info("Cloudlet ID" + indent + "Type" + indent + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Cloudlet cost")
    val dft = new DecimalFormat("###.##")

    list.forEach{cloudlet =>
      if(cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        if(cloudlet.getCloudletType == NewCloudlet.Type.REDUCER)
          LOG.info(indent + cloudlet.getCloudletFullId
            + indent + indent + cloudlet.getCloudletType
            + indent + indent + "SUCCESS"
            + indent + indent + cloudlet.getResourceId
            + indent + indent + indent + cloudlet.getVmId
            + indent + indent + dft.format(cloudlet.getActualCPUTime)
            + indent + indent + dft.format(cloudlet.getExecStartTime)
            + indent + indent + dft.format(cloudlet.getFinishTime)
            + indent + indent + dft.format(cloudlet.getCloudletCost)
            + indent + indent + "Reducing results of mappers:" + cloudlet.getAssociatedMappers
          )
        else {
          LOG.info(indent + cloudlet.getCloudletFullId
            + indent + indent + cloudlet.getCloudletType
            + indent + indent + "SUCCESS"
            + indent + indent + cloudlet.getResourceId
            + indent + indent + indent + cloudlet.getVmId
            + indent + indent + dft.format(cloudlet.getActualCPUTime)
            + indent + indent + dft.format(cloudlet.getExecStartTime)
            + indent + indent + dft.format(cloudlet.getFinishTime)
            + indent + indent + dft.format(cloudlet.getCloudletCost)
          )
        }
      } else {
        LOG.info(indent + cloudlet.getCloudletFullId
          + indent + indent + cloudlet.getCloudletType
          + indent + indent + "FAILED")
      }
    }

    val overallCost = getOverallCost(list)
    LOG.info("OVERALL COST: " + overallCost)

  }

  /**
   * Selects the configuration chosen by the user
   *
   * @param input index chosen by the user
   * @return the file name
   */
  def chooseFile(input : Double): String = {

    input match {
      case 1.0 => "withPolicy1.conf"
      case 1.1 => "noPolicy1.conf"
      case 2.0 => "withPolicy2.conf"
      case 2.1 => "noPolicy2.conf"
      case 3.0 => "withPolicy3.conf"
      case 3.1 => "noPolicy3.conf"
      case 4.0 => "withPolicy4.conf"
      case 4.1 => "noPolicy4.conf"
    }

  }

}

# CS441 - Engineering of Distributed Objects for Cloud Computing: homework 1
### Giorgia Fiscaletti, UIN: 669755907

## Introduction

The purpose of this homework was to build a cloud simulator based on the CloudSim framework, in order to evaluate the execution of application in custom cloud datacenters. The original framework was extended to provide further functionalities, such as a new scheduling policy based on CPU cores and the support for map/reduce tasks.
The project is developed in Scala and can be compiled using SBT. The CloudSim classes that needed further features were extended in Java.

## Instructions

### IntelliJ IDEA

- Open IntelliJ IDEA and select "Check out from Version Control" in the welcome screen
- Select "Git"
- Enter the repository URL (below) and click on "Clone"
```
https://giorgiafiscaletti2@bitbucket.org/giorgiafiscaletti2/giorgia_fiscaletti_hw1.git
```
- Import the sbt project (a log window will appear on the bottom right of the screen) keeping the default settings
- Go in /Giorgia_Fiscaletti_hw1/src/main/scala/com/gfisca2 and select the simulation to run

### SBT CLI

- Open a terminal and type:
```
git clone https://giorgiafiscaletti2@bitbucket.org/giorgiafiscaletti2/giorgia_fiscaletti_hw1.git
```
- To run the tests, type:
```
sbt clean compile test
```
- To run the simulations, type the following command and select the desired simulation when asked:
```
sbt clean compile run
```
## Project Structure

The project has been developed both in Scala and Java. The simulations and the tests have been written in Scala, while the extensions of the CloudSim classes are written in Java. A configuration file is provided for each simulation.

### Scala

#### Simulations

The simulations provided are:

- SimulationPolicy: simulation with the implemented allocation policy considering the impact of communication between cores in multi-cores CPUs  
- SimulationNoPolicy: simulation without the implemented allocation policy

#### Tests

The test files provided are:

- TestSimulationPolicy: a file to test the SimulationPolicy object, with tests for the following methods:
  
  - createCloudlet(): to check if the number of the created cloudlets in the container is correct and if the number of users is correctly associated to each cloudlet;
  - createVM(): to check if the number of the created VMs in the container is correct and if the number of users is correctly associated to each cloudlet;
  - createDatacenter(): to check if the Datacenter object is correctly instantiated and if the number of hosts is correct;
  - createMasterNode(): to check if the MasterNode object is correctly instantiated;
  
### Java

#### Extended classes

The following are the classes extended from the CloudSim framework:

- MasterNode: extends the DatacenterBroker class, providing further functionalities such as the simulation of the map/reduce task with dynamic allocation of reducers, and the scheduling policy based on CPU cores;
- NewCloudlet: extends the Cloudlet class, providing the distinction between different kinds of cloudlets (MAPPER, REDUCER and GENERAL)

## Configuration files

The two configuration files are withPolicy.conf and noPolicy.conf, respectively used in SimulationPolicy.scala and SimulationNoPolicy.scala.
The configurations share all the parameters related to Datacenters, Hosts, VMs and Cloudlets. The only parameter that differs between the two configuration is a boolean value that is set to true in the first one to enable the scheduling policy, and fals ein the second one to disable it.

## Parameters

- Main: contains the parameters used in the main method of the simulation.

  - num_user = 2, the number of users
  - trace_flag = false, boolean to trace events
  - bw_0 = 10.0, the bandwidth set for the first datacenter
  - bw_1 = 10.0, the bandwidth set for the second datacenter
  - lat_0 = 1, the latency set for the fist datacenter
  - lat_1 = 2, the latency set for the second datacenter
  - idShift = 0, base ID to increase for ID assignment
  - cloudlet_types = 2, the distinct types of cloudlets (mapper/reducer/general)
  - vm_types = 2, the distinct types of VMs (CPU, memory, etc.)
  
- Cloudlet_0: contains the parameters for the first type of cloudlet (mapper).

  - length = 20000, millions of instructions executed
  - fileSize = 300, input size
  - outputSize = 300, output size
  - pesNumber = 1, number of processing elements required
  - count = 10, the number of cloudlet to generate
  - type = 0, integer representing the cloudlet type (mapper in this case)
  
- Cloudlet_1: contains the parameters for the second type of cloudlet (general).

  - length = 40000, millions of instructions executed
  - fileSize = 300, input size
  - outputSize = 300, output size
  - pesNumber = 2, number of processing elements required
  - count = 5, the number of cloudlet to generate
  - type = 2, integer representing the cloudlet type (general in this case)
  
- Cloudlet_2: contains the parameters for the third type of cloudlet (reducer). Note that in this case the parameter "count" is not needed, since we are going to generate the cloudlets at runtime depending on the number of submitted mappers.
  
  - length = 20000, millions of instructions executed
  - fileSize = 600, input size (sum of the output of 2 mappers, since in the given configuration each reducer takes the results of 2 mappers)
  - outputSize = 600, output size
  - pesNumber = 1, number of processing elements required
  - type = 0, integer representing the cloudlet type (reducer in this case)

- vm_0: contains the parameters for the first type of VM.
  
  - idShift = 0, base ID to increase for ID assignment
  - size = 524288, storage size allocated for the VM
  - ram = 32768, RAM allocated for the VM
  - mips = 500, MIPS of the VM
  - bw = 1000, bandwidth of the VM
  - pesNumber = 2, number of PEs assigned to the VM
  - vmm = "Xen", VMM name
  - count = 5, number of VM to create

- vm_1: contains the parameters for the second type of VM.

  - idShift = 100, base ID to increase for ID assignment
  - size = 524288, storage size allocated for the VM
  - ram = 32768, RAM allocated for the VM
  - mips = 500, MIPS of the VM
  - bw = 1000, bandwidth of the VM
  - pesNumber = 1, number of PEs assigned to the VM
  - vmm = "Xen", VMM name
  - count = 5, number of VM to create

- Datacenter: contains the parameters for the datacenter.

  - mips_0 = 5000, MIPS of the first host
  - mips_1 = 5000, MIPS of the second host
  - host_types = 2, number of distinct host types
  - cpu_types = 2, number of distinct CPU types
  - hostCount_0 = 1, number of type 0 hosts to create
  - hostCount_1 = 1, number of type 1 hosts to create
  - hostId_0 = 0, first host ID
  - hostId_1 = 1, second host ID
  - cores_0 = 2, cores assigned of CPU type
  - cores_1 = 1, cores of the second CPU type
  - ram = 262144, RAM provided by the datacenter
  - storage = 2097152, storage provided by the datacenter
  - bw = 10000, bandwidth provided by the datacenter
  - arch = "x86", system architecture
  - os = "Linux", operating system
  - vmm = "Xen", VMM name
  - time_zone = 10.0, time zone where this resource is located
  - cost = 3.0, processing cost
  - costPerMem = 0.05, memory cost
  - costPerStorage = 0.1, storage cost
  - costPerBw = 0.1, bandwidth cost
  - peCostPerSec = 0.03, cost per second

- Masternode: contains the parameters needed in the MasterNode class implementation.

  - mapreduce = 2, the number of mappers associated to each reducer (e.g. in this case each reducer is dynamically allocated every 2 mappers)
  - sched_policy = true if the scheduling policy is enabled, false otherwise